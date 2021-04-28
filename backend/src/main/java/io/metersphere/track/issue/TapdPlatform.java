package io.metersphere.track.issue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.IssuesStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.ResultHolder;
import io.metersphere.track.dto.DemandDTO;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TapdPlatform extends AbstractIssuePlatform {

    protected String key = IssuesManagePlatform.Tapd.toString();

    public TapdPlatform(IssuesRequest issueRequest) {
        super(issueRequest);
    }

    @Override
    public List<IssuesDao> getIssue(IssuesRequest issuesRequest) {
        List<IssuesDao> list = new ArrayList<>();
        String tapdId = getProjectId(issuesRequest.getProjectId());

//        TestCaseIssuesExample example = new TestCaseIssuesExample();
//        example.createCriteria().andTestCaseIdEqualTo(testCaseId);

        issuesRequest.setPlatform(IssuesManagePlatform.Tapd.toString());
        List<IssuesDao> issues;
        if (StringUtils.isNotBlank(issuesRequest.getProjectId())) {
            issues = extIssuesMapper.getIssuesByProjectId(issuesRequest);
        } else {
            issues = extIssuesMapper.getIssuesByCaseId(issuesRequest);
        }

        List<String> issuesIds = issues.stream().map(Issues::getId).collect(Collectors.toList());
        issuesIds.forEach(issuesId -> {
            IssuesDao dto = getTapdIssues(tapdId, issuesId);
            if (StringUtils.isBlank(dto.getId())) {
                // 缺陷不存在，解除用例和缺陷的关联
                TestCaseIssuesExample issuesExample = new TestCaseIssuesExample();
                TestCaseIssuesExample.Criteria criteria = issuesExample.createCriteria();
                if (StringUtils.isNotBlank(testCaseId)) {
                    criteria.andTestCaseIdEqualTo(testCaseId);
                }
                criteria.andIssuesIdEqualTo(issuesId);
                testCaseIssuesMapper.deleteByExample(issuesExample);
                issuesMapper.deleteByPrimaryKey(issuesId);
            } else {
                dto.setPlatform(IssuesManagePlatform.Tapd.toString());
                // 缺陷状态为 关闭，则不显示
                if (!StringUtils.equals(IssuesStatus.CLOSED.toString(), dto.getStatus())) {
                    list.add(dto);
                }
            }
        });
        return list;
    }

    @Override
    public void filter(List<IssuesDao> issues) {
        String tapdId = "";
        for (IssuesDao item : issues) {
            if (StringUtils.isNotBlank(item.getProjectId())) {
                tapdId = getProjectId(issues.get(0).getProjectId());
                break;
            }
        }

        for (IssuesDao item : issues) {
            IssuesDao dto = getTapdIssues(tapdId, item.getId());
            if (StringUtils.isBlank(dto.getId())) {
                // 标记成删除
                item.setStatus(IssuesStatus.DELETE.toString());
            } else {
                // 缺陷状态为 完成，则不显示
                if (!StringUtils.equals(IssuesStatus.CLOSED.toString(), dto.getStatus())) {
                    item.setStatus(IssuesStatus.CLOSED.toString());
                }
            }
        }
    }

    @Override
    public List<DemandDTO> getDemandList(String projectId) {
        List<DemandDTO> demandList = new ArrayList<>();
        try {
            String url = "https://api.tapd.cn/stories?workspace_id=" + getProjectId(projectId);
            ResultHolder call = call(url);
            String listJson = JSON.toJSONString(call.getData());
            JSONArray jsonArray = JSON.parseArray(listJson);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                DemandDTO demand = o.getObject("Story", DemandDTO.class);
                demand.setPlatform(IssuesManagePlatform.Tapd.name());
                demandList.add(demand);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }

        return demandList;
    }

    private IssuesDao getTapdIssues(String projectId, String issuesId) {
        String url = "https://api.tapd.cn/bugs?workspace_id=" + projectId + "&id=" + issuesId;
        ResultHolder call = call(url);
        String listJson = JSON.toJSONString(call.getData());
        if (StringUtils.equals(Boolean.FALSE.toString(), listJson)) {
            return new IssuesDao();
        }
        JSONObject jsonObject = JSONObject.parseObject(listJson);
        JSONObject bug = jsonObject.getJSONObject("Bug");
        Long created = bug.getLong("created");
        IssuesDao issues = jsonObject.getObject("Bug", IssuesDao.class);

        // 获取工作流中缺陷状态名称
        String workflow = "https://api.tapd.cn/workflows/status_map?workspace_id=" + projectId + "&system=bug";
        ResultHolder resultHolder = call(workflow);
        String workflowJson = JSON.toJSONString(resultHolder.getData());
        if (!StringUtils.equals(Boolean.FALSE.toString(), workflowJson)) {
            Map map = (Map) JSONObject.parse(workflowJson);
            issues.setStatus((String) map.get(issues.getStatus()));
        }

        issues.setCreateTime(created);
        return issues;
    }

    @Override
    public void addIssue(IssuesUpdateRequest issuesRequest) {
        issuesRequest.setPlatform(IssuesManagePlatform.Tapd.toString());

        String url = "https://api.tapd.cn/bugs";
        String testCaseId = issuesRequest.getTestCaseId();
        String tapdId = getProjectId(issuesRequest.getProjectId());

        if (StringUtils.isBlank(tapdId)) {
            MSException.throwException("未关联Tapd 项目ID");
        }

        String usersStr = "";
        List<String> platformUsers = issuesRequest.getTapdUsers();
        if (CollectionUtils.isNotEmpty(platformUsers)) {
            usersStr = String.join(";", platformUsers);
        }

        String username = SessionUtils.getUser().getName();

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("title", issuesRequest.getTitle());
        paramMap.add("workspace_id", tapdId);
        paramMap.add("description", issuesRequest.getDescription());
        paramMap.add("reporter", username);
        paramMap.add("current_owner", usersStr);

        ResultHolder result = call(url, HttpMethod.POST, paramMap);

        String listJson = JSON.toJSONString(result.getData());
        JSONObject jsonObject = JSONObject.parseObject(listJson);
        String issuesId = jsonObject.getObject("Bug", Issues.class).getId();

        issuesRequest.setId(issuesId);
        // 用例与第三方缺陷平台中的缺陷关联
        handleTestCaseIssues(issuesRequest);

        // 插入缺陷表
        insertIssuesWithoutContext(issuesId, issuesRequest);
    }

    @Override
    public void updateIssue(IssuesUpdateRequest request) {
        // todo 调用接口
        request.setDescription(null);
        handleIssueUpdate(request);
    }

    @Override
    public void deleteIssue(String id) {}

    @Override
    public void testAuth() {
        try {
            String tapdConfig = getPlatformConfig(IssuesManagePlatform.Tapd.toString());
            JSONObject object = JSON.parseObject(tapdConfig);
            String account = object.getString("account");
            String password = object.getString("password");
            HttpHeaders headers = auth(account, password);
            HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange("https://api.tapd.cn/quickstart/testauth", HttpMethod.GET, requestEntity, String.class);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("验证失败！");
        }
    }

    @Override
    public List<PlatformUser> getPlatformUser() {
        List<PlatformUser> users = new ArrayList<>();
        String id = getProjectId(projectId);
        String url = "https://api.tapd.cn/workspaces/users?workspace_id=" + id;
        ResultHolder call = call(url);
        String listJson = JSON.toJSONString(call.getData());
        JSONArray jsonArray = JSON.parseArray(listJson);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject o = jsonArray.getJSONObject(i);
            PlatformUser user = o.getObject("UserWorkspace", PlatformUser.class);
            users.add(user);
        }
        return users;
    }

    @Override
    String getProjectId(String projectId) {
        if (StringUtils.isNotBlank(projectId)) {
            return projectService.getProjectById(projectId).getTapdId();
        }
        TestCaseWithBLOBs testCase = testCaseService.getTestCase(testCaseId);
        Project project = projectService.getProjectById(testCase.getProjectId());
        return project.getTapdId();
    }

    private ResultHolder call(String url) {
        return call(url, HttpMethod.GET, null);
    }

    private ResultHolder call(String url, HttpMethod httpMethod, Object params) {
        String responseJson;

        String config = getPlatformConfig(IssuesManagePlatform.Tapd.toString());
        JSONObject object = JSON.parseObject(config);

        if (object == null) {
            MSException.throwException("tapd config is null");
        }

        String account = object.getString("account");
        String password = object.getString("password");

        HttpHeaders header = auth(account, password);

        if (httpMethod.equals(HttpMethod.GET)) {
            responseJson = TapdRestUtils.get(url, header);
        } else {
            responseJson = TapdRestUtils.post(url, params, header);
        }

        ResultHolder result = JSON.parseObject(responseJson, ResultHolder.class);

        if (!result.isSuccess()) {
            MSException.throwException(result.getMessage());
        }
        return JSON.parseObject(responseJson, ResultHolder.class);

    }


}
