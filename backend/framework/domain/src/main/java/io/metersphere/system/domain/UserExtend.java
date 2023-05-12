package io.metersphere.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "用户扩展")
@TableName("user_extend")
@Data
public class UserExtend implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{user_extend.user_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "用户ID", required = true, allowableValues = "range[1, 50]")
    private String userId;


    @ApiModelProperty(name = "其他平台对接信息", required = false, allowableValues = "range[1, 2000]")
    private byte[] platformInfo;


    @ApiModelProperty(name = "UI本地调试地址", required = false, allowableValues = "range[1, 255]")
    private String seleniumServer;


}