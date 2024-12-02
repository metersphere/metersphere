package io.metersphere.dashboard.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

@Data
public class DashboardFrontPageRequest extends DashboardBaseRequest{

    @Schema(description = "是否全选")
    private boolean selectAll;

    @Schema(description = "固定时间天数")
    private Integer dayNumber;

    @Schema(description = "自定义开始时间")
    private Long startTime;

    @Schema(description = "自定义结束时间")
    private Long endTime;

    @Schema(description = "项目ID集合")
    private List<String> projectIds;

    @Schema(description = "人员集合")
    private List<String> handleUsers;

    @Schema(description = "测试计划ID")
    private String planId;


    public Long getToStartTime() {
        if (startTime == null && dayNumber>0) {
            LocalDate now = LocalDate.now();
            LocalDate localDate = now.minusDays(dayNumber);
            LocalDateTime localDateTime = LocalDateTime.of(localDate, LocalTime.MIN);//当天零点
            return localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        } else {
            return startTime;
        }
    }

    public Long getToEndTime() {
        if (endTime == null && dayNumber>0) {
            LocalDateTime now = LocalDateTime.now();
            return now.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        } else {
            return endTime;
        }
    }

}
