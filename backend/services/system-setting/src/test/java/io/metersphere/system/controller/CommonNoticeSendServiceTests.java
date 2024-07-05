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
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.dto.FunctionalCaseDTO;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.CommonNoticeSendService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CommonNoticeSendServiceTests extends BaseTest {


    @Resource
    private CommonNoticeSendService commonNoticeSendService;
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
        String fieldId = functionalCaseCustomFields.getFirst().getFieldId();
        CustomField customFields = customFieldMapper.selectByPrimaryKey(fieldId);

        List<OptionDTO>optionDTOList = new ArrayList<>();
        OptionDTO optionDTO = new OptionDTO();
        optionDTO.setId(customFields.getName());
        optionDTO.setName(functionalCaseCustomFields.getFirst().getValue());
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
        user.setLanguage("zh-CN");
        for (String event : eventList) {
            commonNoticeSendService.sendNotice(functionalCaseTask, event,resources, user, "100001100001");
        }

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
            commonNoticeSendService.sendNotice(apiDefinitionTask, event,resources, user, "100001100001");
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
            commonNoticeSendService.sendNotice(apiScenarioTask, event,resources, user, "100001100001");
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
            commonNoticeSendService.sendNotice(testPlanTask, event,resources, user, "100001100001");
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
            commonNoticeSendService.sendNotice(caseReviewTask, event,resources, user, "100001100001");
        }

    }

    private static void getTypeList(List<String>eventList ) {
        eventList.add(NoticeConstants.Event.CREATE);
        eventList.add(NoticeConstants.Event.UPDATE);
        eventList.add(NoticeConstants.Event.AT);
        eventList.add(NoticeConstants.Event.REPLY);
    }


}
