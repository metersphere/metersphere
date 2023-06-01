package io.metersphere.issue.service;

import io.metersphere.issue.controller.result.IssueResultCode;
import io.metersphere.issue.domain.Issue;
import io.metersphere.issue.domain.IssueExample;
import io.metersphere.issue.mapper.IssueMapper;
import io.metersphere.sdk.exception.MSException;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jianxing
 * @date : 2023-5-17
 */
@Service
@Transactional
public class IssueService {

    @Resource
    private IssueMapper issueMapper;


    public List<Issue> list() {
        return new ArrayList<>();
    }

    public Issue get(String id) {
        return issueMapper.selectByPrimaryKey(id);
    }

    public Issue add(Issue issue) {
        IssueExample example = new IssueExample();
        example.createCriteria().andTitleEqualTo(issue.getTitle());
        example.createCriteria().andProjectIdEqualTo(issue.getProjectId());
        List<Issue> issues = issueMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(issues)) {
            MSException.throwException(IssueResultCode.ISSUE_EXIST_EXCEPTION);
        }
        issue.setCreateTime(System.currentTimeMillis());
        issue.setUpdateTime(System.currentTimeMillis());
        issueMapper.insert(issue);
        return issue;
    }

    public Issue update(Issue issue) {
        issueMapper.updateByPrimaryKeySelective(issue);
        return issue;
    }

    public int delete(String id) {
        return issueMapper.deleteByPrimaryKey(id);
    }
}
