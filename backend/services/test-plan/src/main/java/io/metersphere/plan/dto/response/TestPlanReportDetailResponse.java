package io.metersphere.plan.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.metersphere.plan.dto.CaseCount;
import io.metersphere.system.serializer.CustomRateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Data
public class TestPlanReportDetailResponse {

    /**
     * 基础报告详情信息
     */
    @Schema(description = "报告ID")
    private String id;
    @Schema(description = "报告名称")
    private String name;
    @Schema(description = "计划名称")
    private String testPlanName;
    @Schema(description = "报告创建时间")
    private Long createTime;
    @Schema(description = "报告开始(执行)时间")
    private Long startTime;
    @Schema(description = "报告结束(执行)时间")
    private Long endTime;
    @Schema(description = "报告内容")
    private String summary;
    @Schema(description = "用例总数")
    private Integer caseTotal;
    @Schema(description = "功能用例总数")
    private Integer functionalTotal;
    @Schema(description = "接口用例总数")
    private Integer apiCaseTotal;
    @Schema(description = "接口场景用例总数")
    private Integer apiScenarioTotal;

    /**
     * 报告分析
     */
    @Schema(description = "通过阈值")
    @JsonSerialize(using = CustomRateSerializer.class)
    private Double passThreshold;
    @Schema(description = "通过率")
    @JsonSerialize(using = CustomRateSerializer.class)
    private Double passRate;
    @Schema(description = "执行完成率")
    @JsonSerialize(using = CustomRateSerializer.class)
    private Double executeRate;
    @Schema(description = "缺陷总数")
    private Integer bugCount;
    @Schema(description = "计划总数")
    private Integer planCount;

    /**
     * 执行分析面板
     */
    @Schema(description = "执行分析-用例数")
    private CaseCount executeCount;
    @Schema(description = "功能用例分析-用例数")
    private CaseCount functionalCount;
    @Schema(description = "接口用例分析-用例数")
    private CaseCount apiCaseCount;
    @Schema(description = "接口场景用例分析-用例数")
    private CaseCount apiScenarioCount;

    /**
     * 一键总结参数
     */
    @Schema(description = "通过的计划数量 - 计划组报告使用")
    private Integer passCountOfPlan = 0;
    @Schema(description = "未通过的计划数量 - 计划组报告使用")
    private Integer failCountOfPlan = 0;
    @Schema(description = "功能用例明细的缺陷数量")
    private Integer functionalBugCount = 0;
    @Schema(description = "接口用例明细的缺陷数量")
    private Integer apiBugCount = 0;
    @Schema(description = "场景用例明细的缺陷数量")
    private Integer scenarioBugCount = 0;

    /**
     * 报告明细列表 参数
     */
    @Schema(description = "报告是否删除")
    private boolean deleted;
    @Schema(description = "报告状态")
    private String resultStatus;

    /**
     * 报告布局
     */
    @Schema(description = "报告是否默认布局")
    private Boolean defaultLayout;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Schema(description = "计划组子计划数据信息")
    private List<TestPlanReportDetailResponse> children;
}
