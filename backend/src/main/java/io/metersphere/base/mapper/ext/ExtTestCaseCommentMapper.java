package io.metersphere.base.mapper.ext;

import io.metersphere.track.dto.TestCaseCommentDTO;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExtTestCaseCommentMapper {

    /**
     * 获取用例的评论
     * @param caseId
     * @return
     */
    List<TestCaseCommentDTO> getCaseComments(@Param("caseId") String caseId);

}
