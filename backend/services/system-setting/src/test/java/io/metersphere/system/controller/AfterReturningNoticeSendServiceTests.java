package io.metersphere.system.controller;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.domain.FunctionalCaseCustomFieldExample;
import io.metersphere.functional.mapper.CaseReviewMapper;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.load.domain.LoadTest;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.dto.FunctionalCaseDTO;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.sender.AfterReturningNoticeSendService;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.*;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class AfterReturningNoticeSendServiceTests extends BaseTest {


    @Resource
    private AfterReturningNoticeSendService afterReturningNoticeSendService;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private CustomFieldMapper customFieldMapper;
    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private CaseReviewMapper caseReviewMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private SystemParameterService systemParameterService;

    private ThreadLocal<String> source = new ThreadLocal<>();

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_aspect.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void noticeSuccess() {
        String functionalCaseTask = NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK;

        List<String>eventList = new ArrayList<>();
        getTypeList(eventList);
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey("aspect_gyq_one");
        FunctionalCaseCustomFieldExample functionalCaseCustomFieldExample = new FunctionalCaseCustomFieldExample();
        functionalCaseCustomFieldExample.createCriteria().andCaseIdEqualTo("aspect_gyq_one");
        List<FunctionalCaseCustomField> functionalCaseCustomFields = functionalCaseCustomFieldMapper.selectByExample(functionalCaseCustomFieldExample);
        String fieldId = functionalCaseCustomFields.get(0).getFieldId();
        CustomField customFields = customFieldMapper.selectByPrimaryKey(fieldId);

        List<OptionDTO>optionDTOList = new ArrayList<>();
        OptionDTO optionDTO = new OptionDTO();
        optionDTO.setId(customFields.getName());
        optionDTO.setName(functionalCaseCustomFields.get(0).getValue());
        optionDTOList.add(optionDTO);
        FunctionalCaseDTO functionalCaseDTO = new FunctionalCaseDTO();
        BeanUtils.copyBean(functionalCaseDTO,functionalCase);
        functionalCaseDTO.setRelatedUsers("aspect-member-user-guo");
        functionalCaseDTO.setFields(optionDTOList);

        String jsonObject = JSON.toJSONString(functionalCaseDTO);
        if (!StringUtils.equals("{}", jsonObject) && !StringUtils.equals("[]", jsonObject)) {
            source.set(JSON.toJSONString(functionalCaseDTO));
        }

        List<Map> resources = new ArrayList<>();
        String v = source.get();
        if (StringUtils.isNotBlank(v)) {
            // array
            if (StringUtils.startsWith(v, "[")) {
                resources.addAll(JSON.parseArray(v, Map.class));
            }
            // map
            else {
                Map<?, ?> value = JSON.parseObject(v, Map.class);
                resources.add(value);
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(sessionId);
        userDTO.setName("admin");
        SessionUser user = SessionUser.fromUser(userDTO, sessionId);

        for (String event : eventList) {
            afterReturningNoticeSendService.sendNotice(functionalCaseTask, event,resources, user, "100001100001");
        }
        source = new ThreadLocal<>();
        FunctionalCase functionalCaseTwo = functionalCaseMapper.selectByPrimaryKey("aspect_gyq_three");
        functionalCaseDTO = new FunctionalCaseDTO();
        BeanUtils.copyBean(functionalCaseDTO,functionalCaseTwo);
        jsonObject = JSON.toJSONString(functionalCaseDTO);
        if (!StringUtils.equals("{}", jsonObject) && !StringUtils.equals("[]", jsonObject)) {
            source.set(JSON.toJSONString(functionalCaseDTO));
        }

        resources = new ArrayList<>();
        v = source.get();
        if (StringUtils.isNotBlank(v)) {
            // array
            if (StringUtils.startsWith(v, "[")) {
                resources.addAll(JSON.parseArray(v, Map.class));
            }
            // map
            else {
                Map<?, ?> value = JSON.parseObject(v, Map.class);
                resources.add(value);
            }
        }
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        for (Map resource : resources) {
            Map paramMap = new HashMap<>();
            paramMap.put("url", baseSystemConfigDTO.getUrl());
            paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user.getName());
            paramMap.putAll(resource);
            paramMap.putIfAbsent("projectId", "aspect_gyq_project_one");
            //TODO: 加来源处理

            // 占位符
            paramMap.put("planShareUrl", StringUtils.EMPTY);

            Project project = projectMapper.selectByPrimaryKey("aspect_gyq_project_one");
            Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
            String context = defaultTemplateMap.get(functionalCaseTask + "_UPDATE");
            Map<String, String> defaultTemplateTitleMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
            String subject = defaultTemplateTitleMap.get(functionalCaseTask + "_UPDATE");

            List<String> relatedUsers = getRelatedUsers(resource.get("relatedUsers"));

            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(user.getId())
                    .context(context)
                    .subject(subject)
                    .paramMap(paramMap)
                    .event("UPDATE")
                    .status((String) paramMap.get("status"))
                    .excludeSelf(true)
                    .relatedUsers(relatedUsers)
                    .build();
            noticeSendService.send(project,functionalCaseTask,noticeModel);
        }
    }

    private List<String> getRelatedUsers(Object relatedUsers) {
        String relatedUser = (String) relatedUsers;
        List<String> relatedUserList = new ArrayList<>();
        if (StringUtils.isNotBlank(relatedUser)) {
            relatedUserList = Arrays.asList(relatedUser.split(";"));
        }
        return relatedUserList;
    }

    @Test
    @Order(2)
    public void ApiNoticeSuccess() {
        String apiDefinitionTask = NoticeConstants.TaskType.API_DEFINITION_TASK;

        List<String>eventList = new ArrayList<>();
        getTypeList(eventList);
        ApiDefinition aspectGyqApiOne = apiDefinitionMapper.selectByPrimaryKey("aspect_gyq_api_one");

        String jsonObject = JSON.toJSONString(aspectGyqApiOne);
        if (!StringUtils.equals("{}", jsonObject) && !StringUtils.equals("[]", jsonObject)) {
            source.set(JSON.toJSONString(aspectGyqApiOne));
        }

        List<Map> resources = new ArrayList<>();
        String v = source.get();
        if (StringUtils.isNotBlank(v)) {
            // array
            if (StringUtils.startsWith(v, "[")) {
                resources.addAll(JSON.parseArray(v, Map.class));
            }
            // map
            else {
                Map<?, ?> value = JSON.parseObject(v, Map.class);
                resources.add(value);
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(sessionId);
        userDTO.setName("admin");
        SessionUser user = SessionUser.fromUser(userDTO, sessionId);

        for (String event : eventList) {
            afterReturningNoticeSendService.sendNotice(apiDefinitionTask, event,resources, user, "100001100001");
        }

    }

    @Test
    @Order(3)
    public void ApiScenarioNoticeSuccess() {
        String apiScenarioTask = NoticeConstants.TaskType.API_SCENARIO_TASK;

        List<String>eventList = new ArrayList<>();
        getTypeList(eventList);
        ApiScenario aspectGyqApiScenarioOne = apiScenarioMapper.selectByPrimaryKey("aspect_gyq_api_scenario_one");

        String jsonObject = JSON.toJSONString(aspectGyqApiScenarioOne);
        if (!StringUtils.equals("{}", jsonObject) && !StringUtils.equals("[]", jsonObject)) {
            source.set(JSON.toJSONString(aspectGyqApiScenarioOne));
        }

        List<Map> resources = new ArrayList<>();
        String v = source.get();
        if (StringUtils.isNotBlank(v)) {
            // array
            if (StringUtils.startsWith(v, "[")) {
                resources.addAll(JSON.parseArray(v, Map.class));
            }
            // map
            else {
                Map<?, ?> value = JSON.parseObject(v, Map.class);
                resources.add(value);
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(sessionId);
        userDTO.setName("admin");
        SessionUser user = SessionUser.fromUser(userDTO, sessionId);

        for (String event : eventList) {
            afterReturningNoticeSendService.sendNotice(apiScenarioTask, event,resources, user, "100001100001");
        }

    }

    @Test
    @Order(4)
    public void TestPlanTaskNoticeSuccess() {
        String testPlanTask = NoticeConstants.TaskType.TEST_PLAN_TASK;

        List<String>eventList = new ArrayList<>();
        getTypeList(eventList);
        TestPlan aspectGyqTestPlanOne = testPlanMapper.selectByPrimaryKey("aspect_gyq_test_plan_one");

        String jsonObject = JSON.toJSONString(aspectGyqTestPlanOne);
        if (!StringUtils.equals("{}", jsonObject) && !StringUtils.equals("[]", jsonObject)) {
            source.set(JSON.toJSONString(aspectGyqTestPlanOne));
        }

        List<Map> resources = new ArrayList<>();
        String v = source.get();
        if (StringUtils.isNotBlank(v)) {
            // array
            if (StringUtils.startsWith(v, "[")) {
                resources.addAll(JSON.parseArray(v, Map.class));
            }
            // map
            else {
                Map<?, ?> value = JSON.parseObject(v, Map.class);
                resources.add(value);
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(sessionId);
        userDTO.setName("admin");
        SessionUser user = SessionUser.fromUser(userDTO, sessionId);

        for (String event : eventList) {
            afterReturningNoticeSendService.sendNotice(testPlanTask, event,resources, user, "100001100001");
        }

    }

    @Test
    @Order(5)
    public void CaseReviewTaskNoticeSuccess() {
        String caseReviewTask = NoticeConstants.TaskType.CASE_REVIEW_TASK;

        List<String>eventList = new ArrayList<>();
        getTypeList(eventList);
        CaseReview aspectGyqCaseReviewOne = caseReviewMapper.selectByPrimaryKey("aspect_gyq_case_review_one");

        String jsonObject = JSON.toJSONString(aspectGyqCaseReviewOne);
        if (!StringUtils.equals("{}", jsonObject) && !StringUtils.equals("[]", jsonObject)) {
            source.set(JSON.toJSONString(aspectGyqCaseReviewOne));
        }

        List<Map> resources = new ArrayList<>();
        String v = source.get();
        if (StringUtils.isNotBlank(v)) {
            // array
            if (StringUtils.startsWith(v, "[")) {
                resources.addAll(JSON.parseArray(v, Map.class));
            }
            // map
            else {
                Map<?, ?> value = JSON.parseObject(v, Map.class);
                resources.add(value);
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(sessionId);
        userDTO.setName("admin");
        SessionUser user = SessionUser.fromUser(userDTO, sessionId);

        for (String event : eventList) {
            afterReturningNoticeSendService.sendNotice(caseReviewTask, event,resources, user, "100001100001");
        }

    }

    @Test
    @Order(6)
    public void LoadTestTaskTaskNoticeSuccess() {
        String loadTestTask = NoticeConstants.TaskType.LOAD_TEST_TASK;

        List<String>eventList = new ArrayList<>();
        getTypeList(eventList);
        LoadTest loadTest = new LoadTest();
        loadTest.setId("aspect_gyq_load_test_one");
        loadTest.setProjectId("100001100001");
        loadTest.setName("load_test");
        loadTest.setCreateTime(System.currentTimeMillis());
        loadTest.setUpdateTime(System.currentTimeMillis());
        loadTest.setStatus("Starting");
        loadTest.setTestResourcePoolId("test_pool");
        loadTest.setNum(10001);
        loadTest.setCreateUser("admin");
        loadTest.setPos(1L);
        loadTest.setVersionId("v1.10");
        loadTest.setRefId("aspect_gyq_load_test_one");
        loadTest.setLatest(true);

        String jsonObject = JSON.toJSONString(loadTest);
        if (!StringUtils.equals("{}", jsonObject) && !StringUtils.equals("[]", jsonObject)) {
            source.set(JSON.toJSONString(loadTest));
        }

        List<Map> resources = new ArrayList<>();
        String v = source.get();
        if (StringUtils.isNotBlank(v)) {
            // array
            if (StringUtils.startsWith(v, "[")) {
                resources.addAll(JSON.parseArray(v, Map.class));
            }
            // map
            else {
                Map<?, ?> value = JSON.parseObject(v, Map.class);
                resources.add(value);
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(sessionId);
        userDTO.setName("admin");
        SessionUser user = SessionUser.fromUser(userDTO, sessionId);

        for (String event : eventList) {
            afterReturningNoticeSendService.sendNotice(loadTestTask, event,resources, user, "100001100001");
        }

    }

    private static void getTypeList(List<String>eventList ) {
        eventList.add(NoticeConstants.Event.CREATE);
        eventList.add(NoticeConstants.Event.UPDATE);
        eventList.add(NoticeConstants.Event.AT);
        eventList.add(NoticeConstants.Event.REPLY);
    }


}
