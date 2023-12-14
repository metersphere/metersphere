package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.functional.domain.FunctionalCaseExample;
import io.metersphere.functional.dto.FunctionalCasePageDTO;
import io.metersphere.functional.mapper.FunctionalCaseCommentMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.request.FunctionalCaseBatchRequest;
import io.metersphere.functional.request.FunctionalCasePageRequest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseTrashControllerTests extends BaseTest {

    private static final String URL_CASE_PAGE = "/functional/case/trash/page";
    private static final String URL_CASE_MODULE_COUNT = "/functional/case/trash/module/count";
    private static final String URL_CASE_RECOVER = "/functional/case/trash/recover/";
    private static final String URL_CASE_BATCH_RECOVER = "/functional/case/trash/batch/recover";
    private static final String URL_CASE_DELETE = "/functional/case/trash/delete/";
    private static final String URL_CASE_BATCH_DELETE = "/functional/case/trash/batch/delete";

    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private CustomFieldMapper customFieldMapper;
    @Resource
    private FunctionalCaseCommentMapper functionalCaseCommentMapper;


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_case_trash.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testGetPageList() throws Exception {
        FunctionalCasePageRequest request = new FunctionalCasePageRequest();
        request.setProjectId("test_project_id");
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPost(URL_CASE_PAGE, request);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL_CASE_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        //自定义字段 测试
        Map<String, Object> map = new HashMap<>();
        map.put("customs", Arrays.asList(new LinkedHashMap() {{
            put("id", "TEST_FIELD_ID");
            put("operator", "in");
            put("value", "222");
            put("type", "List");
        }}));
        request.setCombine(map);
        MvcResult mvcResultPage = this.requestPostWithOkAndReturn(URL_CASE_PAGE, request);
        Pager<List<FunctionalCasePageDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultPage.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        MvcResult moduleCountMvcResult = this.requestPostWithOkAndReturn(URL_CASE_MODULE_COUNT, request);
        Map<String, Integer> moduleCount = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(moduleCountMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Map.class);

        //返回值的页码和当前页码相同
        Assertions.assertEquals(tableData.getCurrent(), request.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(tableData.getList())).size() <= request.getPageSize());

        //如果没有数据，则返回的模块节点也不应该有数据
        boolean moduleHaveResource = false;
        for (int countByModuleId : moduleCount.values()) {
            if (countByModuleId > 0) {
                moduleHaveResource = true;
                break;
            }
        }
        Assertions.assertEquals(request.getPageSize(), tableData.getPageSize());
        if (tableData.getTotal() > 0) {
            Assertions.assertTrue(moduleHaveResource);
        }

        Assertions.assertTrue(moduleCount.containsKey("all"));
    }

    @Test
    @Order(2)
    public void recoverCaseSuccessWidthCustom() throws Exception {
        customFieldMapper.deleteByPrimaryKey("gyq_custom_id2");
        this.requestGetWithOk(URL_CASE_RECOVER + "Trash_TEST_FUNCTIONAL_CASE_ID");
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID");
        Assertions.assertFalse(functionalCase.getDeleted());
        System.out.println(functionalCase.getUpdateUser());
        FunctionalCase functionalCaseV = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_4");
        Assertions.assertFalse(functionalCaseV.getDeleted());
    }

    @Test
    @Order(3)
    public void recoverCaseSuccessWidthNoCustom() throws Exception {
        this.requestGetWithOk(URL_CASE_RECOVER + "Trash_TEST_FUNCTIONAL_CASE_ID_1");
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_1");
        Assertions.assertFalse(functionalCase.getDeleted());
        FunctionalCase functionalCase2 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_2");
        Assertions.assertFalse(functionalCase2.getDeleted());
        FunctionalCase functionalCase3 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_3");
        Assertions.assertFalse(functionalCase3.getDeleted());
    }

    @Test
    @Order(4)
    public void recoverCaseFalse() throws Exception {
         mockMvc.perform(MockMvcRequestBuilders.get(URL_CASE_RECOVER + "Trash_TEST_FUNCTIONAL_CASE_ID_del").header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Order(5)
    public void recoverBatchCaseSuccess() throws Exception {
        FunctionalCaseBatchRequest request = new FunctionalCaseBatchRequest();
        request.setProjectId("project-case-trash-test-1");
        request.setSelectAll(false);
        request.setSelectIds(Arrays.asList("Trash_TEST_FUNCTIONAL_CASE_ID_5", "Trash_TEST_FUNCTIONAL_CASE_ID_7"));
        this.requestPostWithOkAndReturn(URL_CASE_BATCH_RECOVER, request);
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_6");
        Assertions.assertFalse(functionalCase.getDeleted());
        FunctionalCase functionalCase2 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_8");
        Assertions.assertFalse(functionalCase2.getDeleted());

        request = new FunctionalCaseBatchRequest();
        request.setProjectId("project-case-trash-test-1");
        request.setSelectAll(true);
        request.setExcludeIds(List.of("Trash_TEST_FUNCTIONAL_CASE_ID_c"));
        this.requestPostWithOkAndReturn(URL_CASE_BATCH_RECOVER, request);
        FunctionalCase functionalCase3 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_a");
        Assertions.assertFalse(functionalCase3.getDeleted());
        FunctionalCase functionalCase4 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_b");
        Assertions.assertTrue(functionalCase4.getDeleted());

        request = new FunctionalCaseBatchRequest();
        request.setProjectId("project-case-trash-test-X");
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(URL_CASE_BATCH_RECOVER, request);

        request = new FunctionalCaseBatchRequest();
        request.setProjectId("project-case-trash-test-X");
        request.setSelectAll(true);
        this.requestPostWithOkAndReturn(URL_CASE_BATCH_RECOVER, request);

        request = new FunctionalCaseBatchRequest();
        request.setProjectId("project-case-trash-test-1");
        request.setSelectAll(false);
        request.setSelectIds(Arrays.asList("Trash_TEST_FUNCTIONAL_CASE_ID_x", "Trash_TEST_FUNCTIONAL_CASE_ID_y"));
        this.requestPostWithOkAndReturn(URL_CASE_BATCH_RECOVER, request);
    }


    @Test
    @Order(6)
    public void deleteCaseWidthNoExist() throws Exception {
        this.requestGetWithOk(URL_CASE_DELETE + "Trash_TEST_FUNCTIONAL_CASE_ID_del");
    }

    @Test
    @Order(7)
    public void deleteCaseWidthSuccess() throws Exception {
        this.requestGetWithOk(URL_CASE_DELETE + "Trash_TEST_FUNCTIONAL_CASE_ID");
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID");
        Assertions.assertNull(functionalCase);
        FunctionalCase functionalCase4 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_4");
        Assertions.assertNull(functionalCase4);
        FunctionalCaseComment functionalCaseComment = functionalCaseCommentMapper.selectByPrimaryKey("trash_comment_id");
        Assertions.assertNull(functionalCaseComment);
        this.requestGetWithOk(URL_CASE_DELETE + "Trash_TEST_FUNCTIONAL_CASE_ID_GYQ");


    }

    @Test
    @Order(8)
    public void batchDeleteCaseWidthSuccess() throws Exception {
        FunctionalCaseExample functionalCaseExample = new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andRefIdEqualTo("Trash_TEST_FUNCTIONAL_CASE_ID_1");
        List<FunctionalCase> functionalCases = functionalCaseMapper.selectByExample(functionalCaseExample);
        functionalCases.forEach(t->{
            t.setDeleted(true);
            functionalCaseMapper.updateByPrimaryKeySelective(t);
        });
        FunctionalCaseBatchRequest request = new FunctionalCaseBatchRequest();
        request.setProjectId("project-case-trash-test");
        request.setSelectAll(false);
        request.setDeleteAll(true);
        request.setSelectIds(List.of("Trash_TEST_FUNCTIONAL_CASE_ID_1"));
        this.requestPostWithOkAndReturn(URL_CASE_BATCH_DELETE, request);
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_2");
        Assertions.assertNull(functionalCase);


        functionalCaseExample= new FunctionalCaseExample();
        functionalCaseExample.createCriteria().andProjectIdEqualTo("project-case-trash-test-1");
        List<FunctionalCase> functionalCaseList = functionalCaseMapper.selectByExample(functionalCaseExample);
        functionalCaseList.forEach(t->{
            t.setDeleted(true);
            functionalCaseMapper.updateByPrimaryKeySelective(t);
        });
        request = new FunctionalCaseBatchRequest();
        request.setProjectId("project-case-trash-test-1");
        request.setSelectAll(false);
        request.setDeleteAll(false);
        request.setSelectIds(List.of("Trash_TEST_FUNCTIONAL_CASE_ID_5"));
        this.requestPostWithOkAndReturn(URL_CASE_BATCH_DELETE, request);
        FunctionalCase functionalCase2 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_6");
        Assertions.assertNotNull(functionalCase2);

        request = new FunctionalCaseBatchRequest();
        request.setProjectId("project-case-trash-test-1");
        request.setSelectAll(true);
        request.setDeleteAll(false);
        this.requestPostWithOkAndReturn(URL_CASE_BATCH_DELETE, request);
        FunctionalCase functionalCase3 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_a");
        Assertions.assertNull(functionalCase3);
        FunctionalCase functionalCase4 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_9");
        Assertions.assertNotNull(functionalCase4);

        request = new FunctionalCaseBatchRequest();
        request.setProjectId("project-case-trash-test-2");
        request.setSelectAll(true);
        request.setDeleteAll(true);
        request.setExcludeIds(List.of("Trash_TEST_FUNCTIONAL_CASE_ID_e"));
        this.requestPostWithOkAndReturn(URL_CASE_BATCH_DELETE, request);
        FunctionalCase functionalCase5 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_f");
        Assertions.assertNull(functionalCase5);
        FunctionalCase functionalCase6 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_d");
        Assertions.assertNotNull(functionalCase6);


        request = new FunctionalCaseBatchRequest();
        request.setProjectId("project-case-trash-test-x");
        request.setSelectAll(true);
        request.setDeleteAll(true);
        this.requestPostWithOkAndReturn(URL_CASE_BATCH_DELETE, request);

        request = new FunctionalCaseBatchRequest();
        request.setProjectId("project-case-trash-test-1");
        request.setSelectAll(false);
        request.setDeleteAll(true);
        this.requestPostWithOkAndReturn(URL_CASE_BATCH_DELETE, request);
    }



}
