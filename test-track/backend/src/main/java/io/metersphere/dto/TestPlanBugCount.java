package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanBugCount {
    private int index;
    private String planName;
    private long createTime;
    private String status;
    private int caseSize;
    private int bugSize;
    private String passRage;
    private String planId;
}
