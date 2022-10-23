package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BugStatistics {

    private long bugTotalSize;
    private String rage;
    private List<TestPlanBugCount> list = new ArrayList<>();
}
