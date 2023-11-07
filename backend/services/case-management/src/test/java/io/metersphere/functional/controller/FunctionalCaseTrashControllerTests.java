package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.functional.mapper.FunctionalCaseCommentMapper;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.functional.result.FunctionalCaseResultCode;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.mapper.CustomFieldMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseTrashControllerTests extends BaseTest {

    private static final String URL_CASE_RECOVER = "/functional/case/trash/recover/";
    private static final String URL_CASE_DELETE = "/functional/case/trash/delete/";


    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private CustomFieldMapper customFieldMapper;
    @Resource
    private FunctionalCaseCommentMapper functionalCaseCommentMapper;


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_case_trash.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
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
    @Order(2)
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
    @Order(3)
    public void recoverCaseFalse() throws Exception {
        assertErrorCode(this.requestGet(URL_CASE_RECOVER + "Trash_TEST_FUNCTIONAL_CASE_ID_del"), FunctionalCaseResultCode.FUNCTIONAL_CASE_NOT_FOUND);
    }

    @Test
    @Order(4)
    public void deleteCaseWidthNoExist() throws Exception {
        this.requestGetWithOk(URL_CASE_DELETE + "Trash_TEST_FUNCTIONAL_CASE_ID_del");
    }

    @Test
    @Order(5)
    public void deleteCaseWidthSuccess() throws Exception {
        this.requestGetWithOk(URL_CASE_DELETE + "Trash_TEST_FUNCTIONAL_CASE_ID");
        FunctionalCase functionalCase = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID");
        Assertions.assertNull(functionalCase);
        FunctionalCase functionalCase4 = functionalCaseMapper.selectByPrimaryKey("Trash_TEST_FUNCTIONAL_CASE_ID_4");
        Assertions.assertNull(functionalCase4);
        FunctionalCaseComment functionalCaseComment = functionalCaseCommentMapper.selectByPrimaryKey("trash_comment_id");
        Assertions.assertNull(functionalCaseComment);

    }

}
