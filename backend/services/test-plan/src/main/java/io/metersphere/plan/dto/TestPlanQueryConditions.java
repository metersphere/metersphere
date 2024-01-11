package io.metersphere.plan.dto;

import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.system.dto.sdk.BaseCondition;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试计划条件查询
 */
@Data
@NoArgsConstructor
public class TestPlanQueryConditions {
    //模块ID
    private List<String> moduleIds;

    //项目ID
    private String projectId;

    //测试计划所属GroupId
    private String groupId = TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID;

    //查询条件
    private BaseCondition condition = new BaseCondition();

    //隐藏的测试计划ID
    public List<String> hiddenIds = new ArrayList<>();

    public TestPlanQueryConditions(List<String > moduleIds,String projectId,BaseCondition condition){
        this.moduleIds = moduleIds;
        this.projectId = projectId;
        this.condition = condition;
    }
}
