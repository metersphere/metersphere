package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "误报库")
@Table("api_fake_error_config")
@Data
public class ApiFakeErrorConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_fake_error_config.id.not_blank}", groups = {Updated.class})
    @Schema(title = "误报pk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Schema(title = "修改时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_fake_error_config.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_fake_error_config.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;

    @Size(min = 1, max = 50, message = "{api_fake_error_config.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_fake_error_config.update_user.not_blank}", groups = {Created.class})
    @Schema(title = "修改人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String updateUser;

    @Size(min = 1, max = 255, message = "{api_fake_error_config.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_fake_error_config.name.not_blank}", groups = {Created.class})
    @Schema(title = "误报名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(min = 1, max = 255, message = "{api_fake_error_config.match_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_fake_error_config.match_type.not_blank}", groups = {Created.class})
    @Schema(title = "匹配类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String matchType;

    @Schema(title = "状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean status;

    @Size(min = 1, max = 50, message = "{api_fake_error_config.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_fake_error_config.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(title = "描述信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(title = "误报内容", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] content;

}