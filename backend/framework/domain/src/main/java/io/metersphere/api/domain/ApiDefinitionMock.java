package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDefinitionMock implements Serializable {
    @Schema(description =  "mock pk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{api_definition_mock.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "接口路径")
    private String apiPath;

    @Schema(description =  "接口类型")
    private String apiMethod;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "修改时间")
    private Long updateTime;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "mock 名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 200, message = "{api_definition_mock.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "自定义标签")
    private String tags;

    @Schema(description =  "启用/禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{api_definition_mock.enable.not_blank}", groups = {Created.class})
    private Boolean enable;

    @Schema(description =  "mock编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.expect_num.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition_mock.expect_num.length_range}", groups = {Created.class, Updated.class})
    private String expectNum;

    @Schema(description =  "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition_mock.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "接口fk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{api_definition_mock.api_definition_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{api_definition_mock.api_definition_id.length_range}", groups = {Created.class, Updated.class})
    private String apiDefinitionId;

    private static final long serialVersionUID = 1L;
}