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

@Schema(title = "mock 配置")
@Table("api_definition_mock")
@Data
public class ApiDefinitionMock implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{api_definition_mock.id.not_blank}", groups = {Updated.class})
    @Schema(title = "mock pk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Schema(title = "接口路径", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 500]")
    private String apiPath;

    @Schema(title = "接口类型", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 50]")
    private String apiMethod;

    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Schema(title = "修改时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_definition_mock.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_mock.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String createUser;

    @Size(min = 1, max = 200, message = "{api_definition_mock.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_mock.name.not_blank}", groups = {Created.class})
    @Schema(title = "mock 名称", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 200]")
    private String name;

    @Schema(title = "自定义标签", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = "range[1, 500]")
    private String tags;

    @Schema(title = "启用/禁用", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean enable;

    @Size(min = 1, max = 50, message = "{api_definition_mock.expect_num.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_mock.expect_num.not_blank}", groups = {Created.class})
    @Schema(title = "mock编号", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String expectNum;

    @Size(min = 1, max = 50, message = "{api_definition_mock.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_mock.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{api_definition_mock.api_definition_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_mock.api_definition_id.not_blank}", groups = {Created.class})
    @Schema(title = "接口fk", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String apiDefinitionId;


}