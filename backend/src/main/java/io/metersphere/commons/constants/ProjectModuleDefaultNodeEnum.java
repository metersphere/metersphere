package io.metersphere.commons.constants;

/**
 * 项目茶壶给默认
 * @author song-ss
 */
public enum ProjectModuleDefaultNodeEnum {

    /**
     * 测试用例默认节点
     */
    TEST_CASE_DEFAULT_NODE("未规划用例", "test_case_node"),
    /**
     * 接口默认节点
     */
    API_MODULE_DEFAULT_NODE("未规划接口", "api_module"),
    /**
     * 接口场景默认节点
     */
    API_SCENARIO_DEFAULT_NODE("未规划场景", "api_scenario_module");

    private String nodeName;
    private String tableName;

    ProjectModuleDefaultNodeEnum(String nodeName, String tableName) {
        this.nodeName = nodeName;
        this.tableName = tableName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getTableName() {
        return tableName;
    }
}
