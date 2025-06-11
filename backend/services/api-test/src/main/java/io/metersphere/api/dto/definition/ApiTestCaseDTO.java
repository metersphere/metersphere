package io.metersphere.api.dto.definition;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ApiTestCaseDTO {
    @Schema(description = "接口用例pk")
    private String id;

    @Schema(description = "接口用例名称")
    private String name;

    @Schema(description = "用例等级")
    private String priority;

    @Schema(description = "接口用例编号id")
    private Long num;

    @Schema(description = "用例状态")
    private String status;

    @Schema(description = "最新执行结果状态")
    private String lastReportStatus;

    @Schema(description = "最后执行结果报告fk")
    private String lastReportId;

    @Schema(description = "项目fk")
    private String projectId;

    @Schema(description = "接口fk")
    private String apiDefinitionId;

    @Schema(description = "接口num")
    private Long apiDefinitionNum;

    @Schema(description = "接口名称")
    private String apiDefinitionName;

    @Schema(description = "环境fk")
    private String environmentId;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "删除人")
    private String deleteUser;

    @Schema(description = "是否关注")
    private Boolean follow;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求路径")
    private String path;

    @Schema(description = "环境名称")
    private String environmentName;

    @Schema(description = "创建人")
    private String createName;

    @Schema(description = "更新人")
    private String updateName;

    @Schema(description = "删除人")
    private String deleteName;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "用例通过率")
    private String passRate = "NONE";

    @Schema(description = "请求内容")
    private AbstractMsTestElement request;

    @Schema(description = "所属模块")
    private String modulePath;

    @Schema(description = "模块id")
    private String moduleId;

    @Schema(description = "协议")
    private String protocol;

    @Schema(description = "接口定义参数变更标识")
    private Boolean apiChange;

    @Schema(description = "与接口定义不一致")
    private Boolean inconsistentWithApi;

    @Schema(description = "忽略接口与用例参数不一致")
    private Boolean ignoreApiDiff;

    @Schema(description = "忽略接口定义参数变更")
    private Boolean ignoreApiChange;

    @Schema(description = "是否是ai生成的用例")
    private Boolean aiCreate = false;
}
