package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author song-cc-rock
 */
@Data
public class BatchExecTaskReportDTO {

	@Schema(description = "报告ID")
	private String id;
	@Schema(description = "报告来源 {API, SCENARIO}")
	private String source;
	@Schema(description = "是否是集成报告")
	private Boolean integrated;
	@Schema(description = "报告名称")
	private String name;
	@Schema(description = "报告状态")
	private String status;
	@Schema(description = "执行结果")
	private String execResult;
	@Schema(description = "触发方式")
	private String triggerMode;
	@Schema(description = "创建人")
	private String createUser;
	@Schema(description = "创建人名称")
	private String createUserName;
	@Schema(description = "创建时间")
	private Long createTime;
}
