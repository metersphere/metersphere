package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.IssuesMapper;
import io.metersphere.base.mapper.TestCaseIssuesMapper;
import io.metersphere.base.mapper.TestPlanTestCaseMapper;
import io.metersphere.commons.constants.IssueRefType;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.request.issues.IssuesRelevanceRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseIssueService {

    @Resource
    private TestCaseIssuesMapper testCaseIssuesMapper;
    @Lazy
    @Resource
    private TestCaseService testCaseService;
    @Resource
    private IssuesMapper issuesMapper;
    @Resource
    @Lazy
    private IssuesService issuesService;
    @Resource
    private TestPlanTestCaseMapper testPlanTestCaseMapper;


    public void delTestCaseIssues(String testCaseId) {
        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria().andResourceIdEqualTo(testCaseId);
        testCaseIssuesMapper.deleteByExample(example);
    }

    public List<TestCaseDTO> list(IssuesRelevanceRequest request) {
        List<String> caseIds = getCaseIdsByIssuesId(request.getIssuesId());
        List<TestCaseDTO> list = testCaseService.getTestCaseByIds(caseIds);
        testCaseService.addProjectName(list);
        return list;
    }

    public List<TestCaseIssues> getTestCaseIssuesByIssuesId(String issuesId) {
        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria().andIssuesIdEqualTo(issuesId);
        return testCaseIssuesMapper.selectByExample(example);
    }

    /**
     * 测试计划的用例获取对应的功能用例
     * @param issuesId
     * @return
     */
    public List<String> getCaseIdsByIssuesId(String issuesId) {
        List<TestCaseIssues> testCaseIssueList = getTestCaseIssuesByIssuesId(issuesId);
        List<String> caseIds = new ArrayList<>();
        testCaseIssueList.forEach(i -> {
            if (StringUtils.equals(i.getRefType(), IssueRefType.PLAN_FUNCTIONAL.name())) {
                caseIds.add(i.getRefId());
            } else {
                caseIds.add(i.getResourceId());
            }
        });
        return caseIds;
    }

    public void relate(IssuesRelevanceRequest request) {
        if (StringUtils.isNotBlank(request.getCaseResourceId())) {
            List<String> issueIds = request.getIssueIds();
            if (!CollectionUtils.isEmpty(issueIds)) {
                issueIds.forEach(issueId -> {
                    relate(request, issueId, request.getCaseResourceId());
                });
            }
        } else if (StringUtils.isNotBlank(request.getIssuesId())) {
            List<String> caseResourceIds = request.getCaseResourceIds();
            if (!CollectionUtils.isEmpty(caseResourceIds)) {
                caseResourceIds.forEach(caseResourceId -> {
                    relate(request, request.getIssuesId(), caseResourceId);
                });
            }
        }
    }

    protected void relate(IssuesRelevanceRequest request, String issueId, String caseResourceId) {
        if (request.getIsPlanEdit()) {
            add(issueId, caseResourceId, request.getRefId(), IssueRefType.PLAN_FUNCTIONAL.name());
            updateIssuesCount(request.getCaseResourceId());
        } else {
            add(issueId, caseResourceId, null, IssueRefType.FUNCTIONAL.name());
        }
    }

    public void updateIssuesCount(String resourceId) {
        List<IssuesDao> issues = issuesService.getIssues(resourceId, IssueRefType.PLAN_FUNCTIONAL.name());
        int issuesCount = issues.size();
        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
        example.createCriteria().andIdEqualTo(resourceId);
        TestPlanTestCaseWithBLOBs testPlanTestCase = new TestPlanTestCaseWithBLOBs();
        testPlanTestCase.setIssuesCount(issuesCount);
        if (!CollectionUtils.isEmpty(issues)) {
            testPlanTestCase.setIssues(JSONObject.toJSONString(issues));
        }
        testPlanTestCaseMapper.updateByExampleSelective(testPlanTestCase, example);
    }

    public void add(String issuesId, String resourceId, String refId, String refType) {
        if (StringUtils.isNotBlank(resourceId)) {
            TestCaseIssues testCaseIssues = new TestCaseIssues();
            testCaseIssues.setId(UUID.randomUUID().toString());
            testCaseIssues.setIssuesId(issuesId);
            testCaseIssues.setResourceId(resourceId);
            testCaseIssues.setRefType(refType);
            testCaseIssues.setRefId(refId);
            testCaseIssuesMapper.insert(testCaseIssues);
        }
    }

    public String getLogDetails(IssuesRelevanceRequest request) {
        TestCaseWithBLOBs bloBs = testCaseService.getTestCase(request.getCaseId());
        if (bloBs != null) {
            IssuesExample example = new IssuesExample();
            example.createCriteria().andIdIn(request.getIssueIds());
            List<Issues> issues = issuesMapper.selectByExample(example);
            List<String> names = issues.stream().map(Issues::getTitle).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(request.getIssueIds()), bloBs.getProjectId(), bloBs.getName() + " 关联 " + names, bloBs.getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }
}
