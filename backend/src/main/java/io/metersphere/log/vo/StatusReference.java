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

    }

}
