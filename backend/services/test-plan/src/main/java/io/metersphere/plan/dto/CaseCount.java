package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能, 接口, 场景
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseCount {

	@Schema(description = "成功用例数量")
	private Integer success = 0;
	@Schema(description = "失败用例数量")
	private Integer error = 0;
	@Schema(description = "误报用例数量")
	private Integer fakeError = 0;
	@Schema(description = "阻塞用例数量")
	private Integer block = 0;
	@Schema(description = "未执行用例数量")
	private Integer pending = 0;
}
