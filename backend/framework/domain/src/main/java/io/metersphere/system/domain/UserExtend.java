package io.metersphere.system.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "用户扩展")
@Table("user_extend")
@Data
public class UserExtend implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{user_extend.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "用户ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "其他平台对接信息", required = false, allowableValues = "range[1, 2000]")
    private byte[] platformInfo;


    @ApiModelProperty(name = "UI本地调试地址", required = false, allowableValues = "range[1, 255]")
    private String seleniumServer;


}