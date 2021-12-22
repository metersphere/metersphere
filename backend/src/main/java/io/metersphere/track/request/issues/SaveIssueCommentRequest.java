package io.metersphere.track.request.issues;

import io.metersphere.base.domain.IssueComment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveIssueCommentRequest extends IssueComment {
     private String reviewId;
}
