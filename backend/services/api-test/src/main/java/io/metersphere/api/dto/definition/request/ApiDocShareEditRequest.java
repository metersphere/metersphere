package io.metersphere.api.dto.definition.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * @author song-cc-rock
 */

@Data
public class ApiDocShareEditRequest implements Serializable {

	@Schema(title = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{api_doc_share.id.not_blank}", groups = {Updated.class})
	@Size(min = 1, max = 50, message = "{api_doc_share.id.length_range}", groups = {Updated.class})
	private String id;

	@Schema(description = "分享名称", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{api_doc_share.name.not_blank}")
	@Size(min = 1, max = 255, message = "{api_doc_share.name.length_range}")
	private String name;

	@Schema(title = "接口范围;全部接口(ALL)、模块(MODULE)、路径(PATH)、标签(TAG)", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{api_doc_share.api_range.not_blank}", groups = {Created.class})
	@Size(min = 1, max = 10, message = "{api_doc_share.api_range.length_range}", groups = {Created.class, Updated.class})
	private String apiRange;

	@Schema(title = "范围匹配符;包含(CONTAINS)、等于(EQUALS)")
	private String rangeMatchSymbol;

	@Schema(title = "范围匹配值;eg: 选中路径范围时, 该值作为路径匹配")
	private String rangeMatchVal;

	@Schema(title = "截止时间值")
	private Long invalidTime;

	@Schema(title = "失效时间单位;小时(HOUR)、天(DAY)、月(MONTH)、年(YEAR)")
	private String invalidUnit;

	@Schema(title = "是否私有;0: 公开、1: 私有", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "{api_doc_share.is_private.not_blank}", groups = {Created.class})
	private Boolean isPrivate;

	@Schema(title = "访问密码;私有时需要访问密码")
	private String password;

	@Schema(title = "允许导出;0: 不允许、1: 允许", requiredMode = Schema.RequiredMode.REQUIRED)
	private Boolean allowExport;

	@Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "{api_doc_share.project_id.not_blank}", groups = {Created.class})
	@Size(min = 1, max = 50, message = "{api_doc_share.project_id.length_range}", groups = {Created.class, Updated.class})
	private String projectId;
}
