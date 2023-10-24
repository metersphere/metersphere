package io.metersphere.functional.controller;

import io.metersphere.functional.dto.CaseCustomsFieldDTO;
import io.metersphere.functional.request.FunctionalCaseAddRequest;
import io.metersphere.functional.utils.FileBaseUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseControllerTests extends BaseTest {

    public static final String FUNCTIONAL_CASE_URL = "/functional/case/add";

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_file_metadata_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testTestPlanShare() throws Exception {
        //新增
        FunctionalCaseAddRequest request = creatFunctionalCase();
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        List<MockMultipartFile> files = new ArrayList<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_URL, paramMap);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

        //设置自定义字段
        List<CaseCustomsFieldDTO> dtoList = creatCustomsFields();
        request.setCustomsFields(dtoList);

        //设置文件
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_re-upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath));
        files.add(file);

        //设置关联文件
        request.setRelateFileMetaIds(Arrays.asList("relate_file_meta_id_1", "relate_file_meta_id_2"));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);

        this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_URL, paramMap);
    }

    private List<CaseCustomsFieldDTO> creatCustomsFields() {
        List<CaseCustomsFieldDTO> list = new ArrayList<>();
        CaseCustomsFieldDTO customsFieldDTO = new CaseCustomsFieldDTO();
        customsFieldDTO.setFieldId("customs_field_id_1");
        customsFieldDTO.setValue("customs_field_value_1");
        list.add(customsFieldDTO);
        return list;
    }

    private FunctionalCaseAddRequest creatFunctionalCase() {
        FunctionalCaseAddRequest functionalCaseAddRequest = new FunctionalCaseAddRequest();
        functionalCaseAddRequest.setProjectId(DEFAULT_PROJECT_ID);
        functionalCaseAddRequest.setTemplateId("default_template_id");
        functionalCaseAddRequest.setName("测试用例新增");
        functionalCaseAddRequest.setCaseEditType("STEP");
        functionalCaseAddRequest.setModuleId("default_module_id");
        return functionalCaseAddRequest;
    }


}
