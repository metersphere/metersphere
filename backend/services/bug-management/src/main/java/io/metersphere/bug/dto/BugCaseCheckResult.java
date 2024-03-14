package io.metersphere.bug.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BugCaseCheckResult implements Serializable {

	@Schema(description = "校验是否通过")
	private Boolean pass;

	@Schema(description = "未通过信息")
	private String msg;
}
