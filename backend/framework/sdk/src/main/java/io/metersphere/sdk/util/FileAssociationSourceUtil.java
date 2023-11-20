package io.metersphere.sdk.util;

import io.metersphere.sdk.exception.MSException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件关联资源类型
 * 注：类型如果要拓展，QUERY_SQL 中要增加对应的数据库名称查询SQL （不需要拼接where条件）
 */
public class FileAssociationSourceUtil {
    public static final String SOURCE_TYPE_BUG = "BUG";
    public static final String SOURCE_TYPE_FUNCTIONAL_CASE = "FUNCTIONAL_CASE";
    public static final Map<String, String> QUERY_SQL = new HashMap<>();

    static {
        QUERY_SQL.put(SOURCE_TYPE_BUG, "SELECT id AS sourceId,title AS sourceName FROM bug");
        QUERY_SQL.put(SOURCE_TYPE_FUNCTIONAL_CASE, "SELECT id AS sourceId,name AS sourceName FROM functional_case");
    }

    public static void validate(String type) {
        if (!QUERY_SQL.containsKey(type)) {
            throw new MSException(Translator.get("file.association.error.type"));
        }
    }
    public static String getQuerySql(String type) {
        validate(type);
        return QUERY_SQL.get(type) + StringUtils.SPACE;
    }
}
