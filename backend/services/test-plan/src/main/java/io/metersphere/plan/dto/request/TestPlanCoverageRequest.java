package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Data
public class TestPlanCoverageRequest {

    @Schema(description = "固定时间天数")
    private int dayNumber = 0;

    @Schema(description = "自定义开始时间")
    private long startTime = 0;

    @Schema(description = "自定义结束时间")
    private long endTime = 0;

    @Schema(description = "项目ID")
    @NotBlank
    private String projectId;


    public long getStartTime() {
        if (startTime == 0 && dayNumber > 0) {
            LocalDate now = LocalDate.now();
            LocalDate localDate = now.minusDays(dayNumber);
            LocalDateTime localDateTime = LocalDateTime.of(localDate, LocalTime.MIN);//当天零点
            return localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        } else {
            return startTime;
        }
    }

    public long getEndTime() {
        if (endTime == 0 && dayNumber > 0) {
            LocalDateTime now = LocalDateTime.now();
            return now.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        } else {
            return endTime;
        }
    }

}
