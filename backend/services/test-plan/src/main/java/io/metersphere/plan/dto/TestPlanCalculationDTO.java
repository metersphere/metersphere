package io.metersphere.plan.dto;

import io.metersphere.sdk.constants.TestPlanConstants;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class TestPlanCalculationDTO {

    private List<String> statusConditionList;
    private List<String> passedConditionList;

    private List<String> passedTestPlanIds = new ArrayList<>();
    private List<String> passedItemTestPlanIds = new ArrayList<>();
    private List<String> notPassedTestPlanIds = new ArrayList<>();
    private List<String> notPassedItemTestPlanIds = new ArrayList<>();

    private List<String> completedTestPlanIds = new ArrayList<>();
    private List<String> completedItemTestPlanIds = new ArrayList<>();
    private List<String> preparedTestPlanIds = new ArrayList<>();
    private List<String> preparedItemTestPlanIds = new ArrayList<>();
    private List<String> underwayTestPlanIds = new ArrayList<>();
    private List<String> underwayItemTestPlanIds = new ArrayList<>();

    public void addPassedTestPlanId(String testPlanId) {
        if (!passedTestPlanIds.contains(testPlanId)) {
            passedTestPlanIds.add(testPlanId);
        }
    }

    public void addPassedItemTestPlanId(String testPlanId) {
        if (!passedItemTestPlanIds.contains(testPlanId)) {
            passedItemTestPlanIds.add(testPlanId);
        }
    }

    public void addNotPassedTestPlanId(String testPlanId) {
        if (!notPassedTestPlanIds.contains(testPlanId)) {
            notPassedTestPlanIds.add(testPlanId);
        }
    }

    public void addNotPassedItemTestPlanId(String testPlanId) {
        if (!notPassedItemTestPlanIds.contains(testPlanId)) {
            notPassedItemTestPlanIds.add(testPlanId);
        }
    }

    public void addCompletedTestPlanId(String testPlanId) {
        if (!completedTestPlanIds.contains(testPlanId)) {
            completedTestPlanIds.add(testPlanId);
        }
    }

    public void addCompletedItemTestPlanId(String testPlanId) {
        if (!completedItemTestPlanIds.contains(testPlanId)) {
            completedItemTestPlanIds.add(testPlanId);
        }
    }

    public void addPreparedTestPlanId(String testPlanId) {
        if (!preparedTestPlanIds.contains(testPlanId)) {
            preparedTestPlanIds.add(testPlanId);
        }
    }

    public void addPreparedItemTestPlanId(String testPlanId) {
        if (!preparedItemTestPlanIds.contains(testPlanId)) {
            preparedItemTestPlanIds.add(testPlanId);
        }
    }


    public void addUnderwayTestPlanId(String testPlanId) {
        if (!underwayTestPlanIds.contains(testPlanId)) {
            underwayTestPlanIds.add(testPlanId);
        }
    }

    public void addUnderwayItemTestPlanId(String testPlanId) {
        if (!underwayItemTestPlanIds.contains(testPlanId)) {
            underwayItemTestPlanIds.add(testPlanId);
        }
    }

    public void merge(TestPlanCalculationDTO dto) {
        passedTestPlanIds.addAll(dto.getPassedTestPlanIds());
        notPassedTestPlanIds.addAll(dto.getNotPassedTestPlanIds());
        completedTestPlanIds.addAll(dto.getCompletedTestPlanIds());
        preparedTestPlanIds.addAll(dto.getPreparedTestPlanIds());
        underwayTestPlanIds.addAll(dto.getUnderwayTestPlanIds());

        passedItemTestPlanIds.addAll(dto.getPassedItemTestPlanIds());
        notPassedItemTestPlanIds.addAll(dto.getNotPassedItemTestPlanIds());
        completedItemTestPlanIds.addAll(dto.getCompletedItemTestPlanIds());
        preparedItemTestPlanIds.addAll(dto.getPreparedItemTestPlanIds());
        underwayItemTestPlanIds.addAll(dto.getUnderwayItemTestPlanIds());
    }

    public List<String> getConditionInnerId() {
        List<String> passedConditionInnerId = new ArrayList<>();
        List<String> statusConditionInnerId = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(passedConditionList)) {
            passedConditionList.forEach(condition -> {
                if (StringUtils.equalsIgnoreCase(condition, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PASSED)) {
                    passedConditionInnerId.addAll(this.passedTestPlanIds);
                } else if (StringUtils.equalsIgnoreCase(condition, TestPlanConstants.TEST_PLAN_SHOW_STATUS_NOT_PASSED)) {
                    passedConditionInnerId.addAll(this.notPassedTestPlanIds);
                }
            });
        }
        if (CollectionUtils.isNotEmpty(statusConditionList)) {
            statusConditionList.forEach(status -> {
                if (StringUtils.equalsIgnoreCase(status, TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                    statusConditionInnerId.addAll(this.completedTestPlanIds);
                } else if (StringUtils.equalsIgnoreCase(status, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
                    statusConditionInnerId.addAll(this.preparedTestPlanIds);
                } else if (StringUtils.equalsIgnoreCase(status, TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
                    statusConditionInnerId.addAll(this.underwayTestPlanIds);
                }
            });
        }

        if (CollectionUtils.isNotEmpty(statusConditionInnerId) && CollectionUtils.isNotEmpty(passedConditionInnerId)) {
            return statusConditionInnerId.stream().filter(passedConditionInnerId::contains).toList();
        } else {
            return ListUtils.union(statusConditionInnerId, passedConditionInnerId);
        }
    }


    public List<String> getConditionItemPlanId() {
        List<String> passedConditionInnerId = new ArrayList<>();
        List<String> statusConditionInnerId = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(passedConditionList)) {
            passedConditionList.forEach(condition -> {
                if (StringUtils.equalsIgnoreCase(condition, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PASSED)) {
                    passedConditionInnerId.addAll(this.passedItemTestPlanIds);
                } else if (StringUtils.equalsIgnoreCase(condition, TestPlanConstants.TEST_PLAN_SHOW_STATUS_NOT_PASSED)) {
                    passedConditionInnerId.addAll(this.notPassedItemTestPlanIds);
                }
            });
        }
        if (CollectionUtils.isNotEmpty(statusConditionList)) {
            statusConditionList.forEach(status -> {
                if (StringUtils.equalsIgnoreCase(status, TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                    statusConditionInnerId.addAll(this.completedItemTestPlanIds);
                } else if (StringUtils.equalsIgnoreCase(status, TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
                    statusConditionInnerId.addAll(this.preparedItemTestPlanIds);
                } else if (StringUtils.equalsIgnoreCase(status, TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
                    statusConditionInnerId.addAll(this.underwayItemTestPlanIds);
                }
            });
        }

        if (CollectionUtils.isNotEmpty(statusConditionInnerId) && CollectionUtils.isNotEmpty(passedConditionInnerId)) {
            return statusConditionInnerId.stream().filter(passedConditionInnerId::contains).toList();
        } else {
            return ListUtils.union(statusConditionInnerId, passedConditionInnerId);
        }
    }
}
