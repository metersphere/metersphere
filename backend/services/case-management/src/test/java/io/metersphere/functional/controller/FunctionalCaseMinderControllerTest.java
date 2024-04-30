package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.functional.dto.FunctionalCaseStepDTO;
import io.metersphere.functional.dto.FunctionalMinderTreeDTO;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.request.FunctionalCaseMindRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
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

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseMinderControllerTest extends BaseTest {

    public static final String FUNCTIONAL_CASE_LIST_URL = "/functional/mind/case/list";
    @Resource
    private FunctionalCaseBlobMapper functionalCaseBlobMapper;

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
        Assertions.assertEquals(2,baseTreeNodes.size());

    }
}
