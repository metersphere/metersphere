package io.metersphere.track.service;

import io.metersphere.base.domain.TestCaseComment;
import io.metersphere.base.domain.TestCaseCommentExample;
import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.TestCaseCommentMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.track.request.testreview.SaveCommentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseCommentService {

    @Resource
    TestCaseCommentMapper testCaseCommentMapper;
    @Resource
    UserMapper userMapper;

    public void saveComment(SaveCommentRequest request) {
        TestCaseComment testCaseComment = new TestCaseComment();
        testCaseComment.setId(UUID.randomUUID().toString());
        testCaseComment.setAuthor(SessionUtils.getUser().getId());
        testCaseComment.setCaseId(request.getCaseId());
        testCaseComment.setCreateTime(System.currentTimeMillis());
        testCaseComment.setUpdateTime(System.currentTimeMillis());
        testCaseComment.setDescription(request.getDescription());
        testCaseCommentMapper.insert(testCaseComment);
    }

    public List<TestCaseComment> getComments(String caseId) {
        TestCaseCommentExample testCaseCommentExample = new TestCaseCommentExample();
        testCaseCommentExample.setOrderByClause("update_time desc");
        testCaseCommentExample.createCriteria().andCaseIdEqualTo(caseId);
        List<TestCaseComment> testCaseComments = testCaseCommentMapper.selectByExampleWithBLOBs(testCaseCommentExample);
        testCaseComments.forEach(testCaseComment -> {
            String authorId = testCaseComment.getAuthor();
            User user = userMapper.selectByPrimaryKey(authorId);
            testCaseComment.setAuthor(user.getName());
        });
        return testCaseComments;
    }

    public void deleteComment(String caseId) {
        TestCaseCommentExample testCaseCommentExample = new TestCaseCommentExample();
        testCaseCommentExample.createCriteria().andCaseIdEqualTo(caseId);
        testCaseCommentMapper.deleteByExample(testCaseCommentExample);
    }
}
