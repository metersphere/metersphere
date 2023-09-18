package io.metersphere.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
public class MessageTaskTypeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息配置功能类型")
    public String taskType;

    @Schema(description = "消息配置功能类型名称")
    public String taskTypeName;

    @Schema(description = "消息配置场景详情")
    public List<MessageTaskDetailDTO> messageTaskDetailDTOList;
}
