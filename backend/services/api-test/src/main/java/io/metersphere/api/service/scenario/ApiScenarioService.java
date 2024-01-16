package io.metersphere.api.service.scenario;

import com.alibaba.excel.util.BooleanUtils;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.scenario.ApiScenarioBatchEditRequest;
import io.metersphere.api.dto.scenario.ApiScenarioDTO;
import io.metersphere.api.dto.scenario.ApiScenarioPageRequest;
import io.metersphere.api.mapper.*;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.domain.EnvironmentGroup;
import io.metersphere.sdk.domain.EnvironmentGroupExample;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.service.UserLoginService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioService {

    public static final Long ORDER_STEP = 5000L;

    @Resource
    private ApiScenarioMapper apiScenarioMapper;

    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;

    @Resource
    private UserLoginService userLoginService;
    @Resource
    private ApiScenarioEnvironmentMapper apiScenarioEnvironmentMapper;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    @Resource
    private ApiScenarioLogService apiScenarioLogService;
    @Resource
    private ApiScenarioFollowerMapper apiScenarioFollowerMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    public static final String PRIORITY = "Priority";
    public static final String STATUS = "Status";
    public static final String TAGS = "Tags";
    public static final String ENVIRONMENT = "Environment";


    public List<ApiScenarioDTO> getScenarioPage(ApiScenarioPageRequest request) {
        //CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(request, userId);
        //TODO  场景的自定义字段 等设计 不一定会有
        List<ApiScenarioDTO> list = extApiScenarioMapper.list(request);
        if (!CollectionUtils.isEmpty(list)) {
            processApiScenario(list);
        }
        return list;
    }

    private void processApiScenario(List<ApiScenarioDTO> scenarioLists) {
        Set<String> userIds = extractUserIds(scenarioLists);
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userIds));
        //取出所有的apiId
        List<String> scenarioIds = scenarioLists.stream().map(ApiScenarioDTO::getId).distinct().toList();
        ApiScenarioEnvironmentExample scenarioEnvironmentExample = new ApiScenarioEnvironmentExample();
        scenarioEnvironmentExample.createCriteria().andApiScenarioIdIn(scenarioIds);
        List<ApiScenarioEnvironment> apiScenarioEnvironments = apiScenarioEnvironmentMapper.selectByExample(scenarioEnvironmentExample);
        //生成map key为id value为ApiScenarioEnvironment
        Map<String, ApiScenarioEnvironment> environmentMap = apiScenarioEnvironments.stream().collect(Collectors.toMap(ApiScenarioEnvironment::getApiScenarioId, item -> item));
        List<String> envIds = apiScenarioEnvironments.stream().map(ApiScenarioEnvironment::getEnvironmentId).toList();
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andIdIn(envIds);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        Map<String, String> envMap = environments.stream().collect(Collectors.toMap(Environment::getId, Environment::getName));
        List<String> envGroupIds = apiScenarioEnvironments.stream().map(ApiScenarioEnvironment::getEnvironmentGroupId).toList();
        EnvironmentGroupExample groupExample = new EnvironmentGroupExample();
        groupExample.createCriteria().andIdIn(envGroupIds);
        List<EnvironmentGroup> environmentGroups = environmentGroupMapper.selectByExample(groupExample);
        Map<String, String> groupMap = environmentGroups.stream().collect(Collectors.toMap(EnvironmentGroup::getId, EnvironmentGroup::getName));
        //取模块id为新的set
        List<String> moduleIds = scenarioLists.stream().map(ApiScenarioDTO::getModuleId).distinct().toList();
        ApiScenarioModuleExample moduleExample = new ApiScenarioModuleExample();
        moduleExample.createCriteria().andIdIn(moduleIds);
        List<ApiScenarioModule> modules = apiScenarioModuleMapper.selectByExample(moduleExample);
        //生成map key为id value为name
        Map<String, String> moduleMap = modules.stream().collect(Collectors.toMap(ApiScenarioModule::getId, ApiScenarioModule::getName));
        scenarioLists.forEach(item -> {
            item.setCreateUserName(userMap.get(item.getCreateUser()));
            item.setDeleteUserName(userMap.get(item.getDeleteUser()));
            item.setUpdateUserName(userMap.get(item.getUpdateUser()));
            item.setModulePath(StringUtils.isNotBlank(moduleMap.get(item.getModuleId())) ? moduleMap.get(item.getModuleId()) : Translator.get("api_unplanned_scenario"));
            if (!item.getGrouped() && environmentMap.containsKey(item.getId()) &&
                    StringUtils.isNotBlank(environmentMap.get(item.getId()).getEnvironmentId()) &&
                    envMap.containsKey(environmentMap.get(item.getId()).getEnvironmentId())) {
                item.setEnvironmentName(environmentMap.get(item.getId()).getEnvironmentId());
            } else if (item.getGrouped() && environmentMap.containsKey(item.getId()) &&
                    StringUtils.isNotBlank(environmentMap.get(item.getId()).getEnvironmentGroupId()) &&
                    groupMap.containsKey(environmentMap.get(item.getId()).getEnvironmentGroupId())) {
                item.setEnvironmentName(groupMap.get(item.getId()));
            }
        });
    }

    private Set<String> extractUserIds(List<ApiScenarioDTO> list) {
        return list.stream()
                .flatMap(apiDefinition -> Stream.of(apiDefinition.getUpdateUser(), apiDefinition.getDeleteUser(), apiDefinition.getCreateUser()))
                .collect(Collectors.toSet());
    }

    public void batchEdit(ApiScenarioBatchEditRequest request, String userId) {
        List<String> ids = doSelectIds(request, false);
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        SubListUtils.dealForSubList(ids, 500, subList -> batchEditByType(request, subList, userId, request.getProjectId()));
    }

    private void batchEditByType(ApiScenarioBatchEditRequest request, List<String> ids, String userId, String projectId) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        ApiScenario updateScenario = new ApiScenario();
        updateScenario.setUpdateUser(userId);
        updateScenario.setUpdateTime(System.currentTimeMillis());
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioMapper mapper = sqlSession.getMapper(ApiScenarioMapper.class);

        switch (request.getType()) {
            case PRIORITY -> batchUpdatePriority(example, updateScenario, request.getPriority());
            case STATUS -> batchUpdateStatus(example, updateScenario, request.getStatus());
            case TAGS -> batchUpdateTags(example, updateScenario, request, ids, sqlSession, mapper);
            case ENVIRONMENT -> batchUpdateEnvironment(example, updateScenario, request, ids);
            default -> throw new MSException(Translator.get("batch_edit_type_error"));
        }
        List<ApiScenario> scenarioInfoByIds = extApiScenarioMapper.getInfoByIds(ids, false);
        apiScenarioLogService.batchEditLog(scenarioInfoByIds, userId, projectId);
    }

    private void batchUpdateEnvironment(ApiScenarioExample example, ApiScenario updateScenario,
                                        ApiScenarioBatchEditRequest request,
                                        List<String> ids) {
        if (BooleanUtils.isFalse(request.isGrouped())) {
            if (StringUtils.isBlank(request.getEnvId())) {
                throw new MSException(Translator.get("environment_id_is_null"));
            }
            Environment environment = environmentMapper.selectByPrimaryKey(request.getEnvId());
            if (environment == null) {
                throw new MSException(Translator.get("environment_is_not_exist"));
            }
            updateScenario.setGrouped(false);
            ApiScenarioEnvironment apiScenarioEnvironment = new ApiScenarioEnvironment();
            apiScenarioEnvironment.setEnvironmentId(request.getEnvId());
            ApiScenarioEnvironmentExample environmentExample = new ApiScenarioEnvironmentExample();
            environmentExample.createCriteria().andApiScenarioIdIn(ids);
            apiScenarioEnvironmentMapper.updateByExampleSelective(apiScenarioEnvironment, environmentExample);
        } else {
            if (StringUtils.isBlank(request.getGroupId())) {
                throw new MSException(Translator.get("environment_group_id_is_null"));
            }
            EnvironmentGroup environmentGroup = environmentGroupMapper.selectByPrimaryKey(request.getGroupId());
            if (environmentGroup == null) {
                throw new MSException(Translator.get("environment_group_is_not_exist"));
            }
            updateScenario.setGrouped(true);
            ApiScenarioEnvironment apiScenarioEnvironment = new ApiScenarioEnvironment();
            apiScenarioEnvironment.setEnvironmentGroupId(request.getGroupId());
            ApiScenarioEnvironmentExample environmentExample = new ApiScenarioEnvironmentExample();
            environmentExample.createCriteria().andApiScenarioIdIn(ids);
            apiScenarioEnvironmentMapper.updateByExampleSelective(apiScenarioEnvironment, environmentExample);
        }
        apiScenarioMapper.updateByExampleSelective(updateScenario, example);

    }

    private void batchUpdateTags(ApiScenarioExample example, ApiScenario updateScenario,
                                 ApiScenarioBatchEditRequest request, List<String> ids,
                                 SqlSession sqlSession, ApiScenarioMapper mapper) {
        if (CollectionUtils.isEmpty(request.getTags())) {
            throw new MSException(Translator.get("tags_is_null"));
        }
        if (request.isAppendTag()) {
            Map<String, ApiScenario> scenarioMap = extApiScenarioMapper.getTagsByIds(ids, false)
                    .stream()
                    .collect(Collectors.toMap(ApiScenario::getId, Function.identity()));
            if (MapUtils.isNotEmpty(scenarioMap)) {
                scenarioMap.forEach((k, v) -> {
                    if (CollectionUtils.isNotEmpty(v.getTags())) {
                        List<String> orgTags = v.getTags();
                        orgTags.addAll(request.getTags());
                        v.setTags(orgTags.stream().distinct().toList());
                    } else {
                        v.setTags(request.getTags());
                    }
                    v.setUpdateTime(updateScenario.getUpdateTime());
                    v.setUpdateUser(updateScenario.getUpdateUser());
                    mapper.updateByPrimaryKeySelective(v);
                });
                sqlSession.flushStatements();
                if (sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        } else {
            updateScenario.setTags(request.getTags());
            apiScenarioMapper.updateByExampleSelective(updateScenario, example);
        }
    }

    private void batchUpdateStatus(ApiScenarioExample example, ApiScenario updateScenario, String status) {
        if (StringUtils.isBlank(status)) {
            throw new MSException(Translator.get("status_is_null"));
        }
        updateScenario.setStatus(status);
        apiScenarioMapper.updateByExampleSelective(updateScenario, example);
    }

    private void batchUpdatePriority(ApiScenarioExample example, ApiScenario updateScenario, String priority) {
        if (StringUtils.isBlank(priority)) {
            throw new MSException(Translator.get("priority_is_null"));
        }
        updateScenario.setPriority(priority);
        apiScenarioMapper.updateByExampleSelective(updateScenario, example);
    }

    public List<String> doSelectIds(ApiScenarioBatchEditRequest request, boolean deleted) {
        if (request.isSelectAll()) {
            List<String> ids = extApiScenarioMapper.getIds(request, deleted);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    public void follow(String id, String userId) {
        checkApiScenario(id);
        ApiScenarioFollowerExample example = new ApiScenarioFollowerExample();
        example.createCriteria().andApiScenarioIdEqualTo(id).andUserIdEqualTo(userId);
        if (apiScenarioFollowerMapper.countByExample(example) > 0) {
            apiScenarioFollowerMapper.deleteByPrimaryKey(id, userId);
            apiScenarioLogService.unfollowLog(id, userId);
        } else {
            ApiScenarioFollower apiScenarioFollower = new ApiScenarioFollower();
            apiScenarioFollower.setApiScenarioId(id);
            apiScenarioFollower.setUserId(userId);
            apiScenarioFollowerMapper.insertSelective(apiScenarioFollower);
            apiScenarioLogService.followLog(id, userId);
        }
    }

    private ApiScenario checkApiScenario(String id) {
        ApiScenario apiScenario = apiScenarioMapper.selectByPrimaryKey(id);
        if (apiScenario == null) {
            throw new MSException(Translator.get("api_scenario_is_not_exist"));
        }
        return apiScenario;
    }
}
