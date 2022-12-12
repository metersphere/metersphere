package io.metersphere.base.mapper.ext;

import io.metersphere.dto.TestCaseCommentDTO;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExtTestCaseCommentMapper {

    /**
     * 获取用例的评论
     * @param caseId
     * @return
     */
    List<TestCaseCommentDTO> getCaseComments(@Param("caseId") String caseId, @Param("type") String type, @Param("belongId") String belongId);
}
