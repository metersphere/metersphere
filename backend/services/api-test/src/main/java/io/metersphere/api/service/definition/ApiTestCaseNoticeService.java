package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.domain.ApiTestCaseExample;
import io.metersphere.api.dto.definition.ApiCaseBatchSyncRequest;
import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.dto.definition.ApiTestCaseUpdateRequest;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.ApiDefinitionCaseDTO;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.CommonNoticeSendService;
import io.metersphere.system.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiTestCaseNoticeService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private SystemParameterService systemParameterService;

    public ApiDefinitionCaseDTO addCaseDto(ApiTestCaseAddRequest request) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        BeanUtils.copyBean(caseDTO, request);
        return caseDTO;
    }

    public ApiDefinitionCaseDTO getCaseDTO(ApiTestCaseUpdateRequest request) {
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(request.getId());
        BeanUtils.copyBean(caseDTO, testCase);
        return caseDTO;
    }

    public ApiDefinitionCaseDTO getCaseDTO(String id) {
        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(id);
        ApiDefinitionCaseDTO caseDTO = new ApiDefinitionCaseDTO();
        BeanUtils.copyBean(caseDTO, testCase);
        return caseDTO;
    }

    public void batchSendNotice(List<String> ids, String userId, String projectId, String event) {
        if (CollectionUtils.isNotEmpty(ids)) {
            User user = userMapper.selectByPrimaryKey(userId);
            SubListUtils.dealForSubList(ids, 100, (subList) -> {
                ApiTestCaseExample example = new ApiTestCaseExample();
                example.createCriteria().andIdIn(subList);
                List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
                List<ApiDefinitionCaseDTO> noticeLists = apiTestCases.stream()
                        .map(apiTestCase -> {
                            ApiDefinitionCaseDTO apiDefinitionCaseDTO = new ApiDefinitionCaseDTO();
                            BeanUtils.copyBean(apiDefinitionCaseDTO, apiTestCase);
                            return apiDefinitionCaseDTO;
                        })
                        .toList();
                List<Map> resources = new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeLists), Map.class));
                commonNoticeSendService.sendNotice(NoticeConstants.TaskType.API_DEFINITION_TASK, event, resources, user, projectId);
            });
        }
    }

    public void batchSyncSendNotice(List<ApiTestCase> apiTestCases, User user, String projectId,
                                    ApiCaseBatchSyncRequest.ApiCaseSyncNotificationRequest notificationConfig, String event) {

        if (CollectionUtils.isEmpty(apiTestCases)) {
            return;
        }

        Map<String, Set<String>> caseRefApiScenarioCreatorMap = null;
        if (BooleanUtils.isTrue(notificationConfig.getScenarioCreator())) {
            List<String> caseIds = apiTestCases.stream().map(ApiTestCase::getId).toList();
            // 获取引用该用例的场景的创建人信息
            List<ApiTestCase> caseRefApiScenarioCreators = extApiTestCaseMapper.getRefApiScenarioCreator(caseIds);
            // 构建用例和创建人的映射关系
            caseRefApiScenarioCreatorMap = caseRefApiScenarioCreators.stream().collect(Collectors.groupingBy(ApiTestCase::getId,
                    Collectors.mapping(ApiTestCase::getCreateUser, Collectors.toSet())));
        }

        List<ApiDefinitionCaseDTO> noticeLists = apiTestCases.stream()
                .map(apiTestCase -> {
                    ApiDefinitionCaseDTO apiDefinitionCaseDTO = new ApiDefinitionCaseDTO();
                    BeanUtils.copyBean(apiDefinitionCaseDTO, apiTestCase);
                    return apiDefinitionCaseDTO;
                })
                .toList();
        List<Map> resources = new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeLists), Map.class));

        if (BooleanUtils.isTrue(notificationConfig.getScenarioCreator()) || BooleanUtils.isTrue(notificationConfig.getApiCaseCreator())) {
            for (Map resource : resources) {
                String caseId = (String) resource.get("id");
                List<String> extraUsers = new ArrayList<>();
                if (BooleanUtils.isTrue(notificationConfig.getScenarioCreator())) {
                    // 添加引用该用例的场景的创建人
                    Set<String> userIds = Optional.ofNullable(caseRefApiScenarioCreatorMap.get(caseId)).orElse(new HashSet<>(1));
                    extraUsers.addAll(userIds);
                }

                if (BooleanUtils.isTrue(notificationConfig.getApiCaseCreator())) {
                    // 添加用例创建人
                    String createUser = (String) resource.get("createUser");
                    extraUsers.add(createUser);
                }
                BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
                commonNoticeSendService.sendNotice(NoticeConstants.TaskType.API_DEFINITION_TASK, event, resource, user, projectId,
                        baseSystemConfigDTO, extraUsers, true);
            }
        }
    }
}
