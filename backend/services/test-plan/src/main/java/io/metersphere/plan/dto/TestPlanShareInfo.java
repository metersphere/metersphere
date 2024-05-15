package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanShareInfo {

	@Schema(description = "分享ID")
	private String id;

	@Schema(description = "分享链接")
	private String shareUrl;
}
