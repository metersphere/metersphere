package io.metersphere.functional.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.functional.dto.CommentEnum;
import io.metersphere.functional.mapper.FunctionalCaseCommentMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseCommentRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author guoyuqi
 */

@Service
public class FunctionalCaseCommentService {

    @Resource
    private FunctionalCaseCommentMapper functionalCaseCommentMapper;

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;

    /**
     * 新增评论
     * @param functionalCaseCommentRequest functionalCaseCommentDTO
     * @param userId 当前操作用户
     * @return FunctionalCaseComment
     */
    public FunctionalCaseComment saveComment(FunctionalCaseCommentRequest functionalCaseCommentRequest, String userId) {
        checkCase(functionalCaseCommentRequest);
        FunctionalCaseComment functionalCaseComment = new FunctionalCaseComment();
        functionalCaseComment.setId(IDGenerator.nextStr());
        functionalCaseComment.setCaseId(functionalCaseCommentRequest.getCaseId());
        functionalCaseComment.setContent(functionalCaseCommentRequest.getContent());
        functionalCaseComment.setCreateUser(userId);
        functionalCaseComment.setCreateTime(System.currentTimeMillis());
        functionalCaseComment.setUpdateTime(System.currentTimeMillis());
        functionalCaseComment.setType(CommentEnum.CASE.toString());
        if (StringUtils.isNotBlank(functionalCaseCommentRequest.getNotifier())) {
            functionalCaseComment.setNotifier(functionalCaseCommentRequest.getNotifier());
        }
        if (StringUtils.isNotBlank(functionalCaseCommentRequest.getParentId())) {
            functionalCaseComment.setParentId(functionalCaseCommentRequest.getParentId());
        }
        functionalCaseCommentMapper.insert(functionalCaseComment);
        return functionalCaseComment;
    }

    private void checkCase(FunctionalCaseCommentRequest functionalCaseCommentRequest) {
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey(functionalCaseCommentRequest.getCaseId());
        if (functionalCase ==null) {
            throw new MSException(Translator.get("case_comment.case_is_null"));
        }
    }
}
