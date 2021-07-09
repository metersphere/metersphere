package io.metersphere.log.vo;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatusReference {
    public static Map<String, String> statusMap = new LinkedHashMap<>();

    static {
        statusMap.clear();
        statusMap.put("Prepare", "未开始");
        statusMap.put("Underway", "进行中");
        statusMap.put("Completed", "已完成");
        statusMap.put("Finished", "已结束");
        statusMap.put("Saved", "已保存");
        statusMap.put("Starting", "已开始");
        statusMap.put("Running", "运行中");
        statusMap.put("Error", "异常");
        statusMap.put("Pass", "通过");
        statusMap.put("UnPass", "未通过");
        statusMap.put("smoke", "冒烟测试");
        statusMap.put("system", "系统测试");
        statusMap.put("regression", "回归测试");
        statusMap.put("TEST_CASE", "用例模版");
        statusMap.put("ISSUE", "缺陷模版");
        statusMap.put("input", "输入框");
        statusMap.put("textarea", "文本框");
        statusMap.put("select", "单选下拉列表");
        statusMap.put("multipleSelect", "多选下拉列表");
        statusMap.put("radio", "单选框");
        statusMap.put("checkbox", "多选框");
        statusMap.put("member", "单选成员");
        statusMap.put("multipleMember", "多选成员");
        statusMap.put("data", "日期");
        statusMap.put("int", "整型");
        statusMap.put("float", "浮点型");

        statusMap.put("EMAIL", "邮件");
        statusMap.put("EXECUTE_SUCCESSFUL", "执行成功");
        statusMap.put("EXECUTE_FAILED", "执行失败");
        statusMap.put("JENKINS_TASK", "Jenkins接口调用任务通知");
        statusMap.put("DEFECT_TASK", "缺陷任务通知");
        statusMap.put("CREATE", "创建");
        statusMap.put("UPDATE", "修改");
        statusMap.put("DELETE", "删除");
        statusMap.put("false", "否");
        statusMap.put("true", "是");
        statusMap.put("functional", "功能用例");
        statusMap.put("performance", "性能用例");
        statusMap.put("api", "接口用例");

    }

}
