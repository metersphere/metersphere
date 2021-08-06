package io.metersphere.api.dto.datacount.response;

import io.metersphere.track.dto.TestPlanDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 已执行的案例
 */
@Getter
@Setter
public class ExecutedCaseInfoDTO {
    //排名
    private int sortIndex;
    //案例名称
    private String caseName;
    //所属测试计划
    private String testPlan;
    //失败次数
    private Long failureTimes;
    //案例类型
    private String caseType;
    //案例ID -- 目前被用为案例-测试计划 关联表ID
    private String caseID;
    //ID
    private String id;
    //测试计划集合
    private List<TestPlanDTO> testPlanDTOList;
}
