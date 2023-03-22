package io.metersphere.service;


import io.metersphere.base.domain.TestCaseReviewTestCaseUsers;
import io.metersphere.base.domain.TestCaseReviewTestCaseUsersExample;
import io.metersphere.base.mapper.TestCaseReviewTestCaseUsersMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseReviewTestCaseUsersService {
    @Resource
    TestCaseReviewTestCaseUsersMapper testCaseReviewTestCaseUsersMapper;

    public List<String> getUsersByCaseId(String caseId, String reviewId) {
        TestCaseReviewTestCaseUsersExample example = new TestCaseReviewTestCaseUsersExample();
        example.createCriteria()
                .andCaseIdEqualTo(caseId)
                .andReviewIdEqualTo(reviewId);
        return testCaseReviewTestCaseUsersMapper.selectByExample(example)
                .stream()
                .map(TestCaseReviewTestCaseUsers::getUserId)
                .collect(Collectors.toList());
    }
}
