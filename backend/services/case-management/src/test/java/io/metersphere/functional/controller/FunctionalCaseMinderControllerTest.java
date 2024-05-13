package io.metersphere.functional.controller;

import io.metersphere.functional.domain.*;
import io.metersphere.functional.dto.FunctionalCaseStepDTO;
import io.metersphere.functional.dto.FunctionalMinderTreeDTO;
import io.metersphere.functional.dto.MinderOptionDTO;
import io.metersphere.functional.dto.MinderTargetDTO;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.mapper.FunctionalCaseModuleMapper;
import io.metersphere.functional.request.FunctionalCaseMindRequest;
import io.metersphere.functional.request.FunctionalCaseMinderEditRequest;
import io.metersphere.functional.request.FunctionalCaseMinderRemoveRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.enums.MoveTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseMinderControllerTest extends BaseTest {

    public static final String FUNCTIONAL_CASE_LIST_URL = "/functional/mind/case/list";

    public static final String FUNCTIONAL_CASE_UPDATE_NAME_URL = "/functional/mind/case/update/source/name";

    public static final String FUNCTIONAL_CASE_UPDATE_PRIORITY_URL = "/functional/mind/case/update/source/priority";

    public static final String FUNCTIONAL_CASE_BATCH_DELETE = "/functional/mind/case/batch/delete/";

    public static final String FUNCTIONAL_CASE_BATCH_MOVE = "/functional/mind/case/batch/remove";



    @Resource
    private FunctionalCaseBlobMapper functionalCaseBlobMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private FunctionalCaseModuleMapper functionalCaseModuleMapper;
    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_file_minder_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testGetPageList() throws Exception {
        List<FunctionalCaseStepDTO> list = new ArrayList<>();
        FunctionalCaseStepDTO functionalCaseStepDTO = new FunctionalCaseStepDTO();
        functionalCaseStepDTO.setId("12455");
        functionalCaseStepDTO.setNum(0);
        functionalCaseStepDTO.setDesc("步骤一");
        functionalCaseStepDTO.setResult("步骤一结果");
        list.add(functionalCaseStepDTO);
        functionalCaseStepDTO = new FunctionalCaseStepDTO();
        functionalCaseStepDTO.setId("12ddd455");
        functionalCaseStepDTO.setNum(1);
        functionalCaseStepDTO.setDesc("步骤二");
        functionalCaseStepDTO.setResult("步骤二结果");
        list.add(functionalCaseStepDTO);
        String textDescription = "hahahahah，这是文本描述";
        String expectedResult = "文本描述的结果";
        String prerequisite = "前置条件";
        String description = "备注";

        FunctionalCaseBlob functionalCaseBlob = new FunctionalCaseBlob();
        functionalCaseBlob.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_2");
        functionalCaseBlob.setSteps(JSON.toJSONString(list).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setTextDescription(textDescription.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setExpectedResult(expectedResult.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setPrerequisite(prerequisite.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob.setDescription(description.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlobMapper.insert(functionalCaseBlob);
        FunctionalCaseBlob functionalCaseBlob6 = new FunctionalCaseBlob();
        functionalCaseBlob6.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_1");
        functionalCaseBlob6.setSteps(JSON.toJSONString(list).getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob6.setTextDescription(textDescription.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob6.setExpectedResult(expectedResult.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob6.setPrerequisite(prerequisite.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlob6.setDescription(description.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlobMapper.updateByPrimaryKeyWithBLOBs(functionalCaseBlob6);

        FunctionalCaseMindRequest request = new FunctionalCaseMindRequest();
        request.setProjectId("project-case-minder-test");
        request.setModuleId("TEST_MINDER_MODULE_ID_GYQ");
        MvcResult mvcResultPage = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_LIST_URL, request);
        String contentAsString = mvcResultPage.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<FunctionalMinderTreeDTO> baseTreeNodes = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), FunctionalMinderTreeDTO.class);
        Assertions.assertNotNull(baseTreeNodes);
        String jsonString = JSON.toJSONString(baseTreeNodes);
        System.out.println(jsonString);
        Assertions.assertEquals(2, baseTreeNodes.size());

    }

    @Test
    @Order(2)
    public void testUpdateCase() throws Exception {

        FunctionalCaseMinderEditRequest request = new FunctionalCaseMinderEditRequest();
        request.setProjectId("project-case-minder-test");
        request.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        request.setName("TEST_FUNCTIONAL_MINDER_CASE_ID_Change_Name");
        request.setType(Translator.get("minder_extra_node.case"));
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_NAME_URL, request);
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        Assertions.assertEquals(functionalCase.getName(), "TEST_FUNCTIONAL_MINDER_CASE_ID_Change_Name");
        request.setName("TEST_MINDER_MODULE_ID_GYQ5_Change_Name");
        request.setId("TEST_MINDER_MODULE_ID_GYQ5");
        request.setType(Translator.get("minder_extra_node.module"));
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_NAME_URL, request);
        FunctionalCaseModule functionalCaseModule = functionalCaseModuleMapper.selectByPrimaryKey("TEST_MINDER_MODULE_ID_GYQ5");
        Assertions.assertEquals(functionalCaseModule.getName(), "TEST_MINDER_MODULE_ID_GYQ5_Change_Name");

        request.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        request.setName("前置哈哈哈");
        request.setType(Translator.get("minder_extra_node.prerequisite"));
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_NAME_URL, request);
        FunctionalCaseBlob functionalCaseBlob = functionalCaseBlobMapper.selectByPrimaryKey("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        Assertions.assertEquals(new String(functionalCaseBlob.getPrerequisite(), StandardCharsets.UTF_8), "前置哈哈哈");

        FunctionalCaseBlob functionalCaseBlobInDB = new FunctionalCaseBlob();
        functionalCaseBlobInDB.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        String steps = "[{\"id\":\"aa159262-baf9-4a11-91b9-0ab50a9f199e\",\"num\":0,\"desc\":\"点点滴滴\",\"result\":\"点点滴滴的\"},{\"id\":\"2bf16247-96a2-44c4-92c8-f620f85eb351\",\"num\":1,\"desc\":\"d d d\",\"result\":\" 得到的\"}]";
        functionalCaseBlobInDB.setSteps(steps.getBytes(StandardCharsets.UTF_8));
        functionalCaseBlobMapper.updateByPrimaryKeySelective(functionalCaseBlobInDB);
        request.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        request.setName("步骤哈哈哈");
        request.setType(Translator.get("minder_extra_node.steps"));
        request.setPos(1L);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_NAME_URL, request);
        functionalCaseBlob = functionalCaseBlobMapper.selectByPrimaryKey("TEST_FUNCTIONAL_MINDER_CASE_ID_6");

        List<FunctionalCaseStepDTO> functionalCaseStepDTOS = JSON.parseArray(new String(functionalCaseBlob.getSteps(), StandardCharsets.UTF_8), FunctionalCaseStepDTO.class);
        functionalCaseStepDTOS.forEach(t->{
            if (t.getNum()==1) {
                Assertions.assertEquals(t.getDesc(), "步骤哈哈哈");
            }
        });
        request.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        request.setName("步骤结果哈哈哈");
        request.setType(Translator.get("minder_extra_node.steps_expected_result"));
        request.setPos(1L);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_NAME_URL, request);
        functionalCaseBlob = functionalCaseBlobMapper.selectByPrimaryKey("TEST_FUNCTIONAL_MINDER_CASE_ID_6");

        functionalCaseStepDTOS = JSON.parseArray(new String(functionalCaseBlob.getSteps(), StandardCharsets.UTF_8), FunctionalCaseStepDTO.class);
        functionalCaseStepDTOS.forEach(t->{
            if (t.getNum()==1) {
                Assertions.assertEquals(t.getResult(), "步骤结果哈哈哈");
            }
        });
        request.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        request.setName("文本哈哈哈");
        request.setType(Translator.get("minder_extra_node.text_description"));
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_NAME_URL, request);
        functionalCaseBlob = functionalCaseBlobMapper.selectByPrimaryKey("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        Assertions.assertEquals(new String(functionalCaseBlob.getTextDescription(), StandardCharsets.UTF_8), "文本哈哈哈");request.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        request.setName("预期哈哈哈");
        request.setType(Translator.get("minder_extra_node.text_expected_result"));
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_NAME_URL, request);
        functionalCaseBlob = functionalCaseBlobMapper.selectByPrimaryKey("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        Assertions.assertEquals(new String(functionalCaseBlob.getExpectedResult(), StandardCharsets.UTF_8), "预期哈哈哈");
        request.setName("备注哈哈哈");
        request.setType(Translator.get("minder_extra_node.description"));
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_NAME_URL, request);
        functionalCaseBlob = functionalCaseBlobMapper.selectByPrimaryKey("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        Assertions.assertEquals(new String(functionalCaseBlob.getDescription(), StandardCharsets.UTF_8), "备注哈哈哈");
        request = new FunctionalCaseMinderEditRequest();
        request.setProjectId("project-case-minder-test");
        request.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        request.setPriority("P0");
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_PRIORITY_URL, request);
        FunctionalCaseCustomFieldExample customField = new FunctionalCaseCustomFieldExample();
        customField.createCriteria().andCaseIdEqualTo(request.getId()).andFieldIdEqualTo("custom_field_minder_gyq_id_3");
        List<FunctionalCaseCustomField> functionalCaseCustomFields = functionalCaseCustomFieldMapper.selectByExample(customField);
        Assertions.assertEquals(functionalCaseCustomFields.get(0).getValue(), "P0");
        request = new FunctionalCaseMinderEditRequest();
        request.setProjectId("project-case-minder-test");
        request.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_PRIORITY_URL, request);
    }

    @Test
    @Order(3)
    public void testDeleteCase() throws Exception{
        List<MinderOptionDTO> resourceList = new ArrayList<>();
        this.requestPost(FUNCTIONAL_CASE_BATCH_DELETE+"/project-case-minder-test", resourceList).andExpect(status().is5xxServerError());
        MinderOptionDTO optionDTO = new MinderOptionDTO("TEST_MINDER_MODULE_ID_GYQ6", Translator.get("minder_extra_node.module"), 5000L);
        resourceList.add(optionDTO);
        this.requestPost(FUNCTIONAL_CASE_BATCH_DELETE+"/project-case-minder-test-xxx", resourceList).andExpect(status().is5xxServerError());
        resourceList = new ArrayList<>();
        optionDTO = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_7", Translator.get("minder_extra_node.case"), 1000L);
        resourceList.add(optionDTO);
        this.requestPost(FUNCTIONAL_CASE_BATCH_DELETE+"/project-case-minder-test-xxx", resourceList).andExpect(status().is5xxServerError());
        resourceList = new ArrayList<>();
        MinderOptionDTO optionDTOCase = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_6", Translator.get("minder_extra_node.case"), 600L);
        MinderOptionDTO optionDTOModule = new MinderOptionDTO("TEST_MINDER_MODULE_ID_GYQ", Translator.get("minder_extra_node.module"), 1200L);
        resourceList.add(optionDTOModule);
        resourceList.add(optionDTOCase);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_DELETE+"/project-case-minder-test", resourceList);
        FunctionalCase functionalCaseOne = functionalCaseMapper.selectByPrimaryKey("TEST_FUNCTIONAL_MINDER_CASE_ID_2");
        Assertions.assertTrue(functionalCaseOne.getDeleted());
        FunctionalCase functionalCaseTwo = functionalCaseMapper.selectByPrimaryKey("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        Assertions.assertTrue(functionalCaseTwo.getDeleted());
        resourceList = new ArrayList<>();
        optionDTOCase = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_3", Translator.get("minder_extra_node.case"), 600L);
        resourceList.add(optionDTOCase);
        MinderOptionDTO optionDTOPrerequisite = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_3", Translator.get("minder_extra_node.prerequisite").toString(), 0L);
        MinderOptionDTO optionDTODescription = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_3", Translator.get("minder_extra_node.description").toString(), 3L);
        MinderOptionDTO optionDTOStep = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_3", Translator.get("minder_extra_node.steps").toString(), 2L);
        MinderOptionDTO optionDTOStepExpectedResult = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_3", Translator.get("minder_extra_node.steps_expected_result").toString(), 2L);
        MinderOptionDTO optionDTOText = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_3", Translator.get("minder_extra_node.text_description").toString(), 2L);
        MinderOptionDTO optionDTOTextExpectedResult = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_3", Translator.get("minder_extra_node.text_expected_result").toString(), 2L);
        resourceList.add(optionDTOPrerequisite);
        resourceList.add(optionDTODescription);
        resourceList.add(optionDTOStep);
        resourceList.add(optionDTOStepExpectedResult);
        resourceList.add(optionDTOText);
        resourceList.add(optionDTOTextExpectedResult);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_DELETE+"/project-case-minder-test", resourceList);
        resourceList = new ArrayList<>();
        optionDTOText = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_7", Translator.get("minder_extra_node.text_description").toString(), 2L);
        resourceList.add(optionDTOText);
        optionDTOTextExpectedResult = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_7", Translator.get("minder_extra_node.text_expected_result").toString(), 2L);
        resourceList.add(optionDTOTextExpectedResult);
        this.requestPost(FUNCTIONAL_CASE_BATCH_DELETE+"/project-case-minder-test", resourceList).andExpect(status().is5xxServerError());

    }
    @Test
    @Order(4)
    public void testRemoveCase() throws Exception{
        FunctionalCaseMinderRemoveRequest functionalCaseMinderRemoveRequest = new FunctionalCaseMinderRemoveRequest();
        functionalCaseMinderRemoveRequest.setProjectId("project-case-minder-test");
        functionalCaseMinderRemoveRequest.setParentTargetId("TEST_MINDER_MODULE_ID_GYQ2");
        MinderTargetDTO caseMinderTargetDTO = new MinderTargetDTO();
        caseMinderTargetDTO.setTargetId("TEST_FUNCTIONAL_MINDER_CASE_ID_8");
        caseMinderTargetDTO.setMoveMode(MoveTypeEnum.AFTER.name());
        functionalCaseMinderRemoveRequest.setCaseMinderTargetDTO(caseMinderTargetDTO);
        MinderTargetDTO moduleMinderTargetDTO = new MinderTargetDTO();
        moduleMinderTargetDTO.setTargetId("TEST_MINDER_MODULE_ID_GYQ2");
        moduleMinderTargetDTO.setMoveMode(MoveTypeEnum.AFTER.name());
        functionalCaseMinderRemoveRequest.setModuleMinderTargetDTO(moduleMinderTargetDTO);
        functionalCaseMinderRemoveRequest.setResourceList(new ArrayList<>());
        this.requestPost(FUNCTIONAL_CASE_BATCH_MOVE, functionalCaseMinderRemoveRequest).andExpect(status().is5xxServerError());
        List<MinderOptionDTO> resourceList = new ArrayList<>();
        MinderOptionDTO optionDTOCase = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_8", Translator.get("minder_extra_node.case"), 600L);
        resourceList.add(optionDTOCase);
        MinderOptionDTO optionDTOModule = new MinderOptionDTO("TEST_MINDER_MODULE_ID_GYQ5", Translator.get("minder_extra_node.module"), 600L);
        resourceList.add(optionDTOModule);
        functionalCaseMinderRemoveRequest.setResourceList(resourceList);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_MOVE, functionalCaseMinderRemoveRequest);
        FunctionalCaseModule functionalCaseModule = functionalCaseModuleMapper.selectByPrimaryKey("TEST_MINDER_MODULE_ID_GYQ5");
        Assertions.assertTrue(functionalCaseModule.getPos() !=0);
        functionalCaseMinderRemoveRequest.setParentTargetId(null);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_MOVE, functionalCaseMinderRemoveRequest);
        functionalCaseModule = functionalCaseModuleMapper.selectByPrimaryKey("TEST_MINDER_MODULE_ID_GYQ5");
        functionalCaseMinderRemoveRequest.setSteps("[{\"id\":\"aa159262-baf9-4a11-91b9-0ab50a9f199e\",\"num\":0,\"desc\":\"点点滴滴\",\"result\":\"点点滴滴的\"},{\"id\":\"2bf16247-96a2-44c4-92c8-f620f85eb351\",\"num\":1,\"desc\":\"d d d\",\"result\":\" 得到的\"}]");
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_MOVE, functionalCaseMinderRemoveRequest);
        Assertions.assertTrue(functionalCaseModule.getPos() !=0);
        caseMinderTargetDTO = new MinderTargetDTO();
        caseMinderTargetDTO.setTargetId("TEST_FUNCTIONAL_MINDER_CASE_ID_8");
        caseMinderTargetDTO.setMoveMode(MoveTypeEnum.BEFORE.name());
        functionalCaseMinderRemoveRequest.setCaseMinderTargetDTO(caseMinderTargetDTO);
        moduleMinderTargetDTO = new MinderTargetDTO();
        moduleMinderTargetDTO.setTargetId("TEST_MINDER_MODULE_ID_GYQ2");
        moduleMinderTargetDTO.setMoveMode(MoveTypeEnum.BEFORE.name());
        functionalCaseMinderRemoveRequest.setModuleMinderTargetDTO(moduleMinderTargetDTO);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_MOVE, functionalCaseMinderRemoveRequest);
        functionalCaseModule = functionalCaseModuleMapper.selectByPrimaryKey("TEST_MINDER_MODULE_ID_GYQ5");
        Assertions.assertTrue(functionalCaseModule.getPos()==5000);
        functionalCaseMinderRemoveRequest.setCaseMinderTargetDTO(null);
        functionalCaseMinderRemoveRequest.setModuleMinderTargetDTO(null);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_MOVE, functionalCaseMinderRemoveRequest);
        functionalCaseModule = functionalCaseModuleMapper.selectByPrimaryKey("TEST_MINDER_MODULE_ID_GYQ5");
        Assertions.assertTrue(functionalCaseModule.getPos()==5000);
        functionalCaseMinderRemoveRequest.setParentTargetId("TEST_MINDER_MODULE_ID_GYQ6");
        this.requestPost(FUNCTIONAL_CASE_BATCH_MOVE, functionalCaseMinderRemoveRequest).andExpect(status().is5xxServerError());
        resourceList = new ArrayList<>();
        optionDTOCase = new MinderOptionDTO("TEST_FUNCTIONAL_MINDER_CASE_ID_7", Translator.get("minder_extra_node.case"), 600L);
        resourceList.add(optionDTOCase);
        this.requestPost(FUNCTIONAL_CASE_BATCH_MOVE, functionalCaseMinderRemoveRequest).andExpect(status().is5xxServerError());
        optionDTOModule = new MinderOptionDTO("TEST_MINDER_MODULE_ID_GYQ6", Translator.get("minder_extra_node.module"), 600L);
        resourceList = new ArrayList<>();
        resourceList.add(optionDTOModule);
        functionalCaseMinderRemoveRequest.setResourceList(resourceList);
        this.requestPost(FUNCTIONAL_CASE_BATCH_MOVE, functionalCaseMinderRemoveRequest).andExpect(status().is5xxServerError());


    }

}
