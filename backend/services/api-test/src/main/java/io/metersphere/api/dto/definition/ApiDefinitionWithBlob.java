package io.metersphere.api.dto.definition;

import io.metersphere.api.domain.ApiDefinitionBlob;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class ApiDefinitionWithBlob extends ApiDefinitionBlob {

    @Schema(description = "接口pk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "接口协议", requiredMode = Schema.RequiredMode.REQUIRED)
    private String protocol;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    private String method;

    @Schema(description = "http协议路径/其它协议则为空")
    private String path;

    @Schema(description = "接口状态/进行中/已完成", requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;

    @Schema(description = "自定义id")
    private Long num;

    @Schema(description = "标签")
    private java.util.List<String> tags;

    @Schema(description = "自定义排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pos;

    @Schema(description = "项目fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "模块fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleId;

    @Schema(description = "是否为最新版本 0:否，1:是", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean latest;

    @Schema(description = "版本fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String versionId;

    @Schema(description = "版本引用fk", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refId;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "修改人")
    private String updateUser;

    @Schema(description = "删除人")
    private String deleteUser;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "删除状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean deleted;

}
