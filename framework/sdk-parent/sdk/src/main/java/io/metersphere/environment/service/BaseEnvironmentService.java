package io.metersphere.environment.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseApiTestEnvironmentMapper;
import io.metersphere.base.mapper.ext.BaseEnvironmentGroupMapper;
import io.metersphere.base.mapper.ext.ExtApiTestEnvironmentMapper;
import io.metersphere.commons.constants.FileAssociationType;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.NotificationConstants;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.environment.dto.*;
import io.metersphere.environment.utils.BatchProcessingUtil;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.metadata.service.FileAssociationService;
import io.metersphere.notice.service.NotificationService;
import io.metersphere.request.BodyFile;
import io.metersphere.request.variable.ScenarioVariable;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.BaseProjectService;
import io.metersphere.service.NodeTreeService;
import io.metersphere.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lyh
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseEnvironmentService extends NodeTreeService<ApiModuleDTO> {
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    @Resource
    private BaseEnvironmentGroupMapper baseEnvironmentGroupMapper;
    @Resource
    private ExtApiTestEnvironmentMapper extApiTestEnvironmentMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EnvironmentGroupProjectMapper environmentGroupProjectMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private BaseEnvGroupProjectService baseEnvironmentGroupProjectService;
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private FileAssociationService fileAssociationService;
    @Resource
    private BaseApiTestEnvironmentMapper baseApiTestEnvironmentMapper;
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private NotificationService notificationService;
    @Resource
    private UserGroupMapper userGroupMapper;

    public static final String MOCK_EVN_NAME = "Mock环境";

    public static final String POST_STEP = "postStepProcessor";
    public static final String PRE_STEP = "preStepProcessor";
    public static final String POST = "postProcessor";
    public static final String PRE = "preProcessor";
    public static final String SCRIPT = "script";
    public static final String JSR = "jsr223";
    public static final String ASSERTIONS = "assertions";

    public BaseEnvironmentService() {
        super(ApiModuleDTO.class);
    }

    public String getLogDetails(String id) {
        ApiTestEnvironmentWithBLOBs bloBs = apiTestEnvironmentMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, SystemReference.environmentColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(bloBs.getId()), bloBs.getProjectId(), bloBs.getName(), bloBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public EnvironmentGroup add(EnvironmentGroupRequest request) {
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        this.checkEnvironmentGroup(request);

        request.setId(UUID.randomUUID().toString());
        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateTime(System.currentTimeMillis());
        environmentGroupMapper.insertSelective(request);
        this.insertGroupProject(request);

        return request;
    }

    private void insertGroupProject(EnvironmentGroupRequest request) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        EnvironmentGroupProjectMapper mapper = sqlSession.getMapper(EnvironmentGroupProjectMapper.class);

        List<EnvironmentGroupProject> envGroupProject = request.getEnvGroupProject();
        if (CollectionUtils.isEmpty(envGroupProject)) {
            return;
        }
        List<String> distinctProjects = envGroupProject.stream().map(EnvironmentGroupProject::getProjectId).distinct().collect(Collectors.toList());
        if (envGroupProject.size() != distinctProjects.size()) {
            MSException.throwException(Translator.get("environment_group_has_duplicate_project"));
        }
        List<Project> projects = projectMapper.selectByExample(new ProjectExample());
        List<String> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
        List<ApiTestEnvironment> environments = apiTestEnvironmentMapper.selectByExample(new ApiTestEnvironmentExample());
        List<String> environmentIds = environments.stream().map(ApiTestEnvironment::getId).collect(Collectors.toList());

        for (EnvironmentGroupProject egp : envGroupProject) {
            String projectId = egp.getProjectId();
            String environmentId = egp.getEnvironmentId();
            if (StringUtils.isBlank(projectId) || StringUtils.isBlank(environmentId)) {
                continue;
            }

            if (!projectIds.contains(projectId) || !environmentIds.contains(environmentId)) {
                continue;
            }

            EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
            example.createCriteria().andEnvironmentGroupIdEqualTo(request.getId()).andProjectIdEqualTo(projectId);
            List<EnvironmentGroupProject> environmentGroupProjects = environmentGroupProjectMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(environmentGroupProjects)) {
                continue;
            }

            EnvironmentGroupProject e = new EnvironmentGroupProject();
            e.setId(UUID.randomUUID().toString());
            e.setEnvironmentGroupId(request.getId());
            e.setProjectId(projectId);
            e.setEnvironmentId(environmentId);
            mapper.insert(e);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public List<EnvironmentGroup> getList(EnvironmentGroupRequest request) {
        return baseEnvironmentGroupMapper.getList(request);
    }

    public void delete(String id) {
        apiTestEnvironmentMapper.deleteByPrimaryKey(id);
        EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
        example.createCriteria().andEnvironmentGroupIdEqualTo(id);
        environmentGroupProjectMapper.deleteByExample(example);
        FileAssociationExample associationExample = new FileAssociationExample();
        associationExample.createCriteria().andSourceIdEqualTo(id);
        fileAssociationMapper.deleteByExample(associationExample);
    }

    public EnvironmentGroup update(EnvironmentGroupRequest request) {
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        if (StringUtils.isNotBlank(request.getName())) {
            this.checkEnvironmentGroup(request);
        }
        request.setUpdateTime(System.currentTimeMillis());
        environmentGroupMapper.updateByPrimaryKeySelective(request);

        String envGroupId = request.getId();
        EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
        example.createCriteria().andEnvironmentGroupIdEqualTo(envGroupId);
        environmentGroupProjectMapper.deleteByExample(example);

        this.insertGroupProject(request);

        return request;
    }

    private void checkEnvironmentGroup(EnvironmentGroupRequest request) {
        String name = request.getName();
        if (StringUtils.isBlank(name)) {
            MSException.throwException(Translator.get("null_environment_group_name"));
        }

        EnvironmentGroupExample environmentGroupExample = new EnvironmentGroupExample();
        EnvironmentGroupExample.Criteria criteria = environmentGroupExample.createCriteria();
        criteria.andNameEqualTo(name).andWorkspaceIdEqualTo(request.getWorkspaceId());
        if (StringUtils.isNotBlank(request.getId())) {
            criteria.andIdNotEqualTo(request.getId());
        }

        if (environmentGroupMapper.countByExample(environmentGroupExample) > 0) {
            MSException.throwException(Translator.get("environment_group_name") + request.getName() + Translator.get("environment_group_exist"));
        }
    }

    public List<EnvironmentGroup> getRelateProjectGroup(String projectId) {
        return baseEnvironmentGroupMapper.getRelateProject(SessionUtils.getCurrentWorkspaceId(), projectId);
    }

    public void copy(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        EnvironmentGroup environmentGroup = environmentGroupMapper.selectByPrimaryKey(id);
        EnvironmentGroupRequest copy = new EnvironmentGroupRequest();
        String copyId = UUID.randomUUID().toString();
        copy.setId(copyId);

        String copyNameId = copyId.substring(0, 3);
        String copyName = environmentGroup.getName() + "_" + copyNameId + "_COPY";
        copy.setName(copyName);
        copy.setWorkspaceId(environmentGroup.getWorkspaceId());
        this.checkEnvironmentGroup(copy);

        copy.setCreateTime(System.currentTimeMillis());
        copy.setUpdateTime(System.currentTimeMillis());
        copy.setCreateUser(SessionUtils.getUserId());
        copy.setDescription(environmentGroup.getDescription());
        environmentGroupMapper.insertSelective(copy);

        EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
        example.createCriteria().andEnvironmentGroupIdEqualTo(id);
        List<EnvironmentGroupProject> environmentGroupProjects = environmentGroupProjectMapper.selectByExample(example);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        EnvironmentGroupProjectMapper mapper = sqlSession.getMapper(EnvironmentGroupProjectMapper.class);

        for (EnvironmentGroupProject environmentGroupProject : environmentGroupProjects) {
            environmentGroupProject.setId(UUID.randomUUID().toString());
            environmentGroupProject.setEnvironmentGroupId(copyId);
            mapper.insertSelective(environmentGroupProject);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void modify(EnvironmentGroupRequest request) {
        if (StringUtils.isBlank(request.getId()) || StringUtils.isBlank(request.getName())) {
            return;
        }
        checkEnvironmentGroup(request);
        environmentGroupMapper.updateByPrimaryKeySelective(request);
    }

    public void batchAdd(EnvironmentGroupRequest request) {
        Map<String, String> map = request.getMap();
        if (map == null || map.isEmpty()) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        EnvironmentGroupProjectMapper mapper = sqlSession.getMapper(EnvironmentGroupProjectMapper.class);

        List<String> groupIds = request.getGroupIds();
        List<Project> projects = projectMapper.selectByExample(new ProjectExample());
        List<String> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
        List<ApiTestEnvironment> apiTestEnvironments = apiTestEnvironmentMapper.selectByExample(new ApiTestEnvironmentExample());
        List<String> envIds = apiTestEnvironments.stream().map(ApiTestEnvironment::getId).collect(Collectors.toList());
        for (String groupId : groupIds) {
            EnvironmentGroup group = environmentGroupMapper.selectByPrimaryKey(groupId);
            if (group == null) {
                continue;
            }

            for (Map.Entry<String, String> entry : map.entrySet()) {
                String projectId = entry.getKey();
                String envId = entry.getValue();
                if (!projectIds.contains(projectId) || !envIds.contains(envId)) {
                    continue;
                }

                EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
                example.createCriteria().andEnvironmentGroupIdEqualTo(groupId).andProjectIdEqualTo(projectId);
                List<EnvironmentGroupProject> egpList = environmentGroupProjectMapper.selectByExample(example);
                if (CollectionUtils.isNotEmpty(egpList)) {
                    EnvironmentGroupProject environmentGroupProject = egpList.get(0);
                    environmentGroupProject.setEnvironmentId(envId);
                    mapper.updateByPrimaryKeySelective(environmentGroupProject);
                } else {
                    EnvironmentGroupProject egp = new EnvironmentGroupProject();
                    egp.setId(UUID.randomUUID().toString());
                    egp.setEnvironmentGroupId(groupId);
                    egp.setProjectId(projectId);
                    egp.setEnvironmentId(envId);
                    mapper.insertSelective(egp);
                }
            }
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public List<EnvironmentGroupDTO> getEnvOptionGroup(List<String> projectIds) {
        EnvironmentGroupExample example = new EnvironmentGroupExample();
        example.createCriteria().andWorkspaceIdEqualTo(SessionUtils.getCurrentWorkspaceId());
        List<EnvironmentGroupDTO> result = new ArrayList<>();
        List<EnvironmentGroup> groups = environmentGroupMapper.selectByExample(example);
        List<String> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(projectIds)) {
            List<Project> projects = projectMapper.selectByExample(new ProjectExample());
            List<String> allProjectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
            ids = projectIds.stream().filter(allProjectIds::contains).collect(Collectors.toList());
        }
        for (EnvironmentGroup group : groups) {
            Map<String, String> envMap = baseEnvironmentGroupProjectService.getEnvMap(group.getId());
            EnvironmentGroupDTO dto = new EnvironmentGroupDTO();
            BeanUtils.copyProperties(group, dto);
            if (CollectionUtils.isNotEmpty(ids)) {
                boolean b = envMap.keySet().containsAll(ids);
                if (BooleanUtils.isFalse(b)) {
                    dto.setDisabled(true);
                }
            }
            result.add(dto);
        }
        return result;
    }

    public List<ApiTestEnvironmentWithBLOBs> listByConditions(io.metersphere.request.EnvironmentRequest environmentRequest) {
        if (CollectionUtils.isEmpty(environmentRequest.getProjectIds())) {
            return new ArrayList<>();
        }
        ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
        ApiTestEnvironmentExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdIn(environmentRequest.getProjectIds());
        if (StringUtils.isNotBlank(environmentRequest.getName())) {
            environmentRequest.setName(StringUtils.wrapIfMissing(environmentRequest.getName(), '%'));    //使搜索文本变成数据库中的正则表达式
            criteria.andNameLike(environmentRequest.getName());
        }
        example.setOrderByClause("update_time desc");
        return apiTestEnvironmentMapper.selectByExampleWithBLOBs(example);
    }

    public List<ApiTestEnvironmentWithBLOBs> list(String projectId) {
        ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        example.setOrderByClause("update_time desc");
        return apiTestEnvironmentMapper.selectByExampleWithBLOBs(example);
    }

    public ApiTestEnvironmentWithBLOBs get(String id) {
        return apiTestEnvironmentMapper.selectByPrimaryKey(id);
    }

    public void deleteByResourceId(String sourceId) {
        if (StringUtils.isNotEmpty(sourceId)) {
            FileAssociationExample example = new FileAssociationExample();
            example.createCriteria().andSourceIdEqualTo(sourceId);
            fileAssociationMapper.deleteByExample(example);
        }
    }

    public void saveEnvironment(String id, String config, String type) {
        this.deleteByResourceId(id);
        List<BodyFile> files = new ArrayList<>();
        if (StringUtils.isNotEmpty(config)) {
            Map<String, Object> map = JSON.parseObject(config, Map.class);
            JSONObject commonConfig = new JSONObject(map).optJSONObject("commonConfig");
            if (commonConfig != null) {
                JSONArray variables = commonConfig.optJSONArray("variables");
                if (variables != null) {
                    List<ScenarioVariable> list = JSON.parseArray(variables.toString(), ScenarioVariable.class);
                    list.stream().filter(ScenarioVariable::isCSVValid).forEach(keyValue -> {
                        files.addAll(keyValue.getFiles().stream().filter(BodyFile::isRef).collect(Collectors.toList()));
                    });
                }
            }
        }
        if (!org.springframework.util.CollectionUtils.isEmpty(files)) {
            List<BodyFile> list = files.stream().distinct().collect(Collectors.toList());
            fileAssociationService.save(list, type, id);
        }
    }

    public String add(TestEnvironmentDTO request, List<MultipartFile> sslFiles, List<MultipartFile> variableFile) {
        request.setId(UUID.randomUUID().toString());
        request.setCreateUser(SessionUtils.getUserId());
        checkEnvironmentExist(request);
        FileUtils.createFiles(request.getUploadIds(), sslFiles, FileUtils.BODY_FILE_DIR + "/ssl");
        FileUtils.createBodyFiles(request.getVariablesFilesIds(), variableFile);
        //检查Config，判断isMock参数是否给True
        request = this.updateConfig(request, false);
        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateTime(System.currentTimeMillis());
        apiTestEnvironmentMapper.insert(request);
        // 存储附件关系
        saveEnvironment(request.getId(), request.getConfig(), FileAssociationType.ENVIRONMENT.name());
        checkAndSendReviewMessage(request.getId(),
                request.getName(),
                request.getProjectId(),
                NoticeConstants.TaskType.ENV_TASK,
                null,
                request.getConfig(),
                request.getCreateUser()
                );
        return request.getId();
    }

    public String importEnvironment(List<TestEnvironmentDTO> environments) {
        StringBuilder existNames = new StringBuilder();
        for (TestEnvironmentDTO request : environments) {
            request.setId(UUID.randomUUID().toString());
            request.setCreateUser(SessionUtils.getUserId());
            if (request.getName() != null) {
                if (StringUtils.isBlank(request.getProjectId())) {
                    MSException.throwException(Translator.get("项目ID不能为空"));
                }
                ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
                ApiTestEnvironmentExample.Criteria criteria = example.createCriteria();
                criteria.andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId());
                if (StringUtils.isNotBlank(request.getId())) {
                    criteria.andIdNotEqualTo(request.getId());
                }
                if (apiTestEnvironmentMapper.selectByExample(example).size() > 0) {
                    existNames.append(StringUtils.SPACE).append(request.getName());
                    continue;
                }
            }
            //检查Config，判断isMock参数是否给True
            this.updateConfig(request, false);
            request.setCreateTime(System.currentTimeMillis());
            request.setUpdateTime(System.currentTimeMillis());
            apiTestEnvironmentMapper.insert(request);
            // 存储附件关系
            saveEnvironment(request.getId(), request.getConfig(), FileAssociationType.ENVIRONMENT.name());
        }
        if (existNames.length() > 0) {
            return existNames.toString();
        } else {
            return "OK";
        }
    }

    private TestEnvironmentDTO updateConfig(TestEnvironmentDTO request, boolean isMock) {
        if (StringUtils.isNotEmpty(request.getConfig())) {
            try {
                Map<Object, Object> map = JSON.parseObject(request.getConfig(), Map.class);
                JSONObject configObj = new JSONObject(map);
                if (configObj.has("httpConfig")) {
                    JSONObject httpObj = configObj.getJSONObject("httpConfig");
                    httpObj.put("isMock", isMock);
                }
                request.setConfig(configObj.toString());
            } catch (Exception e) {
                LogUtil.error("设置是否为mock环境出错!参数：" + request.getConfig(), e);
            }
        }
        return request;
    }

    public void checkEnvironmentExist(ApiTestEnvironmentWithBLOBs environment) {
        if (environment.getName() != null) {
            if (StringUtils.isEmpty(environment.getProjectId())) {
                MSException.throwException(Translator.get("项目ID不能为空"));
            }
            ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
            ApiTestEnvironmentExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(environment.getName()).andProjectIdEqualTo(environment.getProjectId());
            if (StringUtils.isNotBlank(environment.getId())) {
                criteria.andIdNotEqualTo(environment.getId());
            }
            if (apiTestEnvironmentMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("api_test_environment_already_exists"));
            }
        }
    }

    public void update(TestEnvironmentDTO apiTestEnvironment, List<MultipartFile> sslFiles, List<MultipartFile> variablesFiles) {
        checkEnvironmentExist(apiTestEnvironment);
        FileUtils.createFiles(apiTestEnvironment.getUploadIds(), sslFiles, FileUtils.BODY_FILE_DIR + "/ssl");
        FileUtils.createBodyFiles(apiTestEnvironment.getVariablesFilesIds(), variablesFiles);
        apiTestEnvironment.setUpdateTime(System.currentTimeMillis());
        // 存储附件关系
        saveEnvironment(apiTestEnvironment.getId(), apiTestEnvironment.getConfig(), FileAssociationType.ENVIRONMENT.name());
        ApiTestEnvironmentWithBLOBs envOrg = apiTestEnvironmentMapper.selectByPrimaryKey(apiTestEnvironment.getId());

        apiTestEnvironmentMapper.updateByPrimaryKeyWithBLOBs(apiTestEnvironment);
        if (StringUtils.isBlank(apiTestEnvironment.getCreateUser())) {
            UserGroupExample example = new UserGroupExample();
            example.createCriteria().andSourceIdEqualTo(apiTestEnvironment.getProjectId()).andGroupIdEqualTo("project_admin");
            List<UserGroup> userGroups = userGroupMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(userGroups)) {
                userGroups.forEach(userGroup -> {
                    checkAndSendReviewMessage(apiTestEnvironment.getId(),
                            apiTestEnvironment.getName(),
                            apiTestEnvironment.getProjectId(),
                            NoticeConstants.TaskType.ENV_TASK,
                            envOrg.getConfig(),
                            apiTestEnvironment.getConfig(),
                            userGroup.getUserId()
                    );
                });
            }
        } else {
            checkAndSendReviewMessage(apiTestEnvironment.getId(),
                    apiTestEnvironment.getName(),
                    apiTestEnvironment.getProjectId(),
                    NoticeConstants.TaskType.ENV_TASK,
                    envOrg.getConfig(),
                    apiTestEnvironment.getConfig(),
                    apiTestEnvironment.getCreateUser()
            );
        }

    }

    public List<ApiModuleDTO> getNodeTreeByProjectId(String projectId, String protocol) {
        EnvironmentRequest request = new EnvironmentRequest();
        List<ApiModuleDTO> apiModules = getApiModulesByProjectAndPro(projectId, protocol);
        request.setProjectId(projectId);
        request.setProtocol(protocol);
        List<String> list = new ArrayList<>();
        list.add("Prepare");
        list.add("Underway");
        list.add("Completed");
        Map<String, List<String>> filters = new LinkedHashMap<>();
        filters.put("status", list);
        request.setFilters(filters);
        return getNodeTrees(apiModules);
    }

    public List<ApiModuleDTO> getApiModulesByProjectAndPro(String projectId, String protocol) {
        return baseApiTestEnvironmentMapper.getNodeTreeByProjectId(projectId, protocol);
    }

    public EnvironmentGroup selectById(String id) {
        return environmentGroupMapper.selectByPrimaryKey(id);
    }

    public List<String> selectNameByIds(Collection<String> envIds) {
        if (CollectionUtils.isNotEmpty(envIds)) {
            return baseApiTestEnvironmentMapper.selectNameByIds(envIds);
        } else {
            return new ArrayList<>(0);
        }
    }

    public List<ApiTestEnvironmentWithBLOBs> selectByExampleWithBLOBs(ApiTestEnvironmentExample example) {
        return apiTestEnvironmentMapper.selectByExampleWithBLOBs(example);
    }

    public String add(ApiTestEnvironmentWithBLOBs apiTestEnvironmentWithBLOBs) {
        apiTestEnvironmentWithBLOBs.setId(UUID.randomUUID().toString());
        checkEnvironmentExist(apiTestEnvironmentWithBLOBs);
        apiTestEnvironmentMapper.insert(apiTestEnvironmentWithBLOBs);
        return apiTestEnvironmentWithBLOBs.getId();
    }

    public void update(ApiTestEnvironmentWithBLOBs apiTestEnvironment) {
        checkEnvironmentExist(apiTestEnvironment);
        apiTestEnvironmentMapper.updateByPrimaryKeyWithBLOBs(apiTestEnvironment);
    }

    public String selectNameById(String id) {
        return baseApiTestEnvironmentMapper.selectNameById(id);
    }

    public ApiTestEnvironmentWithBLOBs getMockEnvironmentByProjectId(String projectId) {

        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String baseUrl = baseSystemConfigDTO.getUrl();
        String protocal = "http";
        if (baseSystemConfigDTO != null && StringUtils.isNotEmpty(baseSystemConfigDTO.getUrl())) {
            baseUrl = baseSystemConfigDTO.getUrl();
            if (baseUrl.startsWith("http:")) {
                protocal = "http";
            } else if (baseUrl.startsWith("https:")) {
                protocal = "https";
            }
        }

        String apiName = MOCK_EVN_NAME;
        ApiTestEnvironmentWithBLOBs returnModel;
        ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(apiName);
        List<ApiTestEnvironmentWithBLOBs> list = this.selectByExampleWithBLOBs(example);

        String projectNumber = this.getSystemIdByProjectId(projectId);
        if (list.isEmpty()) {
            returnModel = this.genHttpApiTestEnvironmentByUrl(projectId, projectNumber, protocal, apiName, baseUrl);
            this.add(returnModel);
        } else {
            returnModel = list.get(0);
            returnModel = this.checkMockEvnIsRightful(returnModel, protocal, projectId, projectNumber, apiName, baseUrl);
        }
        return returnModel;
    }

    private String getSystemIdByProjectId(String projectId) {
        BaseProjectService projectService = CommonBeanFactory.getBean(BaseProjectService.class);
        Project project = projectService.getProjectById(projectId);
        if (project != null) {
            projectService.checkSystemId(project);
            return projectService.getSystemIdByProjectId(projectId);
        } else {
            return "";
        }
    }

    public static JSONObject parseObject(String value) {
        try {
            if (StringUtils.isEmpty(value)) {
                MSException.throwException("value is null");
            }
            Map<String, Object> map = JSON.parseObject(value, Map.class);
            return new JSONObject(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ApiTestEnvironmentWithBLOBs checkMockEvnIsRightful(ApiTestEnvironmentWithBLOBs returnModel, String protocal, String projectId, String projectNumber, String name, String url) {
        boolean needUpdate = false;
        Project project = baseProjectService.getProjectById(projectId);
        if (returnModel.getConfig() != null && project != null) {
            try {
                JSONObject configObj = parseObject(returnModel.getConfig());
                String socket = url;
                if (socket.startsWith("http://")) {
                    socket = socket.substring(7);
                } else if (socket.startsWith("https://")) {
                    socket = socket.substring(8);
                }
                if (configObj.has("httpConfig")) {
                    JSONObject httpObj = configObj.getJSONObject("httpConfig");
                    if (httpObj.has("isMock") && httpObj.getBoolean("isMock")) {
                        if (httpObj.has("conditions")) {
                            JSONArray conditions = httpObj.getJSONArray("conditions");
                            if (conditions == null || conditions.length() == 0) {
                                needUpdate = true;
                            } else {
                                for (int i = 0; i < conditions.length(); i++) {
                                    JSONObject obj = conditions.getJSONObject(i);

                                    if (!obj.has("socket") || !StringUtils.startsWith(String.valueOf(obj.get("socket")), socket)) {
                                        needUpdate = true;
                                        break;
                                    } else if (!obj.has("protocol") || !StringUtils.equals(protocal, String.valueOf(obj.get("protocol")))) {
                                        needUpdate = true;
                                        break;
                                    }

                                    String projectSocket = String.valueOf(obj.get("socket"));
                                    if (!StringUtils.contains(projectSocket, "/api/mock/" + projectNumber)) {
                                        needUpdate = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.MOCK_TCP_PORT.name());
                Integer mockPortStr = config.getMockTcpPort();
                if (mockPortStr != null && mockPortStr != 0) {
                    if (configObj.has("tcpConfig")) {
                        JSONObject tcpConfigObj = configObj.getJSONObject("tcpConfig");
                        if (tcpConfigObj.has("port")) {
                            if (tcpConfigObj.optInt("port") != mockPortStr) {
                                needUpdate = true;
                            }
                        } else {
                            needUpdate = true;
                        }

                        String ipStr = socket;
                        if (socket.contains(":")) {
                            String[] urlArr = socket.split(":");
                            ipStr = urlArr[0];
                        }
                        if (tcpConfigObj.has("server")) {
                            if (!StringUtils.equals(tcpConfigObj.optString("server"), ipStr)) {
                                needUpdate = true;
                            }
                        } else {
                            needUpdate = true;
                        }
                    }
                }
            } catch (Exception e) {
                needUpdate = true;
                LogUtil.error(e);
            }
        }
        if (needUpdate) {
            String id = returnModel.getId();
            returnModel = this.genHttpApiTestEnvironmentByUrl(id, project, projectNumber, protocal, name, url);
            apiTestEnvironmentMapper.updateByPrimaryKeyWithBLOBs(returnModel);
        }
        return returnModel;
    }

    private ApiTestEnvironmentWithBLOBs genHttpApiTestEnvironmentByUrl(String projectId, String projectNumber, String protocal, String name, String baseUrl) {
        Project project = baseProjectService.getProjectById(projectId);
        if (project != null) {
            return this.genHttpApiTestEnvironmentByUrl(null, project, projectNumber, protocal, name, baseUrl);
        }
        return null;
    }

    private ApiTestEnvironmentWithBLOBs genHttpApiTestEnvironmentByUrl(String envId, Project project, String projectNumber, String protocal, String name, String baseUrl) {
        if (project == null) {
            return null;
        }
        String socket = "";
        String url = baseUrl;
        if (url.startsWith("http://")) {
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
        }
        socket = url;
        String tcpSocket = socket;
        if (StringUtils.isNotEmpty(tcpSocket) && tcpSocket.contains(":")) {
            tcpSocket = socket.split(":")[0];
        }

        String portStr = "";
        String ipStr = url;
        if (url.contains(":") && !url.endsWith(":")) {
            String[] urlArr = url.split(":");
            int port = -1;
            try {
                port = Integer.parseInt(urlArr[urlArr.length - 1]);
            } catch (Exception e) {
            }
            if (port > -1) {
                portStr = String.valueOf(port);
                ipStr = urlArr[0];
            }
        }

        JSONObject httpConfig = new JSONObject();
        httpConfig.put("socket", "");
        httpConfig.put("isMock", true);
        httpConfig.put("domain", "");
        List<Map<String, Object>> httpVariablesArr = new LinkedList<>();
        Map<String, Object> httpMap = new HashMap<>();
        httpMap.put("enable", true);
        httpVariablesArr.add(httpMap);
        httpConfig.put("headers", new JSONArray(httpVariablesArr));
        httpConfig.put("protocol", "");
        httpConfig.put("port", "");
        List<JSONObject> httpItemArr = new LinkedList<>();
        JSONObject httpItem = new JSONObject();
        httpItem.put("id", UUID.randomUUID().toString());
        httpItem.put("type", "NONE");
        httpItem.put("socket", socket + "/api/mock/" + projectNumber);
        httpItem.put("protocol", protocal);
        List<Map<String, Object>> protocolVariablesArr = new LinkedList<>();
        Map<String, Object> protocolMap = new HashMap<>();
        protocolMap.put("enable", true);
        protocolVariablesArr.add(protocolMap);
        httpItem.put("headers", new JSONArray(protocolVariablesArr));
        httpItem.put("domain", ipStr);
        if (StringUtils.isNotEmpty(portStr)) {
            httpItem.put("port", portStr);
        } else {
            httpItem.put("port", "");
        }
        List<JSONObject> detailArr = new LinkedList<>();
        JSONObject detailObj = new JSONObject();
        detailObj.put("name", "");
        detailObj.put("value", "contains");
        detailObj.put("enable", true);
        detailArr.add(detailObj);
        httpItem.put("details", new JSONArray(detailArr));

        httpItemArr.add(httpItem);
        httpConfig.put("conditions", new JSONArray(httpItemArr));
        httpConfig.put("defaultCondition", "NONE");

        JSONObject tcpConfigObj = new JSONObject();
        tcpConfigObj.put("classname", "TCPClientImpl");
        tcpConfigObj.put("reUseConnection", false);
        tcpConfigObj.put("nodelay", false);
        tcpConfigObj.put("closeConnection", false);
        if (project != null) {
            ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.MOCK_TCP_PORT.name());
            Integer mockPort = config.getMockTcpPort();
            if (mockPort != null && mockPort != 0) {
                tcpConfigObj.put("server", tcpSocket);
                tcpConfigObj.put("port", mockPort);
            }
        }

        ApiTestEnvironmentWithBLOBs blobs = null;
        if (StringUtils.isNotEmpty(envId)) {
            blobs = this.get(envId);
        }
        if (blobs != null && StringUtils.isNotEmpty(blobs.getConfig())) {
            JSONObject object = parseObject(blobs.getConfig());
            object.put("httpConfig", httpConfig);
            object.put("tcpConfig", tcpConfigObj);
            blobs.setConfig(object.toString());
        } else {
            blobs = new ApiTestEnvironmentWithBLOBs();
            JSONObject commonConfigObj = new JSONObject();
            List<Map<String, Object>> commonVariablesArr = new LinkedList<>();
            Map<String, Object> commonMap = new HashMap<>();
            commonMap.put("enable", true);
            commonVariablesArr.add(commonMap);
            commonConfigObj.put("variables", new JSONArray(commonVariablesArr));
            commonConfigObj.put("enableHost", false);
            commonConfigObj.put("hosts", new String[]{});

            JSONArray databaseConfigObj = new JSONArray();

            JSONObject object = new JSONObject();
            object.put("commonConfig", commonConfigObj);
            object.put("httpConfig", httpConfig);
            object.put("databaseConfigs", databaseConfigObj);
            object.put("tcpConfig", tcpConfigObj);
            blobs.setConfig(object.toString());
        }
        blobs.setProjectId(project.getId());
        blobs.setName(name);

        return blobs;
    }

    public void checkMockEvnInfoByBaseUrl(String baseUrl) {
        ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
        example.createCriteria().andNameEqualTo(MOCK_EVN_NAME);
        long mockEnvCount = apiTestEnvironmentMapper.countByExample(example);
        BatchProcessingUtil.batchUpdateMockEnvConfig(baseUrl, mockEnvCount, this::updateMockEvnInfoByBaseUrlAndLimitNum);
    }

    private void updateMockEvnInfoByBaseUrlAndLimitNum(ApiMockEnvUpdateDTO mockEnvUpdateDTO) {
        if (StringUtils.isNotEmpty(mockEnvUpdateDTO.getBaseUrl())) {
            List<ApiTestEnvironmentWithBLOBs> allEvnList = extApiTestEnvironmentMapper.selectByNameAndLimitNum(
                    MOCK_EVN_NAME, mockEnvUpdateDTO.getLimitStart(), mockEnvUpdateDTO.getLimitSize());
            List<ApiTestEnvironmentWithBLOBs> updateList = new ArrayList<>();
            for (ApiTestEnvironmentWithBLOBs model : allEvnList) {
                if (StringUtils.equals(model.getName(), MOCK_EVN_NAME)) {
                    String protocal = "";
                    if (mockEnvUpdateDTO.getBaseUrl().startsWith("http:")) {
                        protocal = "http";
                    } else if (mockEnvUpdateDTO.getBaseUrl().startsWith("https:")) {
                        protocal = "https";
                    }
                    ApiTestEnvironmentWithBLOBs updateModel = this.updateMockEvn(model, protocal, mockEnvUpdateDTO.getBaseUrl());
                    updateList.add(updateModel);
                }
            }
            if (CollectionUtils.isNotEmpty(updateList)) {
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                ApiTestEnvironmentMapper batchMapper = sqlSession.getMapper(ApiTestEnvironmentMapper.class);

                for (ApiTestEnvironmentWithBLOBs updateModel : updateList) {
                    batchMapper.updateByPrimaryKeyWithBLOBs(updateModel);
                }
                sqlSession.flushStatements();
                if (sqlSession != null && sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }

        }
    }

    private ApiTestEnvironmentWithBLOBs updateMockEvn(ApiTestEnvironmentWithBLOBs mockEnv, String protocol, String newUrl) {
        if (mockEnv.getConfig() != null) {
            try {
                JSONObject configObj = parseObject(mockEnv.getConfig());
                if (configObj.has("httpConfig")) {
                    JSONObject httpObj = configObj.getJSONObject("httpConfig");
                    JSONObject httpObject = this.genHttpMockConfig(httpObj, newUrl, protocol);
                    configObj.put("httpConfig", httpObject);
                }
                if (configObj.has("tcpConfig")) {
                    JSONObject tcpObj = configObj.getJSONObject("tcpConfig");
                    JSONObject tcpObject = this.genTcpMockConfig(tcpObj, newUrl);
                    configObj.put("tcpConfig", tcpObject);
                }
                mockEnv.setConfig(configObj.toString());
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        return mockEnv;
    }

    private JSONObject genTcpMockConfig(@NotNull JSONObject tcpObj, String newUrl) {
        if (tcpObj.has("server")) {
            String url = newUrl;
            if (url.startsWith("http://")) {
                url = url.substring(7);
            } else if (url.startsWith("https://")) {
                url = url.substring(8);
            }
            String ipStr = url;
            if (url.contains(":") && !url.endsWith(":")) {
                String[] urlArr = url.split(":");
                int port = -1;
                try {
                    port = Integer.parseInt(urlArr[urlArr.length - 1]);
                } catch (Exception e) {
                }
                if (port > -1) {
                    ipStr = urlArr[0];
                }
            }
            tcpObj.put("server", ipStr);
        }
        return tcpObj;
    }

    private JSONObject genHttpMockConfig(@NotNull JSONObject httpObj, String newUrl, String protocol) {
        if (httpObj.has("isMock") && httpObj.getBoolean("isMock")) {
            if (httpObj.has("conditions")) {
                String url = newUrl;
                if (url.startsWith("http://")) {
                    url = url.substring(7);
                } else if (url.startsWith("https://")) {
                    url = url.substring(8);
                }
                String ipStr = url;
                String portStr = "";
                if (url.contains(":") && !url.endsWith(":")) {
                    String[] urlArr = url.split(":");
                    int port = -1;
                    try {
                        port = Integer.parseInt(urlArr[urlArr.length - 1]);
                    } catch (Exception e) {
                    }
                    if (port > -1) {
                        portStr = String.valueOf(port);
                        ipStr = urlArr[0];
                    }
                }
                if (httpObj.has("socket") && httpObj.has("protocol") && httpObj.has("port")) {
                    String httpSocket = httpObj.optString("socket");
                    if (httpSocket.contains("/api/mock")) {
                        String[] httpSocketArr = StringUtils.split(httpSocket, "/api/mock");
                        httpSocket = StringUtils.join(url, "/api/mock", httpSocketArr[1]);
                    }
                    httpObj.put("socket", httpSocket);
                    httpObj.put("protocol", protocol);
                    if (StringUtils.isNotEmpty(portStr)) {
                        httpObj.put("port", portStr);
                    } else {
                        httpObj.put("port", "");
                    }
                }

                JSONArray conditions = httpObj.getJSONArray("conditions");
                for (int i = 0; i < conditions.length(); i++) {
                    JSONObject httpItem = conditions.getJSONObject(i);
                    if (httpItem.has("socket") && httpItem.has("protocol") && httpItem.has("domain")) {
                        String httpSocket = httpItem.optString("socket");
                        if (httpSocket.contains("/api/mock")) {
                            String[] httpSocketArr = httpSocket.split("/api/mock");
                            httpSocket = StringUtils.join(url, "/api/mock", httpSocketArr[1]);
                        }
                        httpItem.put("socket", httpSocket);
                        httpItem.put("domain", ipStr);
                        httpItem.put("protocol", protocol);
                        if (StringUtils.isNotEmpty(portStr)) {
                            httpItem.put("port", portStr);
                        } else {
                            httpItem.put("port", "");
                        }
                    }
                }
            }
        }
        return httpObj;
    }

    public LinkedHashMap<String, List<String>> selectProjectNameAndEnvName(Map<String, List<String>> projectEnvIdMap) {
        LinkedHashMap<String, List<String>> returnMap = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(projectEnvIdMap)) {
            for (Map.Entry<String, List<String>> entry : projectEnvIdMap.entrySet()) {
                String projectId = entry.getKey();
                List<String> envIdList = entry.getValue();
                Project project = baseProjectService.getProjectById(projectId);
                if (project != null) {
                    String projectName = project.getName();
                    List<String> envNameList = this.selectNameByIds(envIdList);
                    if (CollectionUtils.isNotEmpty(envNameList) && StringUtils.isNotEmpty(projectName)) {
                        returnMap.put(projectName, new ArrayList<>() {{
                            this.addAll(envNameList);
                        }});
                    }
                }
            }
        }
        return returnMap;
    }

    public List<String> selectNameByIdList(List<String> envIdList) {
        if (CollectionUtils.isNotEmpty(envIdList)) {
            return baseApiTestEnvironmentMapper.selectNameByIdList(envIdList);
        } else {
            return new ArrayList<>();
        }
    }

    @Async
    public void checkAndSendReviewMessage(
            String id,
            String name,
            String projectId,
            String resourceType,
            String requestOrg,
            String requestTarget,
            String sendUser) {
        try {
            ProjectApplication scriptEnable = baseProjectApplicationService
                    .getProjectApplication(projectId, ProjectApplicationType.API_REVIEW_TEST_SCRIPT.name());

            if (BooleanUtils.toBoolean(scriptEnable.getTypeValue())) {

                List<String> org = scriptList(requestOrg);
                List<String> target = scriptList(requestTarget);
                boolean isSend = isSend(org, target);
                if (isSend) {

                    ProjectApplication reviewer = baseProjectApplicationService
                            .getProjectApplication(projectId, ProjectApplicationType.API_SCRIPT_REVIEWER.name());
                    if (StringUtils.isNotBlank(reviewer.getTypeValue())) {
                        sendUser = reviewer.getTypeValue();
                    }
                    if (baseProjectService.isProjectMember(projectId, sendUser)) {
                        Notification notification = new Notification();
                        notification.setTitle("环境设置");
                        notification.setOperator(reviewer.getTypeValue());
                        notification.setOperation(NoticeConstants.Event.REVIEW);
                        notification.setResourceId(id);
                        notification.setResourceName(name);
                        notification.setResourceType(resourceType);
                        notification.setType(NotificationConstants.Type.SYSTEM_NOTICE.name());
                        notification.setStatus(NotificationConstants.Status.UNREAD.name());
                        notification.setCreateTime(System.currentTimeMillis());
                        notification.setReceiver(sendUser);
                        notificationService.sendAnnouncement(notification);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("发送通知失败", e);
        }
    }

    public static List<String> scriptList(String request) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isNotBlank(request)){
            Map<Object, Object> configMap = JSON.parseObject(request, Map.class);
            JSONObject configObj = new JSONObject(configMap);
            toList(list, configObj, POST_STEP, PRE_STEP);
            toList(list, configObj, PRE, POST);
            JSONObject object = configObj.optJSONObject(ASSERTIONS);
            if (ObjectUtils.isNotEmpty(object)) {
                JSONArray jsrArray = object.optJSONArray(JSR);
                if (jsrArray != null) {
                    for (int j = 0; j < jsrArray.length(); j++) {
                        JSONObject jsr223 = jsrArray.optJSONObject(j);
                        if (jsr223 != null) {
                            list.add(jsr223.optString(SCRIPT));
                        }
                    }
                }
            }
        }
        return list;
    }

    private static void toList(List<String> list, JSONObject configObj, String pre, String post) {
        JSONObject preProcessor = configObj.optJSONObject(pre);
        if (ObjectUtils.isNotEmpty(preProcessor) && StringUtils.isNotBlank(preProcessor.optString(SCRIPT))) {
            list.add(StringUtils.join(pre,preProcessor.optString(SCRIPT)));
        }
        JSONObject postProcessor = configObj.optJSONObject(post);
        if (ObjectUtils.isNotEmpty(postProcessor) && StringUtils.isNotBlank(postProcessor.optString(SCRIPT))) {
            list.add(StringUtils.join(post,postProcessor.optString(SCRIPT)));
        }
    }

    public static boolean isSend(List<String> orgList, List<String> targetList) {
        if (orgList.size() != targetList.size() && CollectionUtils.isEmpty(orgList)) {
            return true;
        }
        if (orgList.size() != targetList.size() && CollectionUtils.isEmpty(targetList)) {
            return false;
        }
        List<String> diff = orgList.stream()
                .filter(s -> !targetList.contains(s))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(diff)) {
            return true;
        }
        return false;
    }
}
