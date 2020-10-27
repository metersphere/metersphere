package io.metersphere.track.dto;

import io.metersphere.base.domain.TestCaseComment;
import lombok.Data;

@Data
public class TestCaseCommentDTO extends TestCaseComment {
    private String authorName;
}
