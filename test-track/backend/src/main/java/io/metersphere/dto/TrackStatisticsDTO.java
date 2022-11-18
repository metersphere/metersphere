package io.metersphere.dto;

import io.metersphere.excel.converter.TestReviewCaseStatus;
import io.metersphere.i18n.Translator;
import io.metersphere.request.testcase.TrackCount;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private String p0CountStr = StringUtils.EMPTY;
    private String p1CountStr = StringUtils.EMPTY;
    private String p2CountStr = StringUtils.EMPTY;
    private String p3CountStr = StringUtils.EMPTY;

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


    private String apiCaseCountStr = StringUtils.EMPTY;
    private String scenarioCaseStr = StringUtils.EMPTY;
    private String performanceCaseCountStr = StringUtils.EMPTY;

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
     * 评审通过率
     */
    private String reviewPassRage = " 0%";
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
     * 面板数据
     */
    private Map<String, Integer> chartData;


    /**
     * 按照 Priority 统计
     * @param trackCountResults 统计结果
     */
    public void countPriority(List<TrackCountResult> trackCountResults) {
        Map<String, Integer> chartData = new HashMap<>();
        for (TrackCountResult countResult : trackCountResults) {
            if (StringUtils.isNotBlank(countResult.getGroupField())) {
                Integer count = chartData.get(countResult.getGroupField());
                if (count == null) {
                    chartData.put(countResult.getGroupField(), (int) countResult.getCountNumber());
                } else {
                    count += (int) countResult.getCountNumber();
                    chartData.put(countResult.getGroupField(), count);
                }
            }
            this.allCaseCountNumber += countResult.getCountNumber();
        }
        this.chartData = chartData;
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
        Map<String, Integer> chartData = new HashMap<>();
        for (TrackCountResult countResult : relevanceResults) {
            if (StringUtils.equalsIgnoreCase(TrackCount.TESTCASE, countResult.getGroupField())) {
                Integer count = chartData.get(Translator.get("api_case"));
                if (count == null) {
                    chartData.put(Translator.get("api_case"), (int) countResult.getCountNumber());
                } else {
                    count += (int) countResult.getCountNumber();
                    chartData.put(Translator.get("api_case"), count);
                }
            }
            if (StringUtils.equalsIgnoreCase(TrackCount.PERFORMANCE, countResult.getGroupField())) {
                Integer count = chartData.get(Translator.get("performance_case"));
                if (count == null) {
                    chartData.put(Translator.get("performance_case"), (int) countResult.getCountNumber());
                } else {
                    count += (int) countResult.getCountNumber();
                    chartData.put(Translator.get("performance_case"), count);
                }
            }
            if (StringUtils.equalsIgnoreCase(TrackCount.AUTOMATION, countResult.getGroupField())) {
                Integer count = chartData.get(Translator.get("scenario_case"));
                if (count == null) {
                    chartData.put(Translator.get("scenario_case"), (int) countResult.getCountNumber());
                } else {
                    count += (int) countResult.getCountNumber();
                    chartData.put(Translator.get("scenario_case"), count);
                }
            }
            this.allRelevanceCaseCount += countResult.getCountNumber();
        }
        this.chartData = chartData;
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
