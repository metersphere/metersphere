package io.metersphere.excel.constants;

import io.metersphere.commons.utils.DateUtils;
import io.metersphere.excel.domain.IssueExcelData;
import io.metersphere.i18n.Translator;

import java.util.function.Function;

/**
 * @author songcc
 * 导出缺陷HEAD枚举
 */

public enum IssueExportHeadField {

    ID("id", "ID", issueExcelData -> issueExcelData.getNum().toString()), TITLE("title", Translator.get("title"), IssueExcelData::getTitle),
    CREATOR("creator", Translator.get("create_user"), IssueExcelData::getCreator),
    DESCRIPTION("description", Translator.get("description"), IssueExcelData::getDescription),
    CASE_COUNT("caseCount", Translator.get("case_count"), issueExcelData -> String.valueOf(issueExcelData.getCaseCount())),
    COMMENT("comment", Translator.get("comment"), IssueExcelData::getComment),
    RESOURCE("resource", Translator.get("issue_resource"), IssueExcelData::getResourceName),
    PLATFORM("platform", Translator.get("issue_platform"), IssueExcelData::getPlatform),
    PLATFORM_STATUS("platformStatus", Translator.get("platform_status"), IssueExcelData::getPlatformStatus),
    CREATE_TIME("createTime", Translator.get("create_time"), issueExcelData -> issueExcelData.getCreateTime() != null ? DateUtils.getTimeStr(issueExcelData.getCreateTime()) : null);

    private String id;
    private String name;
    private Function<IssueExcelData, String> parseFunc;

    IssueExportHeadField(String id, String name, Function<IssueExcelData, String> parseFunc) {
        this.id = id;
        this.name = name;
        this.parseFunc = parseFunc;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String parseExcelDataValue(IssueExcelData excelData) {
        return parseFunc.apply(excelData);
    }
}
