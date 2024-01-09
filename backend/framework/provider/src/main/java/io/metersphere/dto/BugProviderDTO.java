package io.metersphere.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class BugProviderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "缺陷id")
    private String id;

    @Schema(description = "缺陷名称")
    private String name;

    @Schema(description = "处理人")
    private String handleUser;

    @Schema(description = "处理人姓名")
    private String handleUserName;

    @Schema(description = "缺陷状态")
    private String status;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "创建时间")
    private Long createTime;
}
