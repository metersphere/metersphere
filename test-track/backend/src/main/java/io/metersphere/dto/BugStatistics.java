package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BugStatistics {

    private long bugUnclosedCount;
    private long bugTotalCount;
    private long caseTotalCount;
    private long unClosedP0Size;
    private long unClosedP1Size;
    private long unClosedP2Size;
    private long unClosedP3Size;
    private String unClosedRage;
    private String bugCaseRage;
    private List<TestPlanBugCount> list = new ArrayList<>();
}
