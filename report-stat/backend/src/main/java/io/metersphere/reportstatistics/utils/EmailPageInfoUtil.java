package io.metersphere.reportstatistics.utils;

import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Translator;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableDataDTO;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableItemDataDTO;
import io.metersphere.reportstatistics.dto.table.TestCaseCountTableRowDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class EmailPageInfoUtil {
    public static final String STEP_TYPE_TXT = "txt";
    public static final String STEP_TYPE_REPORT = "report";

    //数据前缀--图片
    public static final String IMG_DATA_PREFIX = "<img src=\"/resource/md/get";
    public static final String IMG_DATA_CONTAINS = "\" alt=\"";

    public static final String IMG_DATA_TYPE_JPG = "data:image/jpeg;";
    public static final String IMG_DATA_TYPE_PNG = "data:image/png;";


    public static String generateEmailStepTitle(String stepName) {
        return StringUtils.join(
                "<div style=\"background-color: #783887;font-size: 16px;color: white;margin: 5px;width: 100%; line-height:30px\">", StringUtils.LF,
                "<span style=\"margin-left: 5px\">",
                stepName, StringUtils.LF,
                "</span>", StringUtils.LF,
                "</div>"
        );
    }

    public static String generateTableInfo(Map<String, Object> reportRecordData) {
        StringBuilder tableBuffer = new StringBuilder();
        tableBuffer.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%;border: 1px\">");
        try {
            String showTableJsonStr = JSON.toJSONString(reportRecordData.get("showTable"));
            TestCaseCountTableDataDTO showTable = JSON.parseObject(showTableJsonStr, TestCaseCountTableDataDTO.class);
            tableBuffer.append("<tr style=\"font-size: 14px;font-weight: 700;color: #909399;text-align: left;\">");
            for (TestCaseCountTableItemDataDTO itemData : showTable.getHeads()) {
                String tableHeadValue = switch (itemData.getValue()) {
                    case "testCase" -> Translator.get("test_case");
                    case "apiCase" -> Translator.get("api_case");
                    case "scenarioCase" -> Translator.get("scenario_case");
                    case "performanceCase" -> Translator.get("performance_case");
                    case "creator" -> Translator.get("create_user");
                    case "casetype" -> Translator.get("test_case_type");
                    case "casestatus" -> Translator.get("test_case_status");
                    case "caselevel" -> Translator.get("test_case_priority");
                    case "Count" -> Translator.get("count");
                    default -> itemData.getValue();
                };

                tableBuffer.append("<th style=\"border: 1px solid #E8EBF3; padding: 6px 10px\">").append(tableHeadValue).append("</th>");
            }
            tableBuffer.append("</tr>");
            for (TestCaseCountTableRowDTO row : showTable.getData()) {
                tableBuffer.append("<tr style=\"font-size: 14px;font-weight: 700;color: #909399;text-align: left;\">");
                for (TestCaseCountTableItemDataDTO itemData : row.getTableDatas()) {
                    tableBuffer.append("<td style=\"border: 1px solid #E8EBF3; padding: 6px 10px\">").append(itemData.getValue()).append("</td>");
                }
                tableBuffer.append("</tr>");

            }
        } catch (Exception e) {
            LogUtil.error("解析表格数据出错!", e);
        }
        tableBuffer.append("</table>");
        return tableBuffer.toString();
    }

    public static String generatePicInfo(String reportRecordId, String imageContent) {
        return "<img  style=\"width: 100%;\" src=\"" + imageContent + EmailPageInfoUtil.IMG_DATA_CONTAINS + reportRecordId + ".jpg\" rel=\"1\" />";
    }
}
