package io.metersphere.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serial;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectRobotConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息配置机器人id")
    public String robotId;

    @Schema(description = "消息配置机器人名称")
    public String robotName;

    @Schema(description = "所属平台（飞书:LARK，钉钉:DING_TALK，企业微信:WE_COM，自定义:CUSTOM, 站内信:IN_SITE, 邮件:MAIL）")
    private String platform;

    @Schema(description = "钉钉机器人的种类: 自定义:CUSTOM, 企业内部:ENTERPRISE")
    private String dingType;

    @Schema(description = "消息配置机器人是否开启")
    public Boolean enable;

    @Schema(description = "消息配置机器人发送模版")
    public String template;

    @Schema(description = "消息配置机器人发送模版")
    public String defaultTemplate;

    @Schema(description = "消息配置机器人是否使用默认模版")
    public Boolean useDefaultTemplate;

    @Schema(description = "机器人配置的标题")
    public String subject;

    @Schema(description = "机器人配置的默认标题")
    public String defaultSubject;

    @Schema(description = "机器人是否使用默认标题")
    public Boolean useDefaultSubject;

    @Schema(description = "机器人预览标题")
    public String previewSubject;

    @Schema(description = "机器人预览模版")
    public String previewTemplate;

}
