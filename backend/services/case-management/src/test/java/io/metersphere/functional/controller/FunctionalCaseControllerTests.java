package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseAttachment;
import io.metersphere.functional.domain.FunctionalCaseAttachmentExample;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.dto.CaseCustomFieldDTO;
import io.metersphere.functional.dto.FunctionalCaseAttachmentDTO;
import io.metersphere.functional.dto.FunctionalCasePageDTO;
import io.metersphere.functional.dto.response.FunctionalCaseImportResponse;
import io.metersphere.functional.mapper.FunctionalCaseAttachmentMapper;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import io.metersphere.functional.request.*;
import io.metersphere.functional.result.CaseManagementResultCode;
import io.metersphere.functional.utils.FileBaseUtils;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.project.domain.Notification;
import io.metersphere.project.domain.NotificationExample;
import io.metersphere.project.mapper.NotificationMapper;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.dto.OperationHistoryDTO;
import io.metersphere.system.dto.request.OperationHistoryRequest;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.OperationHistoryService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseControllerTests extends BaseTest {

    public static final String FUNCTIONAL_CASE_ADD_URL = "/functional/case/add";
    public static final String DEFAULT_TEMPLATE_FIELD_URL = "/functional/case/default/template/field/";
    public static final String FUNCTIONAL_CASE_DETAIL_URL = "/functional/case/detail/";
    public static final String FUNCTIONAL_CASE_UPDATE_URL = "/functional/case/update";
    public static final String FUNCTIONAL_CASE_EDIT_FOLLOWER_URL = "/functional/case/edit/follower";
    public static final String FUNCTIONAL_CASE_DELETE_URL = "/functional/case/delete";
    public static final String FUNCTIONAL_CASE_LIST_URL = "/functional/case/page";
    public static final String FUNCTIONAL_CASE_MODULE_COUNT = "/functional/case/module/count";
    public static final String FUNCTIONAL_CASE_BATCH_DELETE_URL = "/functional/case/batch/delete-to-gc";
    public static final String FUNCTIONAL_CASE_TABLE_URL = "/functional/case/custom/field/";
    public static final String FUNCTIONAL_CASE_BATCH_MOVE_URL = "/functional/case/batch/move";
    public static final String FUNCTIONAL_CASE_BATCH_COPY_URL = "/functional/case/batch/copy";
    public static final String FUNCTIONAL_CASE_VERSION_URL = "/functional/case/version/";
    public static final String FUNCTIONAL_CASE_BATCH_EDIT_URL = "/functional/case/batch/edit";
    public static final String FUNCTIONAL_CASE_POS_URL = "/functional/case/edit/pos";
    public static final String DOWNLOAD_EXCEL_TEMPLATE_URL = "/functional/case/download/excel/template/";
    public static final String CHECK_EXCEL_URL = "/functional/case/pre-check/excel";
    public static final String IMPORT_EXCEL_URL = "/functional/case/import/excel";
    public static final String OPERATION_HISTORY_URL = "/functional/case/operation-history";

    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    private CustomFieldMapper customFieldMapper;
    @Resource
    private ProjectTemplateService projectTemplateService;
    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;
    @Resource
    private OperationHistoryService operationHistoryService;

    @Resource
    private FunctionalCaseAttachmentMapper functionalCaseAttachmentMapper;

    protected static String functionalCaseId;


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_file_metadata_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testFunctionalCaseAdd() throws Exception {
        //新增
        FunctionalCaseAddRequest request = creatFunctionalCase();
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        List<MockMultipartFile> files = new ArrayList<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_ADD_URL, paramMap);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

        //设置自定义字段
        List<CaseCustomFieldDTO> dtoList = creatCustomFields();
        request.setCustomFields(dtoList);

        //设置文件
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_re-upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath));
        files.add(file);

        //设置关联文件
        request.setRelateFileMetaIds(Arrays.asList("relate_file_meta_id_1", "relate_file_meta_id_2"));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);

        MvcResult functionalCaseMvcResult = this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_ADD_URL, paramMap);

        String functionalCaseData = functionalCaseMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder functionalCaseResultHolder = JSON.parseObject(functionalCaseData, ResultHolder.class);
        FunctionalCase functionalCase = JSON.parseObject(JSON.toJSONString(functionalCaseResultHolder.getData()), FunctionalCase.class);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceNameEqualTo(functionalCase.getName()).andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        List<Notification> notifications = notificationMapper.selectByExampleWithBLOBs(notificationExample);
        String jsonString = JSON.toJSONString(notifications);
        System.out.println(jsonString);
        MockMultipartFile newFile = getMockMultipartFile();
        String newFileId = uploadTemp(newFile);
        request.setCaseDetailFileIds(List.of(newFileId));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", new LinkedMultiValueMap<>());
        functionalCaseMvcResult = this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_ADD_URL, paramMap);
        functionalCaseId = functionalCase.getId();
        // 获取返回值
        returnData = functionalCaseMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        functionalCase = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalCase.class);
        FunctionalCaseEditRequest request1 = new FunctionalCaseEditRequest();
        BeanUtils.copyBean(request1, request);
        request1.setId(functionalCase.getId());
        request1.setRelateFileMetaIds(new ArrayList<>());
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request1));
        paramMap.add("files", new LinkedMultiValueMap<>());
        functionalCaseMvcResult = this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_URL, paramMap);
        // 获取返回值
        returnData = functionalCaseMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

        //复制
        List<FunctionalCaseAttachmentDTO> attachmentDTOS = new ArrayList<>();
        FunctionalCaseAttachmentDTO functionalCaseAttachmentDTO = new FunctionalCaseAttachmentDTO();
        functionalCaseAttachmentDTO.setId("12345677");
        functionalCaseAttachmentDTO.setFileName("测试复制");
        functionalCaseAttachmentDTO.setAssociationId("4444");
        functionalCaseAttachmentDTO.setLocal(true);
        functionalCaseAttachmentDTO.setFileSource("ATTACHMENT");
        functionalCaseAttachmentDTO.setSize(111745L);
        functionalCaseAttachmentDTO.setCreateUser("gyq");
        functionalCaseAttachmentDTO.setCreateTime(System.currentTimeMillis());
        functionalCaseAttachmentDTO.setDeleted(false);
        attachmentDTOS.add(functionalCaseAttachmentDTO);
        request = creatFunctionalCase();
        request.setAttachments(attachmentDTOS);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", new LinkedMultiValueMap<>());
        functionalCaseMvcResult = this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_ADD_URL, paramMap);
        returnData = functionalCaseMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        functionalCase = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalCase.class);
        FunctionalCaseAttachmentExample functionalCaseAttachmentExample = new FunctionalCaseAttachmentExample();
        functionalCaseAttachmentExample.createCriteria().andCaseIdEqualTo(functionalCase.getId()).andFileIdEqualTo("12345677");
        List<FunctionalCaseAttachment> functionalCaseAttachments = functionalCaseAttachmentMapper.selectByExample(functionalCaseAttachmentExample);
        Assertions.assertEquals(1, functionalCaseAttachments.size());

        functionalCaseAttachmentDTO.setId(newFileId);
        attachmentDTOS = new ArrayList<>();
        attachmentDTOS.add(functionalCaseAttachmentDTO);
        request = creatFunctionalCase();
        request.setAttachments(attachmentDTOS);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", new LinkedMultiValueMap<>());
        functionalCaseMvcResult = this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_ADD_URL, paramMap);
        returnData = functionalCaseMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        functionalCase = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalCase.class);
        functionalCaseAttachmentExample = new FunctionalCaseAttachmentExample();
        functionalCaseAttachmentExample.createCriteria().andCaseIdEqualTo(functionalCase.getId()).andFileIdEqualTo(newFileId);
        functionalCaseAttachments = functionalCaseAttachmentMapper.selectByExample(functionalCaseAttachmentExample);
        Assertions.assertEquals(1, functionalCaseAttachments.size());
        byte[] array = new byte[55 * 1024 * 1024];
        file = new MockMultipartFile("file", "file_re-upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, array);
        files.add(file);
        paramMap.add("files", files);
        ResultActions resultActions = this.requestMultipart(FUNCTIONAL_CASE_ADD_URL, paramMap);
        resultActions.andExpect(status().is5xxServerError());
    }

    public String uploadTemp(MultipartFile file) {
        String fileName = StringUtils.trim(file.getOriginalFilename());
        String fileId = IDGenerator.nextStr();
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(fileName);
        String systemTempDir = DefaultRepositoryDir.getSystemTempDir();
        fileRequest.setFolder(systemTempDir + "/" + fileId);
        try {
            FileCenter.getDefaultRepository()
                    .saveFile(file, fileRequest);
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(Translator.get("file_upload_fail"));
        }
        return fileId;
    }

    private static MockMultipartFile getMockMultipartFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file_upload.JPG",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes()
        );
        return file;
    }

    private List<CaseCustomFieldDTO> creatCustomFields() {
        insertCustomField();
        List<CaseCustomFieldDTO> list = new ArrayList<>();
        CaseCustomFieldDTO customFieldDTO = new CaseCustomFieldDTO();
        customFieldDTO.setFieldId("custom_field_id_3");
        customFieldDTO.setValue("custom_field_value_1");
        list.add(customFieldDTO);
        CaseCustomFieldDTO customFieldDTO2 = new CaseCustomFieldDTO();
        customFieldDTO2.setFieldId("custom_field_id_2");
        customFieldDTO2.setValue("custom_field_value_2");
        list.add(customFieldDTO2);
        return list;
    }

    @Test
    @Order(2)
    public void testDefaultTemplateField() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(DEFAULT_TEMPLATE_FIELD_URL + DEFAULT_PROJECT_ID);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    @Test
    @Order(3)
    public void testFunctionalCaseDetail() throws Exception {
        assertErrorCode(this.requestGet(FUNCTIONAL_CASE_DETAIL_URL + "ERROR_TEST_FUNCTIONAL_CASE_ID"), CaseManagementResultCode.FUNCTIONAL_CASE_NOT_FOUND);
        this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_DETAIL_URL + "TEST_FUNCTIONAL_CASE_ID_1");
        MvcResult mvcResult = this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_DETAIL_URL + "TEST_FUNCTIONAL_CASE_ID");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_DETAIL_URL + "TEST_FUNCTIONAL_CASE_ID_2");

        //增加覆盖率
        TemplateDTO templateDTO = projectTemplateService.getTemplateDTOById("21312", "100001100001", TemplateScene.FUNCTIONAL.name());
        List<TemplateCustomFieldDTO> customFields = templateDTO.getCustomFields();
        customFields.forEach(item -> {
            if (Translator.get("custom_field.functional_priority").equals(item.getFieldName())) {
                FunctionalCaseCustomField functionalCaseCustomField = new FunctionalCaseCustomField();
                functionalCaseCustomField.setCaseId("TEST_FUNCTIONAL_CASE_ID_3");
                functionalCaseCustomField.setFieldId(item.getFieldId());
                functionalCaseCustomField.setValue("P3");
                functionalCaseCustomFieldMapper.insertSelective(functionalCaseCustomField);
            }
        });
        this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_DETAIL_URL + "TEST_FUNCTIONAL_CASE_ID_3");
        this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_DETAIL_URL + "wx_test_root");
    }


    private void insertCustomField() {
        CustomField customField = new CustomField();
        customField.setId("custom_field_id_1");
        customField.setName("test_custom_field_id_1");
        customField.setType(CustomFieldType.INPUT.toString());
        customField.setScene(TemplateScene.FUNCTIONAL.name());
        customField.setCreateUser("gyq");
        customField.setCreateTime(System.currentTimeMillis());
        customField.setUpdateTime(System.currentTimeMillis());
        customField.setRefId("test_custom_field_id_1");
        customField.setScopeId(DEFAULT_PROJECT_ID);
        customField.setScopeType(TemplateScopeType.PROJECT.name());
        customField.setInternal(false);
        customField.setEnableOptionKey(false);
        customField.setRemark("1");
        customFieldMapper.insertSelective(customField);
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


    @Test
    @Order(3)
    public void testUpdateFunctionalCase() throws Exception {
        FunctionalCaseEditRequest request = creatEditRequest();
        //设置自定义字段
        List<CaseCustomFieldDTO> list = updateCustomFields(request);
        request.setCustomFields(list);
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        List<MockMultipartFile> files = new ArrayList<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_URL, paramMap);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        //设置删除文件id
        request.setDeleteFileMetaIds(Arrays.asList("TEST_CASE_ATTACHMENT_ID_2"));
        request.setUnLinkFilesIds(Arrays.asList("relate_file_meta_id_1"));
        request.setRelateFileMetaIds(Arrays.asList("relate_file_meta_id_1", "relate_file_meta_id_2"));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        MvcResult updateResult = this.requestMultipartWithOkAndReturn(FUNCTIONAL_CASE_UPDATE_URL, paramMap);
        String updateReturnData = updateResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder updateResultHolder = JSON.parseObject(updateReturnData, ResultHolder.class);
        Assertions.assertNotNull(updateResultHolder);

        FunctionalCaseEditRequest editRequest = new FunctionalCaseEditRequest();
        editRequest.setProjectId("WX_PROJECT_ID");
        editRequest.setModuleId("TEST_MODULE_ID");
        editRequest.setId("WX_TEST_FUNCTIONAL_CASE_ID");
        editRequest.setTemplateId("default_template_id");
        editRequest.setName("测试更新评审状态");
        editRequest.setCaseEditType("STEP");
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(editRequest));
        paramMap.add("files", files);
        this.requestMultipart(FUNCTIONAL_CASE_UPDATE_URL, paramMap);


        editRequest.setSteps("123141243");
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(editRequest));
        paramMap.add("files", files);
        this.requestMultipart(FUNCTIONAL_CASE_UPDATE_URL, paramMap);

        editRequest.setTextDescription("adfadsasfdf");
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(editRequest));
        paramMap.add("files", files);
        this.requestMultipart(FUNCTIONAL_CASE_UPDATE_URL, paramMap);
    }

    private List<CaseCustomFieldDTO> updateCustomFields(FunctionalCaseEditRequest editRequest) {
        List<CaseCustomFieldDTO> list = new ArrayList<>() {{
            add(new CaseCustomFieldDTO() {{
                setFieldId("custom_field_id_1");
                setValue("测试更新");
            }});
            add(new CaseCustomFieldDTO() {{
                setFieldId("custom_field_id_2");
                setValue("更新时存在新字段");
            }});
        }};
        return list;
    }

    private FunctionalCaseEditRequest creatEditRequest() {
        FunctionalCaseEditRequest editRequest = new FunctionalCaseEditRequest();
        editRequest.setProjectId(DEFAULT_PROJECT_ID);
        editRequest.setTemplateId("default_template_id");
        editRequest.setName("测试用例编辑");
        editRequest.setCaseEditType("STEP");
        editRequest.setModuleId("default_module_id");
        editRequest.setId("TEST_FUNCTIONAL_CASE_ID");
        editRequest.setSteps("");
        return editRequest;
    }


    @Test
    @Order(4)
    public void testEditFollower() throws Exception {
        FunctionalCaseFollowerRequest functionalCaseFollowerRequest = new FunctionalCaseFollowerRequest();
        functionalCaseFollowerRequest.setFunctionalCaseId("TEST_FUNCTIONAL_CASE_ID");
        functionalCaseFollowerRequest.setUserId("admin");
        //关注
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_EDIT_FOLLOWER_URL, functionalCaseFollowerRequest);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        //取消关注
        MvcResult editMvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_EDIT_FOLLOWER_URL, functionalCaseFollowerRequest);
        String editReturnData = editMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder editResultHolder = JSON.parseObject(editReturnData, ResultHolder.class);
        Assertions.assertNotNull(editResultHolder);
    }

    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;

    @Test
    @Order(5)
    public void testGetPageList() throws Exception {
        FunctionalCasePageRequest request = new FunctionalCasePageRequest();
        request.setProjectId("test_project_id");
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPost(FUNCTIONAL_CASE_LIST_URL, request);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_LIST_URL, request);
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
        MvcResult mvcResultPage = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_LIST_URL, request);
        Pager<List<FunctionalCasePageDTO>> tableData = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResultPage.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);

        MvcResult moduleCountMvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_MODULE_COUNT, request);
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

        {
            //测试count接口的入参中包含不存在的测试计划、存在的开启/关闭了重复用例的测试计划
            TestPlanConfig testPlanConfig = new TestPlanConfig();
            testPlanConfig.setTestPlanId(IDGenerator.nextStr());
            testPlanConfig.setRepeatCase(false);
            testPlanConfig.setAutomaticStatusUpdate(false);
            testPlanConfig.setPassThreshold(100.00);

            request.setTestPlanId(testPlanConfig.getTestPlanId());

            mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_MODULE_COUNT, request);
            returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            Assertions.assertNotNull(resultHolder);
            moduleCount = JSON.parseObject(JSON.toJSONString(
                            JSON.parseObject(moduleCountMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                    Map.class);
            Assertions.assertTrue(moduleCount.containsKey("all"));

            //不开启用例重复的测试计划入库，再次调用
            testPlanConfigMapper.insertSelective(testPlanConfig);

            mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_MODULE_COUNT, request);
            returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            Assertions.assertNotNull(resultHolder);
            moduleCount = JSON.parseObject(JSON.toJSONString(
                            JSON.parseObject(moduleCountMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                    Map.class);
            Assertions.assertTrue(moduleCount.containsKey("all"));

            //开启用例重复的测试计划，再次调用
            testPlanConfig.setRepeatCase(true);
            testPlanConfigMapper.updateByPrimaryKeySelective(testPlanConfig);

            mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_MODULE_COUNT, request);
            returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            Assertions.assertNotNull(resultHolder);
            moduleCount = JSON.parseObject(JSON.toJSONString(
                            JSON.parseObject(moduleCountMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                    Map.class);
            Assertions.assertTrue(moduleCount.containsKey("all"));

            //使用完后删除该数据
            testPlanConfigMapper.deleteByPrimaryKey(testPlanConfig.getTestPlanId());

        }
    }


    @Test
    @Order(19)
    public void testDeleteFunctionalCase() throws Exception {
        FunctionalCaseDeleteRequest request = new FunctionalCaseDeleteRequest();
        request.setId("TEST_FUNCTIONAL_CASE_ID");
        request.setDeleteAll(false);
        request.setProjectId(DEFAULT_PROJECT_ID);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_DELETE_URL, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request.setId("TEST_FUNCTIONAL_CASE_ID_1");
        request.setDeleteAll(false);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_DELETE_URL, request);
        request.setId("TEST_FUNCTIONAL_CASE_ID_3");
        request.setDeleteAll(true);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_DELETE_URL, request);
    }


    @Test
    @Order(20)
    public void testBatchDelete() throws Exception {
        FunctionalCaseBatchRequest request = new FunctionalCaseBatchRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_DELETE_URL, request);

        request.setSelectIds(Arrays.asList("TEST_FUNCTIONAL_CASE_ID_5", "TEST_FUNCTIONAL_CASE_ID_7"));
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_DELETE_URL, request);
        request.setSelectAll(true);
        request.setDeleteAll(false);
        request.setExcludeIds(Arrays.asList("TEST_FUNCTIONAL_CASE_ID_2"));
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_DELETE_URL, request);
    }

    @Test
    @Order(7)
    public void testTableCustomField() throws Exception {
        this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_TABLE_URL + DEFAULT_PROJECT_ID);
    }

    @Test
    @Order(4)
    public void testBatchMove() throws Exception {
        FunctionalCaseBatchMoveRequest request = new FunctionalCaseBatchMoveRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setModuleId("TEST_MOVE_MODULE_ID");
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_MOVE_URL, request);
        request.setSelectAll(true);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_MOVE_URL, request);
    }


    @Test
    @Order(2)
    public void testBatchCopy() throws Exception {
        FunctionalCaseBatchMoveRequest request = new FunctionalCaseBatchMoveRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setModuleId("TEST_MOVE_MODULE_ID");
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_COPY_URL, request);
        request.setSelectIds(Arrays.asList("TEST"));
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_COPY_URL, request);
        request.setSelectIds(new ArrayList<>());
        request.setSelectAll(true);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_COPY_URL, request);
    }

    @Test
    @Order(2)
    public void testVersion() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_VERSION_URL + "TEST_FUNCTIONAL_CASE_ID");
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(2)
    public void testBatchEdit() throws Exception {
        FunctionalCaseBatchEditRequest request = new FunctionalCaseBatchEditRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setAppend(false);
        request.setSelectAll(false);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_EDIT_URL, request);
        request.setSelectIds(Arrays.asList("TEST_FUNCTIONAL_CASE_ID_1", "TEST_FUNCTIONAL_CASE_ID_2"));
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_EDIT_URL, request);
        request.setAppend(true);
        request.setTags(Arrays.asList("追加标签_1", "追加标签_2"));
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_EDIT_URL, request);
        request.setTags(Arrays.asList("追加标签_1", "追加标签_2","追加标签_3","追加标签_4","追加标签_5","追加标签_6","追加标签_7","追加标签_8","追加标签_9","追加标签_10","追加标签_11"));
        this.requestPost(FUNCTIONAL_CASE_BATCH_EDIT_URL, request);
        request.setAppend(false);
        request.setTags(Arrays.asList("覆盖标签1", "覆盖标签2"));
        request.setSelectAll(true);
        CaseCustomFieldDTO caseCustomFieldDTO = new CaseCustomFieldDTO();
        caseCustomFieldDTO.setFieldId("TEST_FIELD_ID_1");
        caseCustomFieldDTO.setValue("批量编辑自定义字段");
        request.setCustomField(caseCustomFieldDTO);
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_BATCH_EDIT_URL, request);
    }


    @Test
    @Order(18)
    public void testPos() throws Exception {
        PosRequest posRequest = new PosRequest();
        posRequest.setProjectId(DEFAULT_PROJECT_ID);
        posRequest.setTargetId("TEST_FUNCTIONAL_CASE_ID_2");
        posRequest.setMoveId("TEST_FUNCTIONAL_CASE_ID_6");
        posRequest.setMoveMode("AFTER");
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_POS_URL, posRequest);

        posRequest.setMoveMode("BEFORE");
        this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_POS_URL, posRequest);

    }


    @Test
    @Order(19)
    public void testDownloadExcelTemplate() throws Exception {
        this.requestGetExcel(DOWNLOAD_EXCEL_TEMPLATE_URL + DEFAULT_PROJECT_ID);

    }

    private MvcResult requestGetExcel(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk()).andReturn();
    }


    @Test
    @Order(18)
    public void testImportCheckExcel() throws Exception {
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/1.xlsx")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "11.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath));
        FunctionalCaseImportRequest request = new FunctionalCaseImportRequest();
        request.setCover(false);
        request.setProjectId("100001100001");
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file);
        MvcResult functionalCaseMvcResult = this.requestMultipartWithOkAndReturn(CHECK_EXCEL_URL, paramMap);

        String functionalCaseImportResponseData = functionalCaseMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder functionalCaseResultHolder = JSON.parseObject(functionalCaseImportResponseData, ResultHolder.class);
        FunctionalCaseImportResponse functionalCaseImportResponse = JSON.parseObject(JSON.toJSONString(functionalCaseResultHolder.getData()), FunctionalCaseImportResponse.class);
        Assertions.assertNotNull(functionalCaseImportResponse);


        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipart(CHECK_EXCEL_URL, paramMap);

        //覆盖异常
        String filePath2 = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/2.xlsx")).getPath();
        MockMultipartFile file2 = new MockMultipartFile("file", "22.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath2));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file2);
        this.requestMultipart(CHECK_EXCEL_URL, paramMap);
    }


    @Test
    @Order(19)
    public void testImportExcel() throws Exception {
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/3.xlsx")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "11.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath));
        FunctionalCaseImportRequest request = new FunctionalCaseImportRequest();
        request.setCover(true);
        request.setProjectId("100001100001");
        request.setCount("1");
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file);
        MvcResult functionalCaseMvcResult = this.requestMultipartWithOkAndReturn(IMPORT_EXCEL_URL, paramMap);

        String functionalCaseImportResponseData = functionalCaseMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder functionalCaseResultHolder = JSON.parseObject(functionalCaseImportResponseData, ResultHolder.class);
        FunctionalCaseImportResponse functionalCaseImportResponse = JSON.parseObject(JSON.toJSONString(functionalCaseResultHolder.getData()), FunctionalCaseImportResponse.class);
        Assertions.assertNotNull(functionalCaseImportResponse);

        String filePath5 = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/5.xlsx")).getPath();
        MockMultipartFile file5 = new MockMultipartFile("file", "15.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath5));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file5);
        this.requestMultipart(IMPORT_EXCEL_URL, paramMap);

        String filePath1 = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/4.xlsx")).getPath();
        MockMultipartFile file1 = new MockMultipartFile("file", "14.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath1));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file1);
        this.requestMultipart(IMPORT_EXCEL_URL, paramMap);


        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipart(IMPORT_EXCEL_URL, paramMap);

        //覆盖异常
        String filePath2 = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/2.xlsx")).getPath();
        MockMultipartFile file2 = new MockMultipartFile("file", "22.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileBaseUtils.getFileBytes(filePath2));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("file", file2);
        this.requestMultipart(IMPORT_EXCEL_URL, paramMap);
    }

    @Test
    @Order(22)
    public void operationHistoryList() throws Exception {
        OperationHistoryRequest request = new OperationHistoryRequest();
        request.setSourceId(functionalCaseId);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("createTime", "asc"));

        MvcResult mvcResult = this.requestPostWithOkAndReturn(OPERATION_HISTORY_URL, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), request.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= request.getPageSize());
        request.setSort(Map.of());
        this.requestPost(OPERATION_HISTORY_URL, request);
        List<OperationHistoryDTO> operationHistoryDTOS = operationHistoryService.listWidthTable(request, "functional_case");
        Assertions.assertTrue(CollectionUtils.isNotEmpty(operationHistoryDTOS));

    }
}
