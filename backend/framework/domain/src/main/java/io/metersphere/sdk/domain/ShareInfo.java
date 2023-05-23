package io.metersphere.sdk.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "分享")
@Table("share_info")
@Data
public class ShareInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{share_info.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "分享ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "创建人", required = false, allowableValues = "range[1, 64]")
    private String createUser;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "分享类型single batch", required = false, allowableValues = "range[1, 64]")
    private String shareType;


    @ApiModelProperty(name = "分享扩展数据", required = false, allowableValues = "range[1, ]")
    private byte[] customData;


    @ApiModelProperty(name = "语言", required = false, allowableValues = "range[1, 10]")
    private String lang;


}