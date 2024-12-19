package io.metersphere.plan.utils;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.sdk.constants.TestPlanConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestPlanUtils {
    public static Map<String, List<String>> parseGroupIdMap(List<TestPlan> testPlanList) {
        Map<String, List<String>> testPlanGroupIdMap = new HashMap<>();
        for (TestPlan testPlan : testPlanList) {

            if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP) && !testPlanGroupIdMap.containsKey(testPlan.getId())) {
                testPlanGroupIdMap.put(testPlan.getId(), new ArrayList<>());
            } else if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
                if (testPlanGroupIdMap.containsKey(testPlan.getGroupId())) {
                    testPlanGroupIdMap.get(testPlan.getGroupId()).add(testPlan.getId());
                } else {
                    testPlanGroupIdMap.put(testPlan.getGroupId(), new ArrayList<>() {{
                        this.add(testPlan.getId());
                    }});
                }
            }
        }
        return testPlanGroupIdMap;
    }
}
