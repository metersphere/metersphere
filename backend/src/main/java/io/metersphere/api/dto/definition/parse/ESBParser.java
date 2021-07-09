package io.metersphere.api.dto.definition.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.EsbDataStruct;
import io.metersphere.api.dto.definition.parse.esb.ESBExcelSheetInfo;
import io.metersphere.api.dto.definition.parse.esb.EsbExcelDataStruct;
import io.metersphere.api.dto.definition.parse.esb.EsbSheetDataStruct;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.service.EsbApiParamService;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.base.domain.EsbApiParamsWithBLOBs;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.SessionUtils;
import io.swagger.models.Model;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author song.tianyang
 * @Date 2021/3/10 11:14 上午
 * @Description
 */
public class ESBParser extends EsbAbstractParser {

    private Map<String, Model> definitions = null;

    /**
     * 导出模板
     *
     * @param response
     * @param fileName
     */
    public static void export(HttpServletResponse response, String fileName) {

        XSSFWorkbook wb = null;
        ServletOutputStream out = null;
        try {
            wb = getTemplate();
            out = response.getOutputStream();
            response.reset();
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
            wb.write(out);
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {

                }
            }
            if (wb != null) {
                try {
                    wb.close();
                } catch (Exception e) {

                }
            }
        }
    }

    private static XSSFWorkbook getTemplate() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        Map<String, XSSFCellStyle> cellStyleMap = createCellStle(workbook);
        XSSFSheet headSheet = workbook.createSheet("公共报文头");
        generateSheet(headSheet, font, cellStyleMap);
        XSSFSheet bodySheet = workbook.createSheet("接口报文信息");
        generateSheet(bodySheet, font, cellStyleMap);
        return workbook;
    }

    private static Map<String, XSSFCellStyle> createCellStle(XSSFWorkbook workbook) {
        Map<String, XSSFCellStyle> cellStype = new HashMap<>();
        short[] colorIndexArr = {IndexedColors.LIGHT_GREEN.getIndex(), IndexedColors.ORCHID.getIndex(), IndexedColors.YELLOW.getIndex()};
        for (short colorIndex : colorIndexArr) {
            XSSFCellStyle style = workbook.createCellStyle();
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);

            String name = "";
            if (colorIndex == IndexedColors.LIGHT_GREEN.getIndex()) {
                style.setFillForegroundColor(new XSSFColor(new java.awt.Color(204, 255, 204)));
                name = "green";
            } else if (colorIndex == IndexedColors.ORCHID.getIndex()) {
                style.setFillForegroundColor(new XSSFColor(new java.awt.Color(151, 50, 101)));
                name = "pop";
            } else if (colorIndex == IndexedColors.YELLOW.getIndex()) {
                style.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 153)));
                name = "yellow";
            } else {
                name = "default";
            }
            cellStype.put(name, style);
        }
        return cellStype;
    }

    private static void generateSheet(XSSFSheet sheet, XSSFFont font, Map<String, XSSFCellStyle> cellStyleMap) {
        if (sheet == null) {
            return;
        }
        sheet.setColumnWidth(0, 4000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 880);
        sheet.setColumnWidth(5, 4000);
        sheet.setColumnWidth(6, 4000);
        sheet.setColumnWidth(7, 4000);
        sheet.setColumnWidth(8, 4000);
        sheet.setColumnWidth(9, 4000);

        /**
         * 模版生成:
         * 字体大小：9
         * row1: 1交易码 2交易名称  （head的话是5）6(紫色[pop]背景空白单元格,下同) 7服务名称 8服务场景
         * row2: 1.请输入交易码(必填) 2请输入交易名称(必填) 7请输入服务名称(如果不填，则以交易名称为主) 8请输入服务场景(选填)
         * row3: 合并7-10单元格，输入：请输入系统名称 背景色:黄色[yellow]
         * row4: 1.英文名称2.中文名称3.数据类型/长度4.是否必输5.备注    7.英文名称8.数据类型/长度9.中文名称 10备注 背景色:黄色
         * row5: 整行都是青色【green】，1:输入
         * row6: 3：请输入STRING(具体长度) 或 ARRAY;    8:同3
         * row7:无
         * row8: 整行都是青色，1:输出
         * row9: 3：请输入STRING(具体长度) 或 ARRAY;    8:同3
         */
        XSSFRow row1 = sheet.createRow(0);
        setCellValue("交易码", row1.createCell(0), font, cellStyleMap.get("default"));
        setCellValue("交易名称", row1.createCell(1), font, cellStyleMap.get("default"));
        setCellValue("", row1.createCell(2), font, cellStyleMap.get("default"));
        setCellValue("", row1.createCell(3), font, cellStyleMap.get("default"));
//        if (isHead) {
        setCellValue("", row1.createCell(4), font, cellStyleMap.get("default"));
        setCellValue("服务名称", row1.createCell(5), font, cellStyleMap.get("default"));
        setCellValue("服务场景", row1.createCell(6), font, cellStyleMap.get("default"));
        setCellValue("", row1.createCell(7), font, cellStyleMap.get("default"));
        setCellValue("", row1.createCell(8), font, cellStyleMap.get("default"));
        setCellValue("", row1.createCell(9), font, cellStyleMap.get("default"));

//        } else {
//            setCellValue("", row1.createCell(4), font, cellStyleMap.get("default"));
//            setCellValue("", row1.createCell(5), font, cellStyleMap.get("pop"));
//            setCellValue("服务名称", row1.createCell(6), font, cellStyleMap.get("default"));
//            setCellValue("服务场景", row1.createCell(7), font, cellStyleMap.get("default"));
//            setCellValue("", row1.createCell(8), font, cellStyleMap.get("default"));
//            setCellValue("", row1.createCell(9), font, cellStyleMap.get("default"));
//            setCellValue("", row1.createCell(10), font, cellStyleMap.get("default"));
//        }


        XSSFRow row2 = sheet.createRow(1);
        setCellValue("请输入交易码(必填)", row2.createCell(0), font, cellStyleMap.get("default"));
        setCellValue("请输入交易名称(必填)", row2.createCell(1), font, cellStyleMap.get("default"));
        setCellValue("", row2.createCell(2), font, cellStyleMap.get("default"));
        setCellValue("", row2.createCell(3), font, cellStyleMap.get("default"));
//        if (isHead) {
        setCellValue("", row2.createCell(4), font, cellStyleMap.get("pop"));
        setCellValue("请输入服务名称(如果不填，则以交易名称为主)", row2.createCell(5), font, null);
        setCellValue("请输入服务场景(选填)", row2.createCell(6), font, cellStyleMap.get("default"));
        setCellValue("", row2.createCell(7), font, cellStyleMap.get("default"));
        setCellValue("", row2.createCell(8), font, cellStyleMap.get("default"));
        setCellValue("", row2.createCell(9), font, cellStyleMap.get("default"));
//        } else {
//            setCellValue("", row2.createCell(4), font, cellStyleMap.get("default"));
//            setCellValue("", row2.createCell(5), font, cellStyleMap.get("pop"));
//            setCellValue("请输入服务名称(如果不填，则以交易名称为主)", row2.createCell(6), font, null);
//            setCellValue("请输入服务场景(选填)", row2.createCell(7), font, cellStyleMap.get("default"));
//            setCellValue("", row2.createCell(8), font, cellStyleMap.get("default"));
//            setCellValue("", row2.createCell(9), font, cellStyleMap.get("default"));
//            setCellValue("", row2.createCell(10), font, cellStyleMap.get("default"));
//        }

        XSSFRow row3 = sheet.createRow(2);
        setCellValue("", row3.createCell(0), font, cellStyleMap.get("yellow"));
        setCellValue("", row3.createCell(1), font, cellStyleMap.get("yellow"));
        setCellValue("", row3.createCell(2), font, cellStyleMap.get("yellow"));
        setCellValue("", row3.createCell(3), font, cellStyleMap.get("yellow"));
//        if (isHead) {
        setCellValue("", row3.createCell(4), font, cellStyleMap.get("yellow"));
        setCellValue("请输入系统名称", row3.createCell(5), font, cellStyleMap.get("yellow"));
        setCellValue("", row3.createCell(6), font, cellStyleMap.get("yellow"));
        setCellValue("", row3.createCell(7), font, cellStyleMap.get("yellow"));
        setCellValue("", row3.createCell(8), font, cellStyleMap.get("yellow"));
        setCellValue("", row3.createCell(9), font, cellStyleMap.get("yellow"));
        CellRangeAddress region1 = new CellRangeAddress(2, 2, 0, 3);
        sheet.addMergedRegion(region1);
        CellRangeAddress region2 = new CellRangeAddress(2, 2, 5, 9);
        sheet.addMergedRegion(region2);
//        } else {
//            setCellValue("", row3.createCell(4), font, cellStyleMap.get("yellow"));
//            setCellValue("", row3.createCell(5), font, cellStyleMap.get("yellow"));
//            setCellValue("请输入系统名称", row3.createCell(6), font, cellStyleMap.get("yellow"));
//            setCellValue("", row3.createCell(7), font, cellStyleMap.get("yellow"));
//            setCellValue("", row3.createCell(8), font, cellStyleMap.get("yellow"));
//            setCellValue("", row3.createCell(9), font, cellStyleMap.get("yellow"));
//            setCellValue("", row3.createCell(10), font, cellStyleMap.get("yellow"));
//            CellRangeAddress region1 = new CellRangeAddress(2, 2, 0, 4);
//            sheet.addMergedRegion(region1);
//            CellRangeAddress region2 = new CellRangeAddress(2, 2, 6, 10);
//            sheet.addMergedRegion(region2);
//        }

        XSSFRow row4 = sheet.createRow(3);
        setCellValue("英文名称", row4.createCell(0), font, cellStyleMap.get("yellow"));
        setCellValue("中文名称", row4.createCell(1), font, cellStyleMap.get("yellow"));
        setCellValue("数据类型/长度", row4.createCell(2), font, cellStyleMap.get("yellow"));
//        if (isHead) {
        setCellValue("备注", row4.createCell(3), font, cellStyleMap.get("yellow"));
        setCellValue("", row4.createCell(4), font, cellStyleMap.get("pop"));
        setCellValue("英文名称", row4.createCell(5), font, cellStyleMap.get("yellow"));
        setCellValue("数据类型/长度", row4.createCell(6), font, cellStyleMap.get("yellow"));
        setCellValue("中文名称", row4.createCell(7), font, cellStyleMap.get("yellow"));
        setCellValue("备注", row4.createCell(8), font, cellStyleMap.get("yellow"));
        setCellValue("所在报文位置", row4.createCell(9), font, cellStyleMap.get("yellow"));
//        } else {
//            setCellValue("是否必输", row4.createCell(3), font, cellStyleMap.get("yellow"));
//            setCellValue("备注", row4.createCell(4), font, cellStyleMap.get("yellow"));
//            setCellValue("", row4.createCell(5), font, cellStyleMap.get("pop"));
//            setCellValue("英文名称", row4.createCell(6), font, cellStyleMap.get("yellow"));
//            setCellValue("数据类型/长度", row4.createCell(7), font, cellStyleMap.get("yellow"));
//            setCellValue("中文名称", row4.createCell(8), font, cellStyleMap.get("yellow"));
//            setCellValue("备注", row4.createCell(9), font, cellStyleMap.get("yellow"));
//            setCellValue("所在报文位置", row4.createCell(10), font, cellStyleMap.get("yellow"));
//        }

        XSSFRow row5 = sheet.createRow(4);
        setCellValue("输入", row5.createCell(0), font, cellStyleMap.get("green"));
        setCellValue("", row5.createCell(1), font, cellStyleMap.get("green"));
        setCellValue("", row5.createCell(2), font, cellStyleMap.get("green"));
        setCellValue("", row5.createCell(3), font, cellStyleMap.get("green"));
        setCellValue("", row5.createCell(4), font, cellStyleMap.get("green"));
        setCellValue("", row5.createCell(5), font, cellStyleMap.get("green"));
        setCellValue("", row5.createCell(6), font, cellStyleMap.get("green"));
        setCellValue("", row5.createCell(7), font, cellStyleMap.get("green"));
        setCellValue("", row5.createCell(8), font, cellStyleMap.get("green"));
        setCellValue("", row5.createCell(9), font, cellStyleMap.get("green"));
//        if (!isHead) {
//            setCellValue("", row5.createCell(10), font, cellStyleMap.get("green"));
//        }

        XSSFRow row6 = sheet.createRow(5);
        setCellValue("", row6.createCell(0), font, cellStyleMap.get("default"));
        setCellValue("", row6.createCell(1), font, cellStyleMap.get("default"));
        setCellValue("请输入STRING(具体长度) 或 ARRAY", row6.createCell(2), font, cellStyleMap.get("default"));
        setCellValue("", row6.createCell(3), font, cellStyleMap.get("default"));
//        if (isHead) {
        setCellValue("", row6.createCell(4), font, cellStyleMap.get("pop"));
        setCellValue("", row6.createCell(5), font, cellStyleMap.get("default"));
        setCellValue("请输入STRING(具体长度) 或 ARRAY", row6.createCell(6), font, cellStyleMap.get("default"));
        setCellValue("", row6.createCell(7), font, cellStyleMap.get("default"));
        setCellValue("", row6.createCell(8), font, cellStyleMap.get("default"));
        setCellValue("", row6.createCell(9), font, cellStyleMap.get("default"));
//        } else {
//            setCellValue("", row6.createCell(4), font, cellStyleMap.get("default"));
//            setCellValue("", row6.createCell(5), font, cellStyleMap.get("pop"));
//            setCellValue("", row6.createCell(6), font, cellStyleMap.get("default"));
//            setCellValue("请输入STRING(具体长度) 或 ARRAY", row6.createCell(7), font, cellStyleMap.get("default"));
//            setCellValue("", row6.createCell(8), font, cellStyleMap.get("default"));
//            setCellValue("", row6.createCell(9), font, cellStyleMap.get("default"));
//            setCellValue("", row6.createCell(10), font, cellStyleMap.get("default"));
//        }


        XSSFRow row7 = sheet.createRow(6);
        setCellValue("", row7.createCell(1), font, cellStyleMap.get("default"));
        setCellValue("", row7.createCell(2), font, cellStyleMap.get("default"));
        setCellValue("", row7.createCell(3), font, cellStyleMap.get("default"));
//        if (isHead) {
        setCellValue("", row7.createCell(4), font, cellStyleMap.get("pop"));
        setCellValue("", row7.createCell(5), font, cellStyleMap.get("default"));
        setCellValue("", row7.createCell(6), font, cellStyleMap.get("default"));
        setCellValue("", row7.createCell(7), font, cellStyleMap.get("default"));
        setCellValue("", row7.createCell(8), font, cellStyleMap.get("default"));
        setCellValue("", row7.createCell(9), font, cellStyleMap.get("default"));
//        } else {
//            setCellValue("", row7.createCell(4), font, cellStyleMap.get("default"));
//            setCellValue("", row7.createCell(5), font, cellStyleMap.get("pop"));
//            setCellValue("", row7.createCell(6), font, cellStyleMap.get("default"));
//            setCellValue("", row7.createCell(7), font, cellStyleMap.get("default"));
//            setCellValue("", row7.createCell(8), font, cellStyleMap.get("default"));
//            setCellValue("", row7.createCell(9), font, cellStyleMap.get("default"));
//            setCellValue("", row7.createCell(10), font, cellStyleMap.get("default"));
//        }


        XSSFRow row8 = sheet.createRow(7);
        setCellValue("输出", row8.createCell(0), font, cellStyleMap.get("green"));
        setCellValue("", row8.createCell(1), font, cellStyleMap.get("green"));
        setCellValue("", row8.createCell(2), font, cellStyleMap.get("green"));
        setCellValue("", row8.createCell(3), font, cellStyleMap.get("green"));
        setCellValue("", row8.createCell(4), font, cellStyleMap.get("green"));
        setCellValue("", row8.createCell(5), font, cellStyleMap.get("green"));
        setCellValue("", row8.createCell(6), font, cellStyleMap.get("green"));
        setCellValue("", row8.createCell(7), font, cellStyleMap.get("green"));
        setCellValue("", row8.createCell(8), font, cellStyleMap.get("green"));
        setCellValue("", row8.createCell(9), font, cellStyleMap.get("green"));
//        if (!isHead) {
//            setCellValue("", row8.createCell(10), font, cellStyleMap.get("green"));
//        }


        XSSFRow row9 = sheet.createRow(8);
        setCellValue("", row9.createCell(0), font, cellStyleMap.get("default"));
        setCellValue("", row9.createCell(1), font, cellStyleMap.get("default"));
        setCellValue("请输入STRING(具体长度) 或 ARRAY", row9.createCell(2), font, cellStyleMap.get("default"));
        setCellValue("", row9.createCell(3), font, cellStyleMap.get("default"));
//        if (isHead) {
        setCellValue("", row9.createCell(4), font, cellStyleMap.get("pop"));
        setCellValue("", row9.createCell(5), font, cellStyleMap.get("default"));
        setCellValue("请输入STRING(具体长度) 或 ARRAY", row9.createCell(6), font, cellStyleMap.get("default"));
        setCellValue("", row9.createCell(7), font, cellStyleMap.get("default"));
        setCellValue("", row9.createCell(8), font, cellStyleMap.get("default"));
        setCellValue("", row9.createCell(9), font, cellStyleMap.get("default"));
//        } else {
//            setCellValue("", row9.createCell(4), font, cellStyleMap.get("default"));
//            setCellValue("", row9.createCell(5), font, cellStyleMap.get("pop"));
//            setCellValue("", row9.createCell(6), font, cellStyleMap.get("default"));
//            setCellValue("请输入STRING(具体长度) 或 ARRAY", row9.createCell(7), font, cellStyleMap.get("default"));
//            setCellValue("", row9.createCell(7), font, cellStyleMap.get("default"));
//            setCellValue("", row9.createCell(8), font, cellStyleMap.get("default"));
//            setCellValue("", row9.createCell(9), font, cellStyleMap.get("default"));
//            setCellValue("", row9.createCell(10), font, cellStyleMap.get("default"));
//        }

    }

    private static void setCellValue(String textValue, Cell cell, XSSFFont font, XSSFCellStyle cellStyle) {
        XSSFRichTextString richString = new XSSFRichTextString(textValue);
        richString.applyFont(font);
        if (cellStyle != null) {
            cell.setCellStyle(cellStyle);
        }
        cell.setCellValue(richString);
    }


    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) throws Exception {
        EsbExcelDataStruct excelDataStruct = this.esbImport(source);
        this.projectId = request.getProjectId();
        ApiDefinitionImport definitionImport = this.parseApiDefinitionImport(excelDataStruct, request);
        return definitionImport;
    }

    public EsbExcelDataStruct esbImport(InputStream source) throws IOException, InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(source);
        int sheetCount = workbook.getNumberOfSheets();
        EsbSheetDataStruct headStruct = null;
        List<EsbSheetDataStruct> interfaceStruct = new ArrayList<>();
        if (sheetCount > 0) {
            Sheet headSheet = workbook.getSheetAt(0);
            headStruct = this.parseExcelSheet(headSheet, true);
            for (int index = 1; index < sheetCount; index++) {
                Sheet dataSheet = workbook.getSheetAt(index);
                EsbSheetDataStruct bodyStruct = this.parseExcelSheet(dataSheet, false);
                interfaceStruct.add(bodyStruct);
            }
        }

        EsbExcelDataStruct excelData = new EsbExcelDataStruct();
        excelData.setHeadData(headStruct);
        excelData.setInterfaceList(interfaceStruct);
        return excelData;
    }

    private EsbSheetDataStruct parseExcelSheet(Sheet headSheet, boolean isHeadSheet) {
        EsbSheetDataStruct importDataStruct = new EsbSheetDataStruct();
        if (headSheet == null) {
            return importDataStruct;
        }
        ESBExcelSheetInfo sheetInfo = this.getEsbExcelSheetInfo(headSheet);

        int rowCount = headSheet.getLastRowNum();

        //根据模版样式，如果不是报文头，则要取接口信息
        if (!isHeadSheet) {
            Row interfaceInfoRow = headSheet.getRow(1);
            if (interfaceInfoRow != null) {
                List<String> rowDataArr = this.getRowDataByStartIndexAndEndIndex(interfaceInfoRow, 0, sheetInfo.getCellCount() - 1);
                if (rowDataArr.size() >= sheetInfo.getCellCount()) {
                    String interfaceCode = rowDataArr.get(sheetInfo.getSimpleCodeIndex());
                    String interfaceName = rowDataArr.get(sheetInfo.getServiceNameIndex());
                    String interfaceDesc = rowDataArr.get(sheetInfo.getServiceScenarioIndex());
                    if (StringUtils.isEmpty(interfaceName)) {
                        interfaceName = rowDataArr.get(sheetInfo.getSimpleNameIndex());
                    }
                    importDataStruct.setInterfaceInfo(interfaceCode, interfaceName, interfaceDesc);
                }
            }
        }
        //超过10行为空白，直接退出。
        //部分office/wpf生成的文件会出现几万多空行，容易造成内存溢出。这里进行判断，连续五行为空白时认为读取结束。
        int blankRowCount = 0;
        boolean isRequest = true;

        for (int startRow = sheetInfo.getRequestMessageRow(); startRow < rowCount; startRow++) {
            Row row = headSheet.getRow(startRow);
            List<String> rowDataArr = this.getRowDataByStartIndexAndEndIndex(row, 0, sheetInfo.getCellCount() - 1);
            boolean isBlankRow = this.checkBlankRow(rowDataArr, sheetInfo.getCellCount());
            if (!isBlankRow) {
                String cellFlag = rowDataArr.get(0);
                if (StringUtils.equals(cellFlag, "输出")) {
                    isRequest = false;
                }
            }
            if (isBlankRow) {
                if (isRequest) {
                    isRequest = false;
                }
                blankRowCount++;
                if (blankRowCount > 10) {
                    break;
                }
            } else {
                blankRowCount = 0;
                EsbDataStruct dataStruct = new EsbDataStruct();
                boolean initDataSuccess = dataStruct.initDefaultData(
                        rowDataArr.get(sheetInfo.getApiNameIndex()), rowDataArr.get(sheetInfo.getDataTypeIndex()), rowDataArr.get(sheetInfo.getChineNameIndex()), rowDataArr.get(sheetInfo.getDescIndex()));
                if (!initDataSuccess) {
                    continue;
                }
                boolean isHead = isHeadSheet;
                if (rowDataArr.size() > sheetInfo.getCellCount()) {
                    if (StringUtils.equals(rowDataArr.get(sheetInfo.getApiPositionIndex()), "SYS_HEAD")) {
                        isHead = true;
                    }

                }
                if (isRequest) {
                    if (isHead) {
                        importDataStruct.getReqHeadList().add(dataStruct);
                    } else {
                        importDataStruct.getRequestList().add(dataStruct);
                    }
                } else {
                    if (isHead) {
                        importDataStruct.getRspHeadList().add(dataStruct);
                    } else {
                        importDataStruct.getResponseList().add(dataStruct);
                    }
                }
            }
        }
        return importDataStruct;
    }

    private boolean checkBlankRow(List<String> rowDataArr, int rowCheckLength) {
        if (rowDataArr == null || rowDataArr.size() < rowCheckLength) {
            return true;
        }
        for (String str : rowDataArr) {
            if (StringUtils.isNotEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    private List<String> getRowDataByStartIndexAndEndIndex(Row row, int startCell, int endCell) {
        List<String> returnArray = new ArrayList<>();
        if (row == null) {
            return returnArray;
        }
        for (int i = startCell; i <= endCell; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                returnArray.add(getCellValue(cell));
            } else {
                returnArray.add("");
            }
        }
        return returnArray;
    }

    private String getCellValue(Cell cell) {
        String returnCellValue = "";
        int cellType = cell.getCellType();
        switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                returnCellValue = "";
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                returnCellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                returnCellValue = "";
                break;
            case Cell.CELL_TYPE_NUMERIC:
                returnCellValue = getValueOfNumericCell(cell);
                break;
            case Cell.CELL_TYPE_FORMULA:
                try {
                    returnCellValue = getValueOfNumericCell(cell);
                } catch (IllegalStateException e) {
                    try {
                        returnCellValue = cell.getRichStringCellValue().toString();
                    } catch (IllegalStateException e2) {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                returnCellValue = cell.getRichStringCellValue().getString();
        }
        if (returnCellValue == null) {
            returnCellValue = "";
        }
        return returnCellValue;
    }

    private String getValueOfNumericCell(Cell cell) {
        Boolean isDate = DateUtil.isCellDateFormatted(cell);
        Double d = cell.getNumericCellValue();
        String o = null;
        if (isDate) {
            o = DateFormat.getDateTimeInstance()
                    .format(cell.getDateCellValue());
        } else {
            o = getRealStringValueOfDouble(d);
        }
        return o;
    }

    // 处理科学计数法与普通计数法的字符串显示，尽最大努力保持精度
    private static String getRealStringValueOfDouble(Double d) {
        String doubleStr = d.toString();
        boolean b = doubleStr.contains("E");
        int indexOfPoint = doubleStr.indexOf('.');
        if (b) {
            int indexOfE = doubleStr.indexOf('E');
            // 小数部分
            BigInteger xs = new BigInteger(doubleStr.substring(indexOfPoint
                    + BigInteger.ONE.intValue(), indexOfE));
            // 指数
            int pow = Integer.valueOf(doubleStr.substring(indexOfE
                    + BigInteger.ONE.intValue()));
            int xsLen = xs.toByteArray().length;
            int scale = xsLen - pow > 0 ? xsLen - pow : 0;
            doubleStr = String.format("%." + scale + "f", d);
        } else {
            java.util.regex.Pattern p = Pattern.compile(".0$");
            java.util.regex.Matcher m = p.matcher(doubleStr);
            if (m.find()) {
                doubleStr = doubleStr.replace(".0", "");
            }
        }
        return doubleStr;
    }

    private ApiDefinitionImport parseApiDefinitionImport(EsbExcelDataStruct esbExcelDataStruct, ApiTestImportRequest importRequest) {
        ApiDefinitionImport resultModel = new ApiDefinitionImport();
        List<ApiDefinitionWithBLOBs> apiDataList = new ArrayList<>();

        ApiModule parentNode = ApiDefinitionImportUtil.getSelectModule(importRequest.getModuleId());
        EsbSheetDataStruct headSheetData = esbExcelDataStruct.getHeadData();
        List<EsbSheetDataStruct> interfaceDataList = esbExcelDataStruct.getInterfaceList();
        List<String> savedNames = new ArrayList<>();
        Map<String, EsbApiParamsWithBLOBs> esbApiParams = new HashMap<>();
        for (EsbSheetDataStruct interfaceData : interfaceDataList) {
            String reqName = interfaceData.getServiceName();
            if (savedNames.contains(reqName)) {
                continue;
            } else {
                savedNames.add(reqName);
            }

            String esbSendRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n${SERVICE}";
            String reqDataStructStr = generateDataStrcut(headSheetData, interfaceData, true);
            String respDataStrutStr = generateDataStrcut(headSheetData, interfaceData, false);

            String apiId = UUID.randomUUID().toString();
            ApiDefinitionWithBLOBs apiDefinition = new ApiDefinitionWithBLOBs();
            apiDefinition.setName(reqName);
            apiDefinition.setMethod("ESB");
            apiDefinition.setId(apiId);
            apiDefinition.setProjectId(this.projectId);
            apiDefinition.setModuleId(importRequest.getModuleId());
            apiDefinition.setModulePath(importRequest.getModulePath());
            apiDefinition.setRequest(genTCPSampler(esbSendRequest, reqDataStructStr));
            if (StringUtils.equalsIgnoreCase("schedule", importRequest.getType())) {
                apiDefinition.setUserId(importRequest.getUserId());
            } else {
                apiDefinition.setUserId(SessionUtils.getUserId());
            }
            apiDefinition.setProtocol(RequestType.TCP);
            buildModule(parentNode, apiDefinition, null);
            apiDataList.add(apiDefinition);

            EsbApiParamsWithBLOBs apiParams = new EsbApiParamsWithBLOBs();
            apiParams.setId(UUID.randomUUID().toString());
            apiParams.setResourceId(apiId);

            apiParams.setDataStruct(reqDataStructStr);
            apiParams.setResponseDataStruct(respDataStrutStr);
            esbApiParams.put(apiId, apiParams);
        }

        resultModel.setData(apiDataList);
        resultModel.setEsbApiParamsMap(esbApiParams);

        return resultModel;
    }

    private String genTCPSampler(String sendRequest, String esbDataStruct) {

        MsTCPSampler tcpSampler = new MsTCPSampler();
        MsJSR223PreProcessor preProcessor = new MsJSR223PreProcessor();

        tcpSampler.setTcpPreProcessor(preProcessor);
        tcpSampler.setProtocol("ESB");
        tcpSampler.setClassname("TCPClientImpl");
        tcpSampler.setReUseConnection(false);
        String script = "String report = ctx.getCurrentSampler().getRequestData();\n" +
                "        if(report!=null){\n" +
                "            //补足8位长度，前置补0\n" +
                "            String reportlengthStr = String.format(\"%08d\",report.length());\n" +
                "            report = reportlengthStr+report;\n" +
                "            ctx.getCurrentSampler().setRequestData(report);\n" +
                "        }";
//        frontScriptList.add(script);
        if (tcpSampler.getTcpPreProcessor() != null) {
            tcpSampler.getTcpPreProcessor().setScriptLanguage("groovy");
            tcpSampler.getTcpPreProcessor().setScript(script);
        }


        if (StringUtils.isNotEmpty(sendRequest)) {
            tcpSampler.setRequest(sendRequest);
        }

        if (StringUtils.isNotEmpty(esbDataStruct)) {
            EsbApiParamService esbApiParamService = CommonBeanFactory.getBean(EsbApiParamService.class);
            List<KeyValue> keyValueList = esbApiParamService.genKeyValueListByDataStruct(tcpSampler, esbDataStruct);
            tcpSampler.setParameters(keyValueList);
        }

        return JSON.toJSONString(tcpSampler);
    }

    private String generateDataStrcut(EsbSheetDataStruct head, EsbSheetDataStruct body, boolean isRequest) {
        EsbDataStruct dataStruct = new EsbDataStruct();
        dataStruct.initDefaultData("SERVICE", null, null, null);
        List<EsbDataStruct> headList = new ArrayList<>();
        List<EsbDataStruct> bodyList = new ArrayList<>();
        if (head != null) {
            if (isRequest) {
                headList.addAll(head.getReqHeadList());
                bodyList.addAll(head.getRequestList());
            } else {
                headList.addAll(head.getRspHeadList());
                bodyList.addAll(head.getResponseList());
            }
        }
        if (body != null) {
            if (isRequest) {
                headList.addAll(body.getReqHeadList());
                bodyList.addAll(body.getRequestList());
            } else {
                headList.addAll(body.getRspHeadList());
                bodyList.addAll(body.getResponseList());
            }
        }

        if (!headList.isEmpty()) {
            EsbDataStruct headStruct = new EsbDataStruct();
            headStruct.initDefaultData("SYS_HEAD", null, null, null);
            dataStruct.getChildren().add(headStruct);
            Map<String, EsbDataStruct> childrenEsbDataStructMap = new HashMap<>();
            //用来判断节点有没有在array节点内
            String lastArrayDataStrcutName = null;
            for (EsbDataStruct headData : headList) {
                if (StringUtils.equalsIgnoreCase("array", headData.getType())) {
                    if (lastArrayDataStrcutName == null) {
                        lastArrayDataStrcutName = headData.getName();
                        EsbDataStruct arrayStrcut = new EsbDataStruct();
                        arrayStrcut.initDefaultData(headData.getName(), headData.getType(), headData.getContentType(), headData.getDescription());
                        headStruct.getChildren().add(arrayStrcut);
                        childrenEsbDataStructMap.put(lastArrayDataStrcutName, arrayStrcut);
                    } else {
                        lastArrayDataStrcutName = null;
                    }
                } else {
                    if (lastArrayDataStrcutName == null) {
                        headStruct.getChildren().add(headData);
                    } else {
                        EsbDataStruct arrayStrcut = childrenEsbDataStructMap.get(lastArrayDataStrcutName);
                        if (arrayStrcut != null) {
                            arrayStrcut.getChildren().add(headData);
                        }
                    }
                }
            }
        }

        if (!bodyList.isEmpty()) {
            EsbDataStruct bodyStruct = new EsbDataStruct();
            bodyStruct.initDefaultData("BODY", null, null, null);
            dataStruct.getChildren().add(bodyStruct);
            Map<String, EsbDataStruct> childrenEsbDataStructMap = new HashMap<>();
            //用来判断节点有没有在array节点内
            String lastArrayDataStrcutName = null;
            for (EsbDataStruct bodyData : bodyList) {
                if (StringUtils.equalsIgnoreCase("array", bodyData.getType())) {
                    if (lastArrayDataStrcutName == null) {
                        lastArrayDataStrcutName = bodyData.getName();
                        EsbDataStruct arrayStrcut = new EsbDataStruct();
                        arrayStrcut.initDefaultData(bodyData.getName(), bodyData.getType(), bodyData.getContentType(), bodyData.getDescription());
                        bodyStruct.getChildren().add(arrayStrcut);
                        childrenEsbDataStructMap.put(lastArrayDataStrcutName, arrayStrcut);
                    } else {
                        lastArrayDataStrcutName = null;
                    }
                } else {
                    if (lastArrayDataStrcutName == null) {
                        bodyStruct.getChildren().add(bodyData);
                    } else {
                        EsbDataStruct arrayStrcut = childrenEsbDataStructMap.get(lastArrayDataStrcutName);
                        if (arrayStrcut != null) {
                            arrayStrcut.getChildren().add(bodyData);
                        }
                    }
                }
            }
        }
        List<EsbDataStruct> list = new ArrayList<>();
        list.add(dataStruct);
        return JSONArray.toJSONString(list);
    }

    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? "" : val;
    }

    private ESBExcelSheetInfo getEsbExcelSheetInfo(Sheet sheet) {
        String apiCodeCellName = "交易码";
        String apiNameCellName = "交易名称";
        String serviceNameCellName = "服务名称";
        String serviceScenarioCellName = "服务场景";

        String englishCellName = "英文名称";
        String chineseCellName = "中文名称";
        String dataTypeCellName = "数据类型";
        String descCellName = "备注";

        int maxContinuityEmptyCellCount = 10;

        ESBExcelSheetInfo sheetInfo = new ESBExcelSheetInfo();

        int lastRowNum = sheet.getLastRowNum();

        rowForeach:
        for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                if (rowIndex == 0) {
                    int lastCellNumber = row.getLastCellNum();

                    //连续空白数据的统计：超过连续maxContinuityEmptyCellCount个空白数据时，停止读取当前行。
                    // （某些Excel进行读取时会有缺陷：单元格没有数据，但是因为格式进行过设置，会一直读取，容易造成内存溢出）
                    int continuityEmptyCount = 0;

                    cellForeach:
                    for (int i = 0; i <= lastCellNumber; i++) {
                        Cell cell = row.getCell(i);
                        if (cell != null) {
                            String cellValue = this.getCellValue(cell).trim();
                            if (StringUtils.isEmpty(cellValue)) {
                                continuityEmptyCount++;
                                if (continuityEmptyCount > maxContinuityEmptyCellCount) {
                                    break cellForeach;
                                }
                            } else {
                                continuityEmptyCount = 0;
                                if (StringUtils.equals(cellValue, apiCodeCellName)) {
                                    sheetInfo.setSimpleCodeIndex(i);
                                } else if (StringUtils.equals(cellValue, apiNameCellName)) {
                                    sheetInfo.setSimpleNameIndex(i);
                                } else if (StringUtils.equals(cellValue, serviceNameCellName)) {
                                    sheetInfo.setServiceNameIndex(i);
                                } else if (StringUtils.equals(cellValue, serviceScenarioCellName)) {
                                    sheetInfo.setServiceScenarioIndex(i);
                                }
                            }
                        }
                    }
                } else {
                    //根据第一行《服务名称》的起始列下标开始读取， 进行匹配（英文名称、数据类型/长度、中文名称、备注）
                    int startReadIndex = sheetInfo.getApiNameIndex();
                    int lastCellNumber = row.getLastCellNum();

                    int continuityEmptyCount = 0;
                    cellForeach:
                    for (int i = startReadIndex; i < lastCellNumber; i++) {
                        Cell cell = row.getCell(i);
                        if (cell != null) {
                            String cellValue = this.getCellValue(cell).trim();
                            if (StringUtils.isEmpty(cellValue)) {
                                continuityEmptyCount++;
                                if (continuityEmptyCount > maxContinuityEmptyCellCount) {
                                    break cellForeach;
                                }
                            } else {
                                continuityEmptyCount = 0;
                                if (StringUtils.equals(cellValue, englishCellName)) {
                                    sheetInfo.setApiNameIndex(i);
                                } else if (StringUtils.equals(cellValue, chineseCellName)) {
                                    sheetInfo.setChineNameIndex(i);
                                } else if (StringUtils.contains(cellValue, dataTypeCellName)) {
                                    sheetInfo.setDataTypeIndex(i);
                                } else if (StringUtils.equals(cellValue, descCellName)) {
                                    sheetInfo.setDescIndex(i);
                                }
                            }
                        }
                    }
                    if (sheetInfo.installedApiInfoIndex()) {
                        sheetInfo.countApiPosisionIndex();
                        sheetInfo.setRequestMessageRow(rowIndex + 1);
                        break rowForeach;
                    }
                }
            }
        }
        return sheetInfo;
    }
}
