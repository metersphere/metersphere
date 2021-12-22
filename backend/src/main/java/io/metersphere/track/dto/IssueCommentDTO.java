package io.metersphere.track.dto;

import io.metersphere.base.domain.IssueComment;
import lombok.Data;

@Data
public class IssueCommentDTO extends IssueComment {
    private String authorName;
}
