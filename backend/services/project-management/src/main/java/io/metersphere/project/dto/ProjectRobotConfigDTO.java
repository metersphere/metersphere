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

    @Schema(description = "消息配置机器人是否开启")
    public Boolean enable;

    @Schema(description = "消息配置机器人发送模版")
    public String template;

    @Schema(description = "消息配置机器人发送模版")
    public String defaultTemplate;

    @Schema(description = "消息配置机器人是否使用默认模版")
    public Boolean useDefaultTemplate;

    @Schema(description = "邮件机器人配置的标题")
    public String subject;

    @Schema(description = "邮件机器人配置的默认标题")
    public String defaultSubject;

    @Schema(description = "邮件机器人是否使用默认标题")
    public Boolean useDefaultSubject;

    @Schema(description = "消息模版的标题")
    public String title;

    @Schema(description = "消息模版的时间")
    public Long time;
}
