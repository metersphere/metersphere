package io.metersphere.functional.dto;

import io.metersphere.functional.domain.CaseReviewHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CaseReviewHistoryDTO extends CaseReviewHistory {

    @Schema(description =  "评审人头像")
    private String userLogo;

    @Schema(description =  "评审人名")
    private String userName;

    @Schema(description =  "评审解析内容")
    private String contentText;

}
