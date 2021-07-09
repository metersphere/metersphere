package io.metersphere.track.controller;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.track.dto.TestCaseDTO;
import io.metersphere.track.request.issues.IssuesRelevanceRequest;
import io.metersphere.track.service.IssuesService;
import io.metersphere.track.service.TestCaseIssueService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("test/case/issues")
@RestController
public class TestCaseIssuesController {

    @Resource
    private TestCaseIssueService testCaseIssueService;
    @Resource
    private IssuesService issuesService;
    @Resource
    private ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;

    @PostMapping("/list")
    public List<TestCaseDTO> list(@RequestBody IssuesRelevanceRequest request) {
        return testCaseIssueService.list(request);
    }

    @PostMapping("/relate")
    @MsAuditLog(module = "track_test_case", type = OperLogConstants.ASSOCIATE_ISSUE, content = "#msClass.getLogDetails(#request)", msClass = TestCaseIssueService.class)
    public void relate(@RequestBody IssuesRelevanceRequest request) {
        testCaseIssueService.relate(request);
        try {
            List<IssuesDao> issues = issuesService.getIssues(request.getCaseId());
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(issues)) {
                LogUtil.error(request.getCaseId() + "下的缺陷为空");
            }
            int issuesCount = issues.size();
            this.updateIssues(issuesCount, "", request.getCaseId(), JSON.toJSONString(issues));
        } catch (Exception e) {
            LogUtil.error("处理bug数量报错caseId: {}, message: {}", request.getCaseId(), ExceptionUtils.getStackTrace(e));
        }
    }

    public void updateIssues(int issuesCount, String id, String caseId, String issues) {
        extTestPlanTestCaseMapper.update(issuesCount, id, caseId, issues);
    }
}
