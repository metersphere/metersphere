package io.metersphere.log.vo.errorreportlibrary;

import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorReportLibraryReference {
    public static Map<String, String> errorReportLibraryColumns = new LinkedHashMap<>();

    static {
        errorReportLibraryColumns.clear();
        errorReportLibraryColumns.put("errorCode", "错误码");
        errorReportLibraryColumns.put("matchType", "匹配规则");
        errorReportLibraryColumns.put("createUser", "创建人");
        errorReportLibraryColumns.put("updateUser", "修改人");
        errorReportLibraryColumns.put("status", "启用状态");
        errorReportLibraryColumns.put("description", "描述");
        errorReportLibraryColumns.put("content", "断言规则");
    }
}