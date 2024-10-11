package io.metersphere.system.dto.taskhub.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.metersphere.system.serializer.CustomRateSerializer;
import io.metersphere.system.utils.RateCalculateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TaskStatisticsResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务id")
    private String id;

    @Schema(description = "任务完成率{已执行用例/全部用例}")
    @JsonSerialize(using = CustomRateSerializer.class)
    private Double executeRate;


    @Schema(description = "成功用例数量")
    private long successCount = 0;
    @Schema(description = "失败用例数量")
    private long errorCount = 0;
    @Schema(description = "误报用例数量")
    private long fakeErrorCount = 0;
    @Schema(description = "未执行用例数量")
    private long pendingCount = 0;

    @Schema(description = "用例总数")
    private long caseTotal = 0;


    public void calculateExecuteRate() {
        this.executeRate = RateCalculateUtils.divWithPrecision(this.caseTotal - this.pendingCount, this.caseTotal, 2);
    }
}
