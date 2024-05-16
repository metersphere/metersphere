package io.metersphere.plan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class TestPlanCaseExecHistoryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private String status;

    @Schema(description = "评论内容")
    private byte[] content;

    @Schema(description =  "评审解析内容")
    private String contentText;

    @Schema(description = "步骤结果")
    private String stepsExecResult;

    @Schema(description = "执行人")
    private String createUser;

    @Schema(description = "执行人姓名")
    private String userName;

    @Schema(description =  "执行人头像")
    private String userLogo;

    @Schema(description =  "执行人邮箱")
    private String email;

    @Schema(description = "执行时间")
    private String steps;
}
