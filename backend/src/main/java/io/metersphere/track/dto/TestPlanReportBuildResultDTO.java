package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanReportBuildResultDTO {
    private TestPlanSimpleReportDTO testPlanSimpleReportDTO;
    /**
     * 判断testPlanReportContent中，APIBaseInfo字段是否改变。
     * 如果改变过，则需要更新testPlanReportContent数据
     */
    private boolean apiBaseInfoChanged = false;
}
