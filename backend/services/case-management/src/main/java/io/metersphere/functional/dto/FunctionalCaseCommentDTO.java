package io.metersphere.functional.dto;

import io.metersphere.functional.domain.FunctionalCaseComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class FunctionalCaseCommentDTO extends FunctionalCaseComment {

    @Schema(description =  "评论的人名")
    private String userName;

    @Schema(description =  "评论的人头像")
    private String userLogo;

    @Schema(description =  "被回复的人名")
    private String replyUserName;

    @Schema(description =  "被回复的人头像")
    private String replyUserLogo;

    @Schema(description =  "该条评论下的所有回复数据")
    private List<FunctionalCaseCommentDTO> childComments;

}
