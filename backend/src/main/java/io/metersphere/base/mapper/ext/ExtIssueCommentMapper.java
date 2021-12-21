package io.metersphere.base.mapper.ext;

import io.metersphere.track.dto.IssueCommentDTO;
import io.metersphere.track.dto.TestCaseCommentDTO;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExtIssueCommentMapper {

    /**
     * 获取用例的评论
     * @param issueId
     * @return
     */
    List<IssueCommentDTO> getComments(@Param("issueId") String issueId);

}
