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
    public static final String SOURCE_TYPE_API_DEBUG = "API_DEBUG";
    public static final String SOURCE_TYPE_API_SCENARIO= "API_SCENARIO";
    public static final String SOURCE_TYPE_API_SCENARIO_STEP = "API_SCENARIO_STEP";
    public static final String SOURCE_TYPE_API_TEST_CASE = "API_TEST_CASE";
    public static final String SOURCE_TYPE_API_DEFINITION = "API_DEFINITION";
    public static final String SOURCE_TYPE_API_DEFINITION_MOCK = "API_DEFINITION_MOCK";
    public static final Map<String, String> QUERY_SQL = new HashMap<>();

    static {
        QUERY_SQL.put(SOURCE_TYPE_BUG, "SELECT id AS sourceId, id AS redirectId, num AS sourceNum, title AS sourceName FROM bug t");
        QUERY_SQL.put(SOURCE_TYPE_FUNCTIONAL_CASE, "SELECT id AS sourceId, id AS redirectId, num AS sourceNum, name AS sourceName FROM functional_case t");
        QUERY_SQL.put(SOURCE_TYPE_API_DEBUG, "SELECT id AS sourceId, id AS redirectId, id AS sourceNum, name AS sourceName FROM api_debug t");
        QUERY_SQL.put(SOURCE_TYPE_API_SCENARIO, "SELECT id AS sourceId, id AS redirectId, num AS sourceNum, name AS sourceName FROM api_scenario t");
        QUERY_SQL.put(SOURCE_TYPE_API_SCENARIO_STEP, """
                SELECT t.id AS sourceId, scenario.id AS redirectId, scenario.num AS sourceNum, CONCAT(scenario.name, ' (', t.name, ')') AS sourceName 
                FROM api_scenario_step t JOIN api_scenario scenario ON t.scenario_id = scenario.id
                """);
        QUERY_SQL.put(SOURCE_TYPE_API_TEST_CASE, "SELECT id AS sourceId, id AS redirectId, num AS sourceNum, name AS sourceName FROM api_test_case t");
        QUERY_SQL.put(SOURCE_TYPE_API_DEFINITION, "SELECT id AS sourceId, id AS redirectId, num AS sourceNum, name AS sourceName FROM api_definition t");
        QUERY_SQL.put(SOURCE_TYPE_API_DEFINITION_MOCK, "SELECT id AS sourceId, id AS redirectId, expect_num AS sourceNum, name AS sourceName FROM api_definition_mock t");
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
