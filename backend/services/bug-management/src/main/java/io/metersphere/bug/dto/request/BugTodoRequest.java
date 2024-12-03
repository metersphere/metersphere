package io.metersphere.bug.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author song-cc-rock
 * 缺陷待办参数 (工作台)
 */
@Data
@Builder
public class BugTodoRequest {

	@Schema(description = "当前用户ID")
	private String msUserId;

	@Schema(description = "Local状态结束标识集合")
	private List<String> msLastStepStatus;

	@Schema(description = "当前对接平台; 非Local时需要")
	private String currentPlatform;

	@Schema(description = "当前对接平台用户ID")
	private String platformUser;

	@Schema(description = "对接平台状态结束标识集合")
	private List<String> platformLastStatus;
}
