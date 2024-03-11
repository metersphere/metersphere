package io.metersphere.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ReferenceDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private String id;
    @Schema(description = "ID")
    private Long num;
    @Schema(description = "资源名称")
    private String resourceName;
    @Schema(description = "资源类型 API 接口测试  UI UI测试  TEST_PLAN 测试计划")
    private String resourceType;
    @Schema(description = "引用类型 COPY/REF")
    private String refType;
    @Schema(description = "组织ID")
    private String organizationId;
    @Schema(description = "项目ID")
    private String projectId;
    @Schema(description = "所属组织")
    private String organizationName;
    @Schema(description = "所属项目")
    private String projectName;


}
