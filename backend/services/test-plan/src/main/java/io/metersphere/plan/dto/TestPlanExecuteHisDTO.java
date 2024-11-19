package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanExecuteHisDTO {

	@Schema(description = "执行结果ID")
	private String id;
	@Schema(description = "序号")
	private String num;
	@Schema(description = "执行方式")
	private String triggerMode;
	@Schema(description = "执行状态")
	private String execStatus;
	@Schema(description = "执行结果")
	private String execResult;
	@Schema(description = "操作人")
	private String operationUser;
	@Schema(description = "执行起始时间")
	private Long startTime;
	@Schema(description = "执行结束时间")
	private Long endTime;
	@Schema(description = "报告是否删除")
	private Boolean deleted;
	@Schema(description = "报告ID")
	private String reportId;
	@Schema(description = "执行结果是否删除")
	private Boolean resultDeleted;
}
