package io.metersphere.track.issue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.Issues;
import io.metersphere.base.domain.IssuesDao;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.IssuesStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.ResultHolder;
import io.metersphere.dto.CustomFieldItemDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.dto.DemandDTO;
import io.metersphere.track.issue.client.TapdClient;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.issue.domain.tapd.TapdBug;
import io.metersphere.track.issue.domain.tapd.TapdConfig;
import io.metersphere.track.issue.domain.tapd.TapdGetIssueResponse;
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

    private TapdClient tapdClient = new TapdClient();


    public TapdPlatform(IssuesRequest issueRequest) {
        super(issueRequest);
    }

    @Override
    public List<IssuesDao> getIssue(IssuesRequest issuesRequest) {
        issuesRequest.setPlatform(IssuesManagePlatform.Tapd.toString());
        List<IssuesDao> issues;
        if (StringUtils.isNotBlank(issuesRequest.getProjectId())) {
            issues = extIssuesMapper.getIssuesByProjectId(issuesRequest);
        } else {
            issues = extIssuesMapper.getIssuesByCaseId(issuesRequest);
        }
        return issues;
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

    @Override
    public void addIssue(IssuesUpdateRequest issuesRequest) {
        issuesRequest.setPlatform(IssuesManagePlatform.Tapd.toString());

        List<CustomFieldItemDTO> customFields = getCustomFields(issuesRequest.getCustomFields());

        String tapdId = getProjectId(issuesRequest.getProjectId());

        if (StringUtils.isBlank(tapdId)) {
            MSException.throwException("未关联Tapd 项目ID");
        }

        String usersStr = "";
        List<String> platformUsers = issuesRequest.getTapdUsers();
        if (CollectionUtils.isNotEmpty(platformUsers)) {
            usersStr = String.join(";", platformUsers);
        }

        String reporter = getReporter();
        if (StringUtils.isBlank(reporter)) {
            reporter = SessionUtils.getUser().getName();
        }

        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("title", issuesRequest.getTitle());
        paramMap.add("workspace_id", tapdId);
        paramMap.add("description", msDescription2Tapd(issuesRequest.getDescription()));
        paramMap.add("current_owner", usersStr);

        customFields.forEach(item -> {
            if (StringUtils.isNotBlank(item.getCustomData())) {
                paramMap.add(item.getCustomData(), item.getValue());
            }
        });
        paramMap.add("reporter", reporter);

        setConfig();
        TapdBug bug = tapdClient.addIssue(paramMap);
        Map<String, String> statusMap = tapdClient.getStatusMap(getProjectId(this.projectId));
        issuesRequest.setPlatformStatus(statusMap.get(bug.getStatus()));

        issuesRequest.setId(bug.getId());
        // 用例与第三方缺陷平台中的缺陷关联
        handleTestCaseIssues(issuesRequest);

        // 插入缺陷表
        insertIssues(bug.getId(), issuesRequest);
    }

    @Override
    public void updateIssue(IssuesUpdateRequest request) {
        // todo 调用接口
        request.setDescription(null);
        handleIssueUpdate(request);
    }

    private String msDescription2Tapd(String msDescription) {
        SystemParameterService parameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        msDescription = msImg2HtmlImg(msDescription, parameterService.getValue("base.url"));
        return msDescription.replaceAll("\\n", "<br/>");
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
    public void userAuth(UserDTO.PlatformInfo userInfo) {
        testAuth();
    }

    @Override
    public List<PlatformUser> getPlatformUser() {
        List<PlatformUser> users = new ArrayList<>();
        String id = getProjectId(projectId);
        if (StringUtils.isBlank(id)) {
            MSException.throwException("未关联Tapd项目ID");
        }
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
    public void syncIssues(Project project, List<IssuesDao> tapdIssues) {
        int pageNum = 1;
        int limit = 50;
        int count = 50;

        List<String> ids = tapdIssues.stream()
                .map(Issues::getId)
                .collect(Collectors.toList());

        LogUtil.info("ids: " + ids);

        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        setConfig();

        Map<String, String> statusMap = tapdClient.getStatusMap(project.getTapdId());

        while (count == limit) {
            count = 0;
            TapdGetIssueResponse result = tapdClient.getIssueForPageByIds(project.getTapdId(), pageNum, limit, ids);
            List<TapdGetIssueResponse.Data> data = result.getData();
            count = data.size();
            pageNum++;
            data.forEach(issue -> {
                TapdBug bug = issue.getBug();
                IssuesDao issuesDao = new IssuesDao();
                BeanUtils.copyBean(issuesDao, bug);
                issuesDao.setPlatformStatus(statusMap.get(bug.getStatus()));
                issuesDao.setDescription(htmlDesc2MsDesc(issuesDao.getDescription()));
                issuesMapper.updateByPrimaryKeySelective(issuesDao);
                ids.remove(issue.getBug().getId());
            });
        }
        // 查不到的就置为删除
        ids.forEach((id) -> {
            IssuesDao issuesDao = new IssuesDao();
            issuesDao.setId(id);
            issuesDao.setPlatformStatus(IssuesStatus.DELETE.toString());
            issuesMapper.updateByPrimaryKeySelective(issuesDao);
        });
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

    public TapdConfig getConfig() {
        String config = getPlatformConfig(IssuesManagePlatform.Tapd.toString());
        TapdConfig tapdConfig = JSONObject.parseObject(config, TapdConfig.class);
//        validateConfig(tapdConfig);
        return tapdConfig;
    }

    public String getReporter() {
        UserDTO.PlatformInfo userPlatInfo = getUserPlatInfo(this.orgId);
        if (userPlatInfo != null && StringUtils.isNotBlank(userPlatInfo.getTapdUserName())) {
            return userPlatInfo.getTapdUserName();
        }
        return null;
    }

    public TapdConfig setConfig() {
        TapdConfig config = getConfig();
        tapdClient.setConfig(config);
        return config;
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
