package io.metersphere.log.vo.reportstatistics;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnterpriseTestReportReference {
    public static Map<String, String> enterpriseTestReportColumns = new LinkedHashMap<>();

    static {
        enterpriseTestReportColumns.clear();
        enterpriseTestReportColumns.put("name", "报告名称");
        enterpriseTestReportColumns.put("createUser", "创建人");
        enterpriseTestReportColumns.put("updateUser", "修改人");
        enterpriseTestReportColumns.put("status", "报告状态");
    }
}