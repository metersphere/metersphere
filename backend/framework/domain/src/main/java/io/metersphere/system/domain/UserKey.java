package io.metersphere.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "用户api key")
@TableName("user_key")
@Data
public class UserKey implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{user_key.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "user_key ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{user_key.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_key.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "用户ID", required = true, allowableValues = "range[1, 50]")
    private String createUser;

    @Size(min = 1, max = 50, message = "{user_key.access_key.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_key.access_key.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "access_key", required = true, allowableValues = "range[1, 50]")
    private String accessKey;

    @Size(min = 1, max = 50, message = "{user_key.secret_key.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_key.secret_key.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "secret key", required = true, allowableValues = "range[1, 50]")
    private String secretKey;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "状态", required = false, allowableValues = "range[1, 10]")
    private String status;


}