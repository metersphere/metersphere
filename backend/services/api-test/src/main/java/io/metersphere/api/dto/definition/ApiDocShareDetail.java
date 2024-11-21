package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author song-cc-rock
 */
@Data
@Builder
public class ApiDocShareDetail implements Serializable {

	@Schema(title = "是否失效")
	private Boolean invalid;
	@Schema(title = "是否公开")
	private Boolean isPrivate;
	@Schema(title = "是否允许导出")
	private Boolean allowExport;
	@Schema(title = "项目名称")
	private String projectName;
}
