package io.metersphere.commons.constants;

/**
 * 项目模块默认节点
 * @author song-cc
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
    API_SCENARIO_DEFAULT_NODE("未规划场景", "api_scenario_module"),
    /**
     * UI自动化默认节点
     */
    UI_SCENARIO_DEFAULT_NODE("未规划场景", "ui_scenario_module"),
    /**
     * UI元素库默认节点
     */
    UI_ELEMENT_DEFAULT_NODE("未规划元素", "ui_element_module");

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
