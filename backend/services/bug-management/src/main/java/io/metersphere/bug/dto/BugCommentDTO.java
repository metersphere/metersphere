package io.metersphere.bug.dto;

import io.metersphere.bug.domain.BugComment;
import io.metersphere.system.dto.CommentUserInfo;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugCommentDTO extends BugComment {

    @Schema(description = "评论人信息")
    private CommentUserInfo commentUserInfo;

    @Schema(description = "回复人名称")
    private String replyUserName;

    @Schema(description = "@通知人集合")
    private List<OptionDTO> notifierOption;

    @Schema(description = "子评论")
    private List<BugCommentDTO> childComments;
}
