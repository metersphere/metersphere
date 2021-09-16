package io.metersphere.performance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitorItem {
    private String value; // 表达式
    private String name; // 监控项
}
