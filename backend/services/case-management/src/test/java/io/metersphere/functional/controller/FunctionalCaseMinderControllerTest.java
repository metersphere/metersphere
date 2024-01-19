package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCaseBlob;
import io.metersphere.functional.dto.FunctionalCaseStepDTO;
import io.metersphere.functional.dto.FunctionalMinderTreeDTO;
import io.metersphere.functional.mapper.FunctionalCaseBlobMapper;
import io.metersphere.functional.request.FunctionalCasePageRequest;
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
import java.util.HashMap;
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
        List<FunctionalCaseBlob>blobs = new ArrayList<>();
        FunctionalCaseBlob blob = new FunctionalCaseBlob();
        blob.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_4");
        List<FunctionalCaseStepDTO> stepDTOList = new ArrayList<>();
        FunctionalCaseStepDTO functionalCaseStepDTO = new FunctionalCaseStepDTO();
        functionalCaseStepDTO.setNum(1);
        functionalCaseStepDTO.setDesc("用例4的步骤描述");
        functionalCaseStepDTO.setResult("用例4的步骤预期");
        stepDTOList.add(functionalCaseStepDTO);
        FunctionalCaseStepDTO functionalCaseStepDTOTwo = new FunctionalCaseStepDTO();
        functionalCaseStepDTOTwo.setNum(2);
        functionalCaseStepDTOTwo.setDesc("用例4的步骤描述2");
        functionalCaseStepDTOTwo.setResult("用例4的步骤预期1");
        stepDTOList.add(functionalCaseStepDTOTwo);
        blob.setSteps(JSON.toJSONString(stepDTOList).getBytes(StandardCharsets.UTF_8));
        String prerequisite = "用例4的前置";
        blob.setPrerequisite(prerequisite.getBytes(StandardCharsets.UTF_8));
        String description = "用例4的备注";
        blob.setDescription(description.getBytes(StandardCharsets.UTF_8));
        blobs.add(blob);
        FunctionalCaseBlob blob5 = new FunctionalCaseBlob();
        blob5.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_5");
        List<FunctionalCaseStepDTO> stepDTOList5 = new ArrayList<>();
        FunctionalCaseStepDTO functionalCaseStepDTO5 = new FunctionalCaseStepDTO();
        functionalCaseStepDTO5.setNum(1);
        functionalCaseStepDTO5.setDesc("用例5的步骤描述");
        functionalCaseStepDTO5.setResult("用例5的步骤预期");
        stepDTOList5.add(functionalCaseStepDTO5);
        FunctionalCaseStepDTO functionalCaseStepDTOTwo5= new FunctionalCaseStepDTO();
        functionalCaseStepDTOTwo5.setNum(2);
        functionalCaseStepDTOTwo5.setDesc("用例5的步骤描述2");
        functionalCaseStepDTOTwo5.setResult("用例5的步骤预期1");
        stepDTOList5.add(functionalCaseStepDTOTwo5);
        blob5.setSteps(JSON.toJSONString(stepDTOList5).getBytes(StandardCharsets.UTF_8));
        String prerequisite5 = "用例5的前置";
        blob5.setPrerequisite(prerequisite5.getBytes(StandardCharsets.UTF_8));
        String description5 = "用例5的备注";
        blob5.setDescription(description5.getBytes(StandardCharsets.UTF_8));
        blobs.add(blob5);
        FunctionalCaseBlob blob6 = new FunctionalCaseBlob();
        blob6.setId("TEST_FUNCTIONAL_MINDER_CASE_ID_6");
        String textDescription = "用例6的步骤描述";
        blob6.setTextDescription(textDescription.getBytes(StandardCharsets.UTF_8));
        String expectedResult = "用例6的步骤预期";
        blob6.setExpectedResult(expectedResult.getBytes(StandardCharsets.UTF_8));
        String prerequisite6 = "用例6的前置";
        blob6.setPrerequisite(prerequisite6.getBytes(StandardCharsets.UTF_8));
        String description6 = "用例6的备注";
        blob6.setDescription(description6.getBytes(StandardCharsets.UTF_8));
        blobs.add(blob6);
        functionalCaseBlobMapper.batchInsert(blobs);

        FunctionalCasePageRequest request = new FunctionalCasePageRequest();
        request.setProjectId("project-case-minder-test");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        /*//自定义字段 测试
        Map<String, Object> map = new HashMap<>();
        map.put("customs", Arrays.asList(new LinkedHashMap() {{
            put("id", "TEST_FIELD_ID");
            put("operator", "in");
            put("value", "222");
            put("type", "List");
        }}));
        request.setCombine(map);*/
        MvcResult mvcResultPage = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_LIST_URL, request);
        String contentAsString = mvcResultPage.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        FunctionalMinderTreeDTO baseTreeNodes = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalMinderTreeDTO.class);
        Assertions.assertNotNull(baseTreeNodes);
        String jsonString = JSON.toJSONString(baseTreeNodes);
        System.out.println(jsonString);

    }
}
