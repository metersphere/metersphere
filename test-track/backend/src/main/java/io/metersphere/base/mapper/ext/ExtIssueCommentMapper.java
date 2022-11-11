package io.metersphere.base.mapper.ext;

import io.metersphere.dto.IssueCommentDTO;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ExtIssueCommentMapper {

    /**
     * 获取用例的评论
     * @param issueId
     * @return
     */
    List<IssueCommentDTO> getComments(@Param("issueId") String issueId);

    /**
     * 获取多条用例的的评论
     * @param issueIds
     * @return
     */
    List<IssueCommentDTO> getCommentsByIssueIds(@Param("issueIds") List<String> issueIds);

}
