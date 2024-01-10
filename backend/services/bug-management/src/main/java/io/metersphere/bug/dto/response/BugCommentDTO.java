package io.metersphere.bug.dto.response;

import io.metersphere.bug.domain.BugComment;
import io.metersphere.system.dto.CommentUserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugCommentDTO extends BugComment {

    @Schema(description = "评论相关用户信息")
    private List<CommentUserInfo> commentUserInfos;

    @Schema(description = "子评论")
    private List<BugCommentDTO> childComments;
}
