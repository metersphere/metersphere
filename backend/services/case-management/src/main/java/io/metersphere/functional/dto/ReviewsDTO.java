package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class ReviewsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用例id")
    private String caseId;

    @Schema(description = "用例评审人id字符串集合")
    private String userIds;

    @Schema(description = "用例评审人名称字符集合")
    private String userNames;

}
