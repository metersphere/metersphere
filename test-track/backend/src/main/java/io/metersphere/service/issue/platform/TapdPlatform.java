package io.metersphere.service.issue.platform;

import io.metersphere.base.domain.IssuesWithBLOBs;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.IssuesStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.xpack.track.dto.*;
import io.metersphere.dto.*;
import io.metersphere.i18n.Translator;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.xpack.track.dto.request.IssuesUpdateRequest;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.issue.client.TapdClient;
import io.metersphere.service.issue.domain.tapd.TapdBug;
import io.metersphere.service.issue.domain.tapd.TapdConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TapdPlatform extends AbstractIssuePlatform {

    protected TapdClient tapdClient;

    public TapdPlatform(IssuesRequest issueRequest) {
        super(issueRequest);
        this.key = IssuesManagePlatform.Tapd.name();
        tapdClient = new TapdClient();
        setConfig();
    }

    // xpack 反射调用
    public TapdClient getTapdClient() {
        return tapdClient;
    }

    @Override
    public List<IssuesDao> getIssue(IssuesRequest issuesRequest) {
        issuesRequest.setPlatform(key);
        List<IssuesDao> issues;
        if (StringUtils.isNotBlank(issuesRequest.getProjectId())) {
            issues = extIssuesMapper.getIssues(issuesRequest);
        } else {
            issues = extIssuesMapper.getIssuesByCaseId(issuesRequest);
        }
        for (IssuesDao issue : issues) {
            if(StringUtils.isNotBlank(issue.getPlatform())&&issue.getPlatform().equals("Tapd")){
                List<String> tapdUsers = getTapdUsers(issue.getProjectId(), issue.getPlatformId());
                issue.setTapdUsers(tapdUsers);
            }
        }
        return issues;
    }

    public List<String> getTapdUsers(String projectId,String num){
        List<String>ids = new ArrayList<>(1);
        ids.add(num);
        List tapdIssues = tapdClient.getIssueForPageByIds(getProjectId(projectId),1,50,ids).getData();
        List<String>tapdUsers = new ArrayList<>(tapdIssues.size());
        for (Object tapdIssue : tapdIssues) {
            Map bug = (Map) ((Map) tapdIssue).get("Bug");
            String currentOwner = Optional.ofNullable(bug.get("current_owner")).orElse("").toString();
            tapdUsers.add(currentOwner);
        }
        return tapdUsers;
    }

    @Override
    public List<DemandDTO> getDemandList(String projectId) {
        List<DemandDTO> demandList = new ArrayList<>();
        List demands = tapdClient.getDemands(getProjectId(projectId));
        for (int i = 0; i < demands.size(); i++) {
            Map o = (Map) demands.get(i);
            DemandDTO demand = JSON.parseObject(JSON.toJSONString(o.get("Story")), DemandDTO.class);
            demand.setPlatform(key);
            demandList.add(demand);
        }
        return demandList;
    }

    @Override
    public IssuesWithBLOBs addIssue(IssuesUpdateRequest issuesRequest) {

        MultiValueMap<String, Object> param = buildUpdateParam(issuesRequest);
        TapdBug bug = tapdClient.addIssue(param);
        Map<String, String> statusMap = tapdClient.getStatusMap(getProjectId(this.projectId));
        issuesRequest.setPlatformStatus(statusMap.get(bug.getStatus()));

        issuesRequest.setPlatformId(bug.getId());
        issuesRequest.setPlatformStatus(bug.getStatus());
        issuesRequest.setId(UUID.randomUUID().toString());

        // 插入缺陷表
        IssuesWithBLOBs issues = insertIssues(issuesRequest);

        // 用例与第三方缺陷平台中的缺陷关联
        handleTestCaseIssues(issuesRequest);

        return issues;
    }

    @Override
    public void updateIssue(IssuesUpdateRequest request) {
        MultiValueMap<String, Object> param = buildUpdateParam(request);
        param.add("id", request.getPlatformId());
        handleIssueUpdate(request);
        tapdClient.updateIssue(param);
    }

    private MultiValueMap<String, Object> buildUpdateParam(IssuesUpdateRequest issuesRequest) {
        issuesRequest.setPlatform(key);

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
        if (issuesRequest.getTransitions() != null) {
            paramMap.add("status", issuesRequest.getTransitions().getValue());
        }

        addCustomFields(issuesRequest, paramMap);

        paramMap.add("reporter", reporter);
        return paramMap;
    }

    private String msDescription2Tapd(String msDescription) {
        SystemParameterService parameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        msDescription = msImg2HtmlImg(msDescription, parameterService.getValue("base.url"));
        return msDescription.replaceAll("\\n", "<br/>");
    }

    @Override
    public void deleteIssue(String id) {
        super.deleteIssue(id);
        // todo 暂无删除API
    }

    @Override
    public void testAuth() {
        tapdClient.auth();
    }

    @Override
    public void userAuth(UserDTO.PlatformInfo userInfo) {
        testAuth();
    }

    @Override
    public List<PlatformUser> getPlatformUser() {
        Boolean exist = checkProjectExist(getProjectId(projectId));
        if (!exist) {
            MSException.throwException(Translator.get("tapd_project_not_exist"));
        }
        List<PlatformUser> users = new ArrayList<>();
        List res = tapdClient.getPlatformUser(getProjectId(projectId));
        for (int i = 0; i < res.size(); i++) {
            Map o = (Map) res.get(i);
            PlatformUser user = JSON.parseObject(JSON.toJSONString(o.get("UserWorkspace")), PlatformUser.class);
            users.add(user);
        }
        return users;
    }

    @Override
    public void syncIssues(Project project, List<IssuesDao> tapdIssues) {
        Map<String, String> idMap = tapdIssues.stream()
                .collect(Collectors.toMap(IssuesDao::getPlatformId, IssuesDao::getId));

        List<String> ids = tapdIssues.stream()
                .map(IssuesDao::getPlatformId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(ids)) return;
        HashMap<String, List<CustomFieldResourceDTO>> customFieldMap = new HashMap<>();

        Map<String, String> statusMap = tapdClient.getStatusMap(project.getTapdId());

        int index = 0;
        int limit = 50;

        while (index < ids.size()) {
            List<String> subIds = ids.subList(index, (index + limit) > ids.size() ? ids.size() : (index + limit));
            TapdGetIssueResponse result = tapdClient.getIssueForPageByIds(project.getTapdId(), 1, limit, subIds);
            List<Map> datas = result.getData();
            datas.forEach(issue -> {
                Map bug = (Map) issue.get("Bug");
                String platformId = bug.get("id").toString();
                String id = idMap.get(platformId);
                IssuesWithBLOBs updateIssue = getUpdateIssue(issuesMapper.selectByPrimaryKey(id), bug);
                updateIssue.setId(id);
                updateIssue.setCustomFields(syncIssueCustomField(updateIssue.getCustomFields(), bug));
                customFieldMap.put(id, baseCustomFieldService.getCustomFieldResourceDTO(updateIssue.getCustomFields()));
                issuesMapper.updateByPrimaryKeySelective(updateIssue);
                ids.remove(platformId);
            });
            index += limit;
        }
        // 查不到的设置为删除
        ids.forEach((id) -> {
            if (StringUtils.isNotBlank(idMap.get(id))) {
                IssuesDao issuesDao = new IssuesDao();
                issuesDao.setId(idMap.get(id));
                issuesDao.setPlatformStatus(IssuesStatus.DELETE.toString());
                issuesMapper.updateByPrimaryKeySelective(issuesDao);
            }
        });
        customFieldIssuesService.batchEditFields(customFieldMap);
    }

    public IssuesWithBLOBs getUpdateIssue(IssuesWithBLOBs issue, Map bug) {
        if (issue == null) {
            issue = new IssuesWithBLOBs();
            issue.setCustomFields(defaultCustomFields);
        } else {
            mergeCustomField(issue, defaultCustomFields);
        }

        parseIssue(issue, bug);
        return issue;
    }

    public IssuesWithBLOBs getUpdateIssue(Map bug) {
        return getUpdateIssue(null, bug);
    }

    private void parseIssue(IssuesWithBLOBs issue, Map bug) {
        TapdBug bugObj = JSON.parseObject(JSON.toJSONString(bug), TapdBug.class);
        BeanUtils.copyBean(issue, bugObj);
        issue.setPlatformStatus(bugObj.getStatus());
        issue.setDescription(htmlDesc2MsDesc(issue.getDescription()));
        issue.setCustomFields(syncIssueCustomField(issue.getCustomFields(), bug));
        issue.setPlatform(key);
        try {
            issue.setCreateTime(DateUtils.getTime((String) bug.get("created")).getTime());
            issue.setUpdateTime(DateUtils.getTime((String) bug.get("modified")).getTime());
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public IssuesWithBLOBs getUpdateIssue(IssuesWithBLOBs issue, Map bug, List<CustomFieldItemDTO> customField) {
        if (issue == null) {
            issue = new IssuesWithBLOBs();
            issue.setCustomFields(defaultCustomFields);
        } else {
            issue.setCustomFields(JSON.toJSONString(customField));
            mergeIfIssueWithCustomField(issue, defaultCustomFields);
        }
        parseIssue(issue, bug);
        return issue;
    }

    @Override
    public String getProjectId(String projectId) {
        return getProjectId(projectId, Project::getTapdId);
    }

    public TapdConfig getConfig() {
        return getConfig(key, TapdConfig.class);
    }

    public TapdConfig setConfig() {
        TapdConfig config = getConfig();
        tapdClient.setConfig(config);
        return config;
    }

    public String getReporter() {
        UserDTO.PlatformInfo userPlatInfo = getUserPlatInfo(this.workspaceId);
        if (userPlatInfo != null && StringUtils.isNotBlank(userPlatInfo.getTapdUserName())) {
            return userPlatInfo.getTapdUserName();
        }
        return null;
    }

    @Override
    public Boolean checkProjectExist(String relateId) {
        try {
            return tapdClient.checkProjectExist(relateId);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void syncIssuesAttachment(IssuesUpdateRequest issuesRequest, File file, AttachmentSyncType syncType) {
        // TODO: 同步缺陷MS附件到TAPD
    }

    @Override
    public List<PlatformStatusDTO> getTransitions(String issueKey) {
        List<PlatformStatusDTO> platformStatusDTOS = new ArrayList<>();
        Project project = baseProjectService.getProjectById(this.projectId);

        // 获取缺陷状态数据
        Map<String, String> statusMap = tapdClient.getStatusMap(project.getTapdId());
        for (String key : statusMap.keySet()) {
            PlatformStatusDTO platformStatusDTO = new PlatformStatusDTO();
            platformStatusDTO.setValue(key);
            platformStatusDTO.setLable(statusMap.get(key));
            platformStatusDTOS.add(platformStatusDTO);
        }

        return platformStatusDTOS;
    }
}
