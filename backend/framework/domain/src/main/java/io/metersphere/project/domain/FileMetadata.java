package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class FileMetadata implements Serializable {
    @Schema(title = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{file_metadata.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{file_metadata.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "文件类型")
    private String type;

    @Schema(title = "文件大小", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{file_metadata.size.not_blank}", groups = {Created.class})
    private Long size;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "文件存储方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.storage.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata.storage.length_range}", groups = {Created.class, Updated.class})
    private String storage;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "修改人")
    private String updateUser;

    @Schema(title = "标签")
    private String tags;

    @Schema(title = "描述")
    private String description;

    @Schema(title = "文件所属模块")
    private String moduleId;

    @Schema(title = "是否加载jar（开启后用于接口测试执行时使用）")
    private Boolean loadJar;

    @Schema(title = "文件存储路径")
    private String path;

    @Schema(title = "资源作用范围，主要兼容2.1版本前的历史数据，后续版本不再产生数据")
    private String resourceType;

    @Schema(title = "是否是最新版", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{file_metadata.latest.not_blank}", groups = {Created.class})
    private Boolean latest;

    @Schema(title = "同版本数据关联的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_metadata.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_metadata.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    private static final long serialVersionUID = 1L;
}