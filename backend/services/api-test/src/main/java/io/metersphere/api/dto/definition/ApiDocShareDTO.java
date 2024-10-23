package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiDocShare;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiDocShareDTO extends ApiDocShare {

	@Schema(title = "分享是否失效")
	private Boolean invalid;

	@Schema(title = "分享接口数量")
	private Integer apiShareNum;

	@Schema(title = "创建人")
	private String createUserName;

	@Schema(title = "更新人")
	private String updateUserName;
}
