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
    private long newCount;
    private long resolvedCount;
    private long rejectedCount;
    private long unKnownCount;
    private long thisWeekCount;
    private String unClosedRage;
    private String bugCaseRage;
    private List<TestPlanBugCount> list = new ArrayList<>();
}
