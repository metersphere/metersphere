package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiDefinitionCaseDTO {

    @Schema(description = "接口名称")
    private String name;

    @Schema(description = "接口协议")
    private String protocol;

    @Schema(description = "http协议类型post/get/其它协议则是协议名(mqtt)")
    private String method;

    @Schema(description = "http协议路径/其它协议则为空")
    private String path;

    @Schema(description = "接口状态/进行中/已完成")
    private String status;

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

    @Schema(description = "接口用例名称")
    private String caseName;

    @Schema(description = "用例等级")
    private String priority;

    @Schema(description = "用例状态")
    private String caseStatus;

    @Schema(description = "用例最新执行结果状态")
    private String lastReportStatus;

    @Schema(description = "用例最后执行结果报告fk")
    private String lastReportId;

    @Schema(description = "用例责任人")
    private String principal;

    @Schema(description = "用例创建时间")
    private Long caseCreateTime;

    @Schema(description = "用例创建人")
    private String caseCreateUser;

    @Schema(description = "用例更新时间")
    private Long caseUpdateTime;

    @Schema(description = "用例更新人")
    private String caseUpdateUser;

    @Schema(description = "用例删除时间")
    private Long caseDeleteTime;

    @Schema(description = "用例删除人")
    private String caseDeleteUser;



}
