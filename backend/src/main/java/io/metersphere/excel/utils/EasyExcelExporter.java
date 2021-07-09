package io.metersphere.excel.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.exception.ExcelException;
import org.apache.commons.collections.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EasyExcelExporter {

    private Class clazz;

    public EasyExcelExporter(Class clazz) {
        this.clazz = clazz;
    }

    public void export(HttpServletResponse response, List data, String fileName, String sheetName) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        contentWriteCellStyle.setWrapped(true);
        try {
            HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(null, contentWriteCellStyle);
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
            EasyExcel.write(response.getOutputStream(), this.clazz).registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(data);
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("Utf-8 encoding is not supported");
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("IO exception");
        }
    }

    public void exportByCustomWriteHandler(HttpServletResponse response, Set<String> excludeColumnFiledNames,  List data, String fileName, String sheetName, WriteHandler writeHandler) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        List<List<String>> list = new ArrayList<>();
        List<String>aaaList = new ArrayList<>();
        aaaList.add("aaaaa");
        List<String>nameList = new ArrayList<>();
        nameList.add("所属模块");
        List<String>name2List = new ArrayList<>();
        name2List.add("用例名称");
        list.add(aaaList);
        list.add(nameList);
        list.add(name2List);
        try {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
            if(CollectionUtils.isNotEmpty(excludeColumnFiledNames)){
                EasyExcel.write(response.getOutputStream()).head(list).
                        registerWriteHandler(writeHandler).
//                        excludeColumnFiledNames(excludeColumnFiledNames).
//                        includeColumnFiledNames(list).
                        sheet(sheetName).doWrite(data);
            }else {
                EasyExcel.write(response.getOutputStream(), this.clazz).registerWriteHandler(writeHandler).sheet(sheetName).doWrite(data);
            }
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("Utf-8 encoding is not supported");
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("IO exception");
        }
    }

    public void exportByCustomWriteHandler(HttpServletResponse response, List<List<String>> headList,  List<List<Object>> data, String fileName, String sheetName, WriteHandler writeHandler) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        try {
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
            EasyExcel.write(response.getOutputStream()).head(headList).registerWriteHandler(writeHandler).
                sheet(sheetName).doWrite(data);
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("Utf-8 encoding is not supported");
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            throw new ExcelException("IO exception");
        }
    }

}
