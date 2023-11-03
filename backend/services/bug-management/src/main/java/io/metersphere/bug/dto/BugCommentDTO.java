package io.metersphere.bug.dto;

import io.metersphere.bug.domain.BugComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugCommentDTO extends BugComment {

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "回复人名称")
    private String replyUserName;

    @Schema(description = "子评论")
    private List<BugCommentDTO> childComments;
}
