package io.metersphere.system.controller;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.domain.FunctionalCaseCustomFieldExample;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
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
import io.metersphere.system.notice.sender.AfterReturningNoticeSendService;
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
public class AfterReturningNoticeSendServiceTests extends BaseTest {


    @Resource
    private AfterReturningNoticeSendService afterReturningNoticeSendService;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private CustomFieldMapper customFieldMapper;
    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;


    private ThreadLocal<String> source = new ThreadLocal<>();

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_aspect.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void noticeSuccess() {
        String taskType = NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK;
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
            afterReturningNoticeSendService.sendNotice(taskType, event,resources, user, "100001100001");
        }

    }

    private static void getTypeList(List<String>eventList ) {
        eventList.add(NoticeConstants.Event.CREATE);
        eventList.add(NoticeConstants.Event.UPDATE);
        eventList.add(NoticeConstants.Event.AT);
        eventList.add(NoticeConstants.Event.REPLY);
    }


}
