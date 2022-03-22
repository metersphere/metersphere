package io.metersphere.track.response;

import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.commons.constants.TestReviewCaseStatus;
import io.metersphere.track.request.testcase.TrackCount;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 用例数量统计数据
 */
@Getter
@Setter
public class TrackStatisticsDTO {

    /**
     * 用例总计
     */
    private long allCaseCountNumber = 0;

    /**
     * P0登记用例总计
     */
    private long p0CaseCountNumber = 0;

    /**
     * P1登记用例总计
     */
    private long p1CaseCountNumber = 0;

    /**
     * P2登记用例总计
     */
    private long p2CaseCountNumber = 0;

    /**
     * P3登记用例总计
     */
    private long p3CaseCountNumber = 0;

    private String p0CountStr = "";
    private String p1CountStr = "";
    private String p2CountStr = "";
    private String p3CountStr = "";

    /**
     * 关联用例数量统计
     */
    private long allRelevanceCaseCount = 0;

    /**
     * 接口用例数量统计
     */
    private long apiCaseCount = 0;

    /**
     * 场景用例数量统计
     */
    private long scenarioCaseCount = 0;

    /**
     * 性能用例数量统计
     */
    private long performanceCaseCount = 0;


    private String apiCaseCountStr = "";
    private String scenarioCaseStr = "";
    private String performanceCaseCountStr = "";

    /**
     * 本周新增数量
     */
    private long thisWeekAddedCount = 0;
    /**
     * 未覆盖
     */
    private long uncoverageCount = 0;
    /**
     * 已覆盖
     */
    private long coverageCount = 0;
    /**
     * 覆盖率
     */
    private String coverageRage = " 0%";
    /**
     * 评审率
     */
    private String reviewRage = " 0%";
    /**
     * 未评审
     */
    private long prepareCount = 0;
    /**
     * 已通过
     */
    private long passCount = 0;
    /**
     * 未通过
     */
    private long unPassCount = 0;


    /**
     * 按照 Priority 统计
     * @param trackCountResults 统计结果
     */
    public void countPriority(List<TrackCountResult> trackCountResults) {
        for (TrackCountResult countResult : trackCountResults) {
            if (StringUtils.isNotBlank(countResult.getGroupField())) {
                switch (countResult.getGroupField().toUpperCase()){
                    case TrackCount.P0:
                        this.p0CaseCountNumber += countResult.getCountNumber();
                        break;
                    case TrackCount.P1:
                        this.p1CaseCountNumber += countResult.getCountNumber();
                        break;
                    case TrackCount.P2:
                        this.p2CaseCountNumber += countResult.getCountNumber();
                        break;
                    case TrackCount.P3:
                        this.p3CaseCountNumber += countResult.getCountNumber();
                        break;
                    default:
                        break;
                }
            }
            this.allCaseCountNumber += countResult.getCountNumber();
        }
    }

    public void countStatus(List<TrackCountResult> statusResults) {
        for (TrackCountResult countResult : statusResults) {
            if(TestReviewCaseStatus.Prepare.name().equals(countResult.getGroupField())){
                this.prepareCount += countResult.getCountNumber();
            }else if(TestReviewCaseStatus.Pass.name().equals(countResult.getGroupField())){
                this.passCount += countResult.getCountNumber();
            }else if(TestReviewCaseStatus.UnPass.name().equals(countResult.getGroupField())){
                this.unPassCount += countResult.getCountNumber();
            }
        }
    }

    public void countRelevance(List<TrackCountResult> relevanceResults) {
        for (TrackCountResult countResult : relevanceResults) {
            switch (countResult.getGroupField()){
                case TrackCount.TESTCASE:
                    this.apiCaseCount += countResult.getCountNumber();
                    this.allRelevanceCaseCount += countResult.getCountNumber();
                    break;
                case TrackCount.PERFORMANCE:
                    this.performanceCaseCount += countResult.getCountNumber();
                    this.allRelevanceCaseCount += countResult.getCountNumber();
                    break;
                case TrackCount.AUTOMATION:
                    this.scenarioCaseCount += countResult.getCountNumber();
                    this.allRelevanceCaseCount += countResult.getCountNumber();
                    break;
                default:
                    break;
            }
        }
    }

    public void countCoverage(List<TrackCountResult> coverageResults) {
        for (TrackCountResult countResult : coverageResults) {
            if("coverage".equals(countResult.getGroupField())){
                this.coverageCount+= countResult.getCountNumber();
            }else if("uncoverage".equals(countResult.getGroupField())){
                this.uncoverageCount+= countResult.getCountNumber();
            }
        }
    }
}
