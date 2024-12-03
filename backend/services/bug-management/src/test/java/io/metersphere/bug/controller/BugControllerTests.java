package io.metersphere.bug.controller;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugExample;
import io.metersphere.bug.domain.BugLocalAttachment;
import io.metersphere.bug.domain.BugLocalAttachmentExample;
import io.metersphere.bug.dto.BugExportColumn;
import io.metersphere.bug.dto.request.*;
import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.metersphere.bug.dto.response.BugDTO;
import io.metersphere.bug.mapper.BugLocalAttachmentMapper;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.bug.service.BugService;
import io.metersphere.bug.service.BugSyncExtraService;
import io.metersphere.bug.service.BugSyncService;
import io.metersphere.plugin.platform.dto.request.SyncAllBugRequest;
import io.metersphere.project.domain.*;
import io.metersphere.project.dto.ProjectTemplateOptionDTO;
import io.metersphere.project.mapper.FileAssociationMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldExample;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.dto.request.PluginUpdateRequest;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import io.metersphere.system.service.FileService;
import io.metersphere.system.service.PluginService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugControllerTests extends BaseTest {

    public static final String BUG_HEADER_CUSTOM_FIELD = "/bug/header/custom-field";
    public static final String BUG_HEADER_COLUMNS_OPTION = "/bug/header/columns-option";
    public static final String BUG_PAGE = "/bug/page";
    public static final String BUG_EDIT_POS = "/bug/edit/pos";
    public static final String BUG_ADD = "/bug/add";
    public static final String BUG_UPDATE = "/bug/update";
    public static final String BUG_DETAIL = "/bug/get";
    public static final String BUG_DELETE = "/bug/delete";
    public static final String BUG_TEMPLATE_OPTION = "/bug/template/option";
    public static final String BUG_TEMPLATE_DETAIL = "/bug/template/detail";
    public static final String BUG_BATCH_DELETE = "/bug/batch-delete";
    public static final String BUG_BATCH_UPDATE = "/bug/batch-update";
    public static final String BUG_FOLLOW = "/bug/follow";
    public static final String BUG_UN_FOLLOW = "/bug/unfollow";
    public static final String BUG_SYNC = "/bug/sync";
    public static final String BUG_SYNC_ALL = "/bug/sync/all";
    public static final String BUG_SYNC_CHECK = "/bug/sync/check";
    public static final String BUG_EXPORT_COLUMNS = "/bug/export/columns/%s";
    public static final String BUG_EXPORT = "/bug/export";
    public static final String BUG_CURRENT_PLATFORM = "/bug/current-platform/%s";
    public static final String BUG_EXIST_CHECK = "/bug/check-exist/%s";

    @Resource
    private PluginService pluginService;
    @Resource
    private BugMapper bugMapper;
    @Resource
    private BugLocalAttachmentMapper bugLocalAttachmentMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private CustomFieldMapper customFieldMapper;
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private FileService fileService;
    @Resource
    private BugSyncService bugSyncService;
    @Resource
    private BugSyncExtraService bugSyncExtraService;
    @Resource
    private BugService bugService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ServiceIntegrationMapper serviceIntegrationMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_bug.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void prepareData() throws Exception {
        // 插入准备的文件
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(DefaultRepositoryDir.getFileManagementDir("default-project-for-bug"));
        fileRequest.setFileName("default-bug-file-id-1");
        fileRequest.setStorage(StorageType.MINIO.name());
        fileService.upload(getMockFile(), fileRequest);
    }

    @Test
    @Order(1)
    void testBugPageSuccess() throws Exception {
        // 表头字段, 状态选项, 处理人选项
        this.requestGetWithOk(BUG_HEADER_CUSTOM_FIELD + "/default-project-for-bug");
        this.requestGetWithOk(BUG_HEADER_COLUMNS_OPTION + "/default-project-for-bug");
        BugPageRequest bugRequest = new BugPageRequest();
        bugRequest.setCurrent(1);
        bugRequest.setPageSize(10);
        bugRequest.setKeyword("default");
        bugRequest.setProjectId("default-project-for-bug");
        bugRequest.setFilter(buildRequestFilter());
        bugRequest.setCombine(buildRequestCombine());
        MvcResult mvcResult = this.requestPostWithOkAndReturn(BUG_PAGE, bugRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), bugRequest.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= bugRequest.getPageSize());
        // 返回值中取出第一条数据, 并判断是否包含关键字default
        BugDTO bugDTO = JSON.parseArray(JSON.toJSONString(pageData.getList()), BugDTO.class).getFirst();
        Assertions.assertTrue(StringUtils.contains(bugDTO.getTitle(), bugRequest.getKeyword())
                || StringUtils.contains(bugDTO.getId(), bugRequest.getKeyword()));

        // sort不为空
        Map<String, String> sort = new HashMap<>();
        sort.put("id", "desc");
        bugRequest.setSort(sort);
        MvcResult sortResult = this.requestPostWithOkAndReturn(BUG_PAGE, bugRequest);
        String sortData = sortResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        Pager<?> sortPageData = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), Pager.class);
        // 返回值中取出第一条ID最大的数据, 并判断是否是default-bug
        BugDTO maxBugDTO = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), BugDTO.class).getFirst();
        Assertions.assertTrue(maxBugDTO.getId().contains("default"));

        // 拖拽
        PosRequest posRequest = new PosRequest();
        posRequest.setProjectId("default-project-for-bug");
        posRequest.setMoveId("default-bug-id");
        posRequest.setMoveMode("AFTER");
        posRequest.setTargetId("default-bug-id-tapd1");
        this.requestPost(BUG_EDIT_POS, posRequest);
    }

    @Test
    @Order(2)
    void testBugPageEmptySuccess() throws Exception {
        BugPageRequest bugPageRequest = new BugPageRequest();
        bugPageRequest.setCurrent(1);
        bugPageRequest.setPageSize(10);
        bugPageRequest.setKeyword("default-x");
        bugPageRequest.setProjectId("default-project-for-bug");
        bugPageRequest.setCreateByMe(true);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(BUG_PAGE, bugPageRequest);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), bugPageRequest.getCurrent());
        // 返回的数据量为0条
        Assertions.assertEquals(0, pageData.getTotal());
        // cover filter
        Map<String, List<String>> filter = new HashMap<>();
        filter.put("handleUser", List.of("admin"));
        filter.put("custom_multiple_test_field", null);
        bugPageRequest.setFilter(filter);
        bugPageRequest.setCombine(null);
        this.requestPostWithOkAndReturn(BUG_PAGE, bugPageRequest);
    }

    @Test
    @Order(3)
    void testBugPageError() throws Exception {
        // 页码有误
        BugPageRequest bugPageRequest = new BugPageRequest();
        bugPageRequest.setCurrent(0);
        bugPageRequest.setPageSize(10);
        this.requestPost(BUG_PAGE, bugPageRequest, status().isBadRequest());
        // 页数有误
        bugPageRequest = new BugPageRequest();
        bugPageRequest.setCurrent(1);
        bugPageRequest.setPageSize(1);
        this.requestPost(BUG_PAGE, bugPageRequest, status().isBadRequest());
    }

    @Test
    @Order(4)
    void testAddBugSuccess() throws Exception {
        BugEditRequest request = buildRequest(false);
        request.setDescription(null);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMap = getMultiPartParam(request, file);
        this.requestMultipartWithOkAndReturn(BUG_ADD, paramMap);

        request.setCaseId("test");
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(BUG_ADD, paramMap);

        request.setCaseId("test-case-1");
        request.setTestPlanId("test-plan-1");
        request.setTestPlanCaseId("test-plan-case-1");
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(BUG_ADD, paramMap);
    }

    @Test
    @Order(5)
    void testAddBugError() throws Exception {
        BugEditRequest request = buildRequest(false);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        // 项目ID为空
        request.setProjectId(null);
        MultiValueMap<String, Object> paramMap = getMultiPartParam(request, file);
        this.requestMultipart(BUG_ADD, paramMap).andExpect(status().isBadRequest());
        request.setProjectId("default-project-for-bug");
        // V3.1需求：标签超过10个不会报错，只会留下前10个
        request.setTags(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"));
        paramMap = getMultiPartParam(request, file);
        this.requestMultipart(BUG_ADD, paramMap).andExpect(status().is2xxSuccessful());
        request.setTags(null);
        // 处理人为空
        request.setTitle("default-bug-title");
        List<BugCustomFieldDTO> noHandleUser = request.getCustomFields().stream().filter(field -> !StringUtils.equals(field.getId(), "handleUser")).toList();
        request.setCustomFields(noHandleUser);
        paramMap = getMultiPartParam(request, file);
        this.requestMultipart(BUG_ADD, paramMap).andExpect(status().is5xxServerError());
        // 模板为空
        request.setTemplateId(null);
        paramMap = getMultiPartParam(request, file);
        this.requestMultipart(BUG_ADD, paramMap).andExpect(status().isBadRequest());
        // 状态为空
        request.setTemplateId("default-bug-template");
        List<BugCustomFieldDTO> noStatus = request.getCustomFields().stream().filter(field -> !StringUtils.equals(field.getId(), "status")).toList();
        request.setCustomFields(noStatus);
        paramMap = getMultiPartParam(request, file);
        this.requestMultipart(BUG_ADD, paramMap).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(6)
    void testUpdateBugSuccess() throws Exception {
        BugEditRequest request = buildRequest(true);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMap = getMultiPartParam(request, file);
        this.requestMultipartWithOkAndReturn(BUG_UPDATE, paramMap);
        // 第二次更新, no-file
        MultiValueMap<String, Object> noFileParamMap = new LinkedMultiValueMap<>();
        request.setLinkFileIds(null);
        request.setUnLinkRefIds(null);
        request.setDeleteLocalFileIds(null);
        request.setDescription("1111");
        noFileParamMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(BUG_UPDATE, noFileParamMap);
        this.requestGetWithOk(BUG_DETAIL + "/" + request.getId());
    }

    @Test
    @Order(7)
    void testUpdateBugError() throws Exception {
        BugEditRequest request = buildRequest(true);
        request.setId("default-bug-id-not-exist");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMap = getMultiPartParam(request, file);
        this.requestMultipart(BUG_UPDATE, paramMap).andExpect(status().is5xxServerError());

        // 标签超过10个
        request = buildRequest(true);
        request.setTags(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"));
        paramMap = getMultiPartParam(request, file);
        this.requestMultipart(BUG_UPDATE, paramMap).andExpect(status().is5xxServerError());

    }

    @Test
    @Order(8)
    void testUpdateBugWithEmptyField() throws Exception {
        BugEditRequest request = buildRequest(true);
        List<BugCustomFieldDTO> statusAndHandleUser = request.getCustomFields().stream().filter(field -> StringUtils.equalsAny(field.getId(), "status", "handleUser")).toList();
        request.setCustomFields(statusAndHandleUser);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        request.setLinkFileIds(null);
        request.setUnLinkRefIds(null);
        request.setDeleteLocalFileIds(null);
        MultiValueMap<String, Object> paramMap = getMultiPartParam(request, file);
        this.requestMultipartWithOkAndReturn(BUG_UPDATE, paramMap);
    }

    @Test
    @Order(9)
    void testGetBugTemplateOption() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(BUG_TEMPLATE_OPTION + "/default-project-for-bug");
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(sortData, ResultHolder.class);
        List<ProjectTemplateOptionDTO> templateOptionDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), ProjectTemplateOptionDTO.class);
        // 默认模板断言
        Assertions.assertTrue(templateOptionDTOS.stream().anyMatch(ProjectTemplateOptionDTO::getEnableDefault));
    }

    @Test
    @Order(10)
    void testGetBugTemplateDetailSuccess() throws Exception {
        BugTemplateRequest request = new BugTemplateRequest();
        request.setId("default-bug-template-id");
        request.setProjectId("default-project-for-bug");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(BUG_TEMPLATE_DETAIL, request);
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(sortData, ResultHolder.class);
        TemplateDTO templateDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TemplateDTO.class);
        Assertions.assertNotNull(templateDTO);
        Assertions.assertEquals("default-bug-template-id", templateDTO.getId());
        // 编辑时的模板详情
        // 覆盖状态流
        request.setFromStatusId("1");
        this.requestPostWithOk(BUG_TEMPLATE_DETAIL, request);
        request.setFromStatusId("2");
        this.requestPostWithOk(BUG_TEMPLATE_DETAIL, request);
        request.setProjectId("no-status-project");
        request.setId("no-status-template");
        request.setFromStatusId(null);
        this.requestPostWithOk(BUG_TEMPLATE_DETAIL, request);
    }

    @Test
    @Order(11)
    void testBatchUpdateBugSuccess() throws Exception {
        BugBatchUpdateRequest request = new BugBatchUpdateRequest();
        request.setProjectId("default-project-for-bug");
        // 全选, 编辑所有数据
        request.setSelectAll(true);
        request.setSelectIds(List.of("test"));
        // TAG追加
        request.setTags(List.of("TAG", "TEST_TAG"));
        request.setAppend(true);
        request.setClear(false);
        this.requestPost(BUG_BATCH_UPDATE, request, status().isOk());
        // TAG覆盖
        request.setExcludeIds(List.of("default-bug-id-tapd1"));
        request.setTags(List.of("A", "B"));
        request.setAppend(false);
        request.setClear(false);
        this.requestPost(BUG_BATCH_UPDATE, request, status().isOk());
        // 勾选部分
        request.setSelectAll(false);
        request.setSelectIds(List.of("default-bug-id"));
        this.requestPost(BUG_BATCH_UPDATE, request, status().isOk());
        //TAG追加 清空
        Bug bug = bugMapper.selectByPrimaryKey("default-bug-id");
        Assertions.assertEquals(2, bug.getTags().size());
        request.setAppend(false);
        request.setClear(true);
        this.requestPost(BUG_BATCH_UPDATE, request, status().isOk());
        bug = bugMapper.selectByPrimaryKey("default-bug-id");
        Assertions.assertTrue(CollectionUtils.isEmpty(bug.getTags()));



    }

    @Test
    @Order(12)
    void testBatchUpdateEmptyBugSuccess() throws Exception {
        BugBatchUpdateRequest request = new BugBatchUpdateRequest();
        request.setProjectId("default-project-for-bug-no-data");
        // 全选, 空数据
        request.setSelectAll(true);
        request.setSelectIds(List.of("test"));
        request.setTags(List.of("TAG", "TEST_TAG"));
        request.setAppend(true);
        this.requestPost(BUG_BATCH_UPDATE, request, status().is5xxServerError());
        // 取消全选, 空数据
        request.setSelectAll(false);
        request.setSelectIds(null);
        this.requestPost(BUG_BATCH_UPDATE, request, status().is5xxServerError());
    }

    @Test
    @Order(13)
    void testExportColumns() throws Exception {
        this.requestGetWithOkAndReturn(String.format(BUG_EXPORT_COLUMNS, "default-project-for-bug"));
        //校验权限
        this.requestGetPermissionTest(PermissionConstants.PROJECT_BUG_READ, String.format(BUG_EXPORT_COLUMNS, DEFAULT_PROJECT_ID));
    }

    @Test
    @Order(14)
    void testExportBugs() throws Exception {
        BugExportRequest request = new BugExportRequest();
        request.setProjectId("default-project-for-bug");
        request.setSelectAll(true);
        List<BugExportColumn> exportColumns = new ArrayList<>();
        exportColumns.add(new BugExportColumn("name", "名称", "system"));
        exportColumns.add(new BugExportColumn("id", "ID", "system"));
        exportColumns.add(new BugExportColumn("content", "缺内容", "system"));
        exportColumns.add(new BugExportColumn("status", "陷状态", "system"));
        exportColumns.add(new BugExportColumn("handleUser", "处理人儿", "system"));
        exportColumns.add(new BugExportColumn("createUser", "创建人儿", "other"));
        exportColumns.add(new BugExportColumn("createTime", "搞定时间", "other"));
        exportColumns.add(new BugExportColumn("caseCount", "用例量", "other"));
        exportColumns.add(new BugExportColumn("comment", "评论", "other"));
        exportColumns.add(new BugExportColumn("platform", "平台", "other"));
        request.setExportColumns(exportColumns);

        MvcResult result = this.requestPostDownloadFile(BUG_EXPORT, null, request);
        byte[] bytes = result.getResponse().getContentAsByteArray();
        Assertions.assertTrue(bytes.length > 0);

        // 非Local的缺陷导出
        request.setProjectId("default-project-for-bug-no-local");
        result = this.requestPostDownloadFile(BUG_EXPORT, null, request);
        bytes = result.getResponse().getContentAsByteArray();
        Assertions.assertTrue(bytes.length > 0);

        // 勾选部分
        request.setSelectAll(false);
        request.setSelectIds(List.of("default-bug-id-single"));
        result = this.requestPostDownloadFile(BUG_EXPORT, null, request);
        bytes = result.getResponse().getContentAsByteArray();
        Assertions.assertTrue(bytes.length > 0);

        //不存在的ID
        request.setSelectIds(List.of(IDGenerator.nextStr()));
        this.requestPost(BUG_EXPORT, request).andExpect(status().is5xxServerError());

        //没有数据
        request = new BugExportRequest();
        request.setProjectId("default-project-for-bug");
        request.setSelectAll(false);
        request.setExportColumns(exportColumns);
        this.requestPost(BUG_EXPORT, request).andExpect(status().is5xxServerError());

        //测试权限
        request = new BugExportRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(true);
        request.setExportColumns(exportColumns);
        this.requestPostPermissionTest(PermissionConstants.PROJECT_BUG_EXPORT, BUG_EXPORT, request);
    }

    @Test
    @Order(15)
    void testCurrentPlatform() throws Exception {
        this.requestGetWithOk(String.format(BUG_CURRENT_PLATFORM, "default-project-for-bug"));
    }

    @Test
    @Order(16)
    void testCheckBugExist() throws Exception {
        this.requestGetWithOk(String.format(BUG_EXIST_CHECK, "default-project-for-bug-not-exist"));
    }

    @Test
    @Order(90)
    void testDeleteBugSuccess() throws Exception {
        // Local
        this.requestGet(BUG_DELETE + "/default-bug-id", status().isOk());
        // Tapd
        this.requestGet(BUG_DELETE + "/default-bug-id-tapd1", status().isOk());
    }

    @Test
    @Order(91)
    void testDeleteBugError() throws Exception {
        this.requestGet(BUG_DELETE + "/default-bug-id-not-exist", status().is5xxServerError());
    }

    @Test
    @Order(92)
    void testFollowBug() throws Exception {
        // 关注的缺陷存在
        this.requestGet(BUG_FOLLOW + "/default-bug-id-single", status().isOk());
        // 关注的缺陷不存在
        this.requestGet(BUG_FOLLOW + "/default-bug-id-not-exist", status().is5xxServerError());
    }

    @Test
    @Order(93)
    void testUnFollowBug() throws Exception {
        // 取消关注的缺陷存在
        this.requestGet(BUG_UN_FOLLOW + "/default-bug-id-single", status().isOk());
        // 取消关注的缺陷不存在
        this.requestGet(BUG_UN_FOLLOW + "/default-bug-id-not-exist", status().is5xxServerError());
    }

    @Test
    @Order(94)
    void testBatchDeleteBugSuccess() throws Exception {
        BugBatchRequest request = new BugBatchRequest();
        request.setProjectId("default-project-for-bug");
        // 全选, 删除所有
        request.setSelectAll(true);
        request.setSelectIds(List.of("test"));
        request.setExcludeIds(List.of("default-bug-id-jira-delete", "default-bug-id-jira-sync", "default-bug-id-jira-sync-1"));
        this.requestPost(BUG_BATCH_DELETE, request, status().is5xxServerError());
    }

    @Test
    @Order(95)
    void coverPlatformTemplateTests() throws Exception {
        // 覆盖同步缺陷(Local)
        this.requestGetWithOk(BUG_SYNC + "/default-project-for-not-integration");

        // 上传Jira插件
        addJiraPlugin();

        // 获取Jira默认模板
        BugTemplateRequest request = new BugTemplateRequest();
        request.setId("jira");
        request.setProjectId("default-project-for-bug");
        this.requestPostWithOk(BUG_TEMPLATE_DETAIL, request);
        // 获取MS默认模板
        request.setId("default-bug-template-not-exist");
        request.setProjectId("default-project-for-bug");
        this.requestPostWithOk(BUG_TEMPLATE_DETAIL, request);
        // 关闭插件集成
        ServiceIntegration record = new ServiceIntegration();
        record.setId("621103810617344");
        record.setEnable(false);
        serviceIntegrationMapper.updateByPrimaryKeySelective(record);
        this.requestPost(BUG_TEMPLATE_DETAIL, request, status().is5xxServerError());
        // 获取处理人选项(Local)
        this.requestGetWithOk(BUG_HEADER_COLUMNS_OPTION + "/default-project-for-bug");
        // 开启插件集成
        record.setEnable(true);
        serviceIntegrationMapper.updateByPrimaryKeySelective(record);
        this.requestPostWithOk(BUG_TEMPLATE_DETAIL, request);
        // 关闭同步
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo("default-project-for-bug").andTypeEqualTo("BUG_SYNC_PLATFORM_KEY");
        projectApplicationMapper.deleteByExample(example);
        this.requestPostWithOk(BUG_TEMPLATE_DETAIL, request);
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId("default-project-for-bug");
        projectApplication.setType("BUG_SYNC_PLATFORM_KEY");
        projectApplication.setTypeValue("jira");
        projectApplicationMapper.insert(projectApplication);
    }

    @Test
    @Order(96)
    void coverPlatformBugSyncTests() throws Exception {
        // 获取默认模板缺陷详情
        this.requestGetWithOk(BUG_DETAIL + "/default-bug-id-jira-sync");
        // 表头字段, 状态选项, 处理人选项 (非Local平台)
        this.requestGetWithOk(BUG_HEADER_CUSTOM_FIELD + "/default-project-for-bug");
        this.requestGetWithOk(BUG_HEADER_COLUMNS_OPTION + "/default-project-for-bug");

        // 同步并删除的两条缺陷
        this.requestGetWithOk(BUG_SYNC + "/default-project-for-bug");
        bugMapper.deleteByPrimaryKey("default-bug-id-jira-sync-1");

        // 添加Jira缺陷
        BugEditRequest addRequest = buildJiraBugRequest(false);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> addParam = getMultiPartParam(addRequest, file);
        this.requestMultipartWithOkAndReturn(BUG_ADD, addParam);

        BugPageRequest bugPageRequest = new BugPageRequest();
        bugPageRequest.setCurrent(1);
        bugPageRequest.setPageSize(10);
        bugPageRequest.setUnresolved(true);
        bugPageRequest.setAssignedToMe(true);
        bugPageRequest.setProjectId("default-project-for-bug");
        this.requestPostWithOk(BUG_PAGE, bugPageRequest);

        // 更新Jira缺陷
        BugEditRequest updateRequest = buildJiraBugRequest(true);
        updateRequest.setUnLinkRefIds(List.of(getAddJiraAssociateFile().getId()));
        updateRequest.setDeleteLocalFileIds(List.of(getAddJiraLocalFile().getFileId()));
        MultiValueMap<String, Object> updateParma = getMultiPartParam(updateRequest, null);
        this.requestMultipartWithOkAndReturn(BUG_UPDATE, updateParma);
        // 删除Jira缺陷
        this.requestGet(BUG_DELETE + "/" + updateRequest.getId(), status().isOk());

        // 添加使用Jira默认模板的缺陷
        addRequest.setTemplateId("jira");
        BugCustomFieldDTO summary = new BugCustomFieldDTO();
        summary.setId("summary");
        summary.setName("摘要");
        summary.setType("INPUT");
        summary.setValue("这是一个系统Jira模板创建的缺陷");
        addRequest.getCustomFields().add(summary);
        addRequest.setRichTextTmpFileIds(List.of("rich-text-file-id"));
        MultiValueMap<String, Object> addParam3 = getMultiPartParam(addRequest, null);
        this.requestMultipart(BUG_ADD, addParam3).andExpect(status().is5xxServerError());

        // 同步Jira存量缺陷(存量数据为空)
        this.requestGetWithOk(BUG_SYNC + "/default-project-for-bug");

        // 添加没有附件的Jira缺陷
        addRequest.setRichTextTmpFileIds(null);
        addRequest.setLinkFileIds(null);
        addRequest.setTemplateId("default-bug-template-id");
        MultiValueMap<String, Object> addParam2 = getMultiPartParam(addRequest, null);
        this.requestMultipartWithOkAndReturn(BUG_ADD, addParam2);
        this.requestGetWithOk(BUG_SYNC + "/default-project-for-bug");
        // 覆盖Redis-Key还未删除的情况
        this.requestGetWithOk(BUG_SYNC_CHECK + "/default-project-for-bug");
        this.requestGetWithOk(BUG_SYNC + "/default-project-for-bug");

        // 更新没有附件的缺陷
        BugEditRequest updateRequest2 = buildJiraBugRequest(true);
        updateRequest2.setLinkFileIds(List.of("default-bug-file-id-1"));
        MultiValueMap<String, Object> updateParam2 = getMultiPartParam(updateRequest2, file);
        this.requestMultipartWithOkAndReturn(BUG_UPDATE, updateParam2);
        // 同步方法为异步, 所以换成手动调用
        BugExample example = new BugExample();
        example.createCriteria().andIdEqualTo(updateRequest2.getId());
        List<Bug> remainBugs = bugMapper.selectByExample(example);
        Project defaultProject = projectMapper.selectByPrimaryKey("default-project-for-bug");
        // 同步第一次
        bugService.syncPlatformBugs(remainBugs, defaultProject, "admin", Locale.SIMPLIFIED_CHINESE.getLanguage(), Translator.get("sync_mode.manual"));
        // 同步第二次
        renameLocalFile(updateRequest2.getId()); // 重命名后, 同步时会删除本地文件
        bugService.syncPlatformBugs(remainBugs, defaultProject, "admin", Locale.SIMPLIFIED_CHINESE.getLanguage(), Translator.get("sync_mode.manual"));
        // 同步第三次
        deleteLocalFile(updateRequest2.getId()); // 手动删除关联的文件, 重新同步时会下载平台附件
        bugService.syncPlatformBugs(remainBugs, defaultProject, "admin", Locale.SIMPLIFIED_CHINESE.getLanguage(), Translator.get("sync_mode.manual"));

        // 全选删除所有Jira缺陷
        BugBatchRequest request = new BugBatchRequest();
        request.setProjectId("default-project-for-bug");
        request.setSelectAll(true);
        this.requestPost(BUG_BATCH_DELETE, request);

        // 集成配置为空
        addRequest.setProjectId("default-project-for-not-integration");
        MultiValueMap<String, Object> notIntegrationParam = getMultiPartParam(addRequest, file);
        this.requestMultipart(BUG_ADD, notIntegrationParam).andExpect(status().is5xxServerError());

        // 执行同步全部
        Project project = new Project();
        project.setId("default-project-for-bug");
        SyncAllBugRequest syncAllBugRequest = new SyncAllBugRequest();
        syncAllBugRequest.setPre(true);
        syncAllBugRequest.setCreateTime(1702021500000L);
        // 同步后置方法处理为空, 覆盖主工程代码即可
        syncAllBugRequest.setSyncPostProcessFunc((param) -> {
        });
        bugService.execSyncAll(project, syncAllBugRequest);
    }

    @Test
    @Order(97)
    void coverSyncScheduleTests() {
        // 定时同步存量缺陷
        bugSyncService.syncPlatformBugBySchedule("default-project-for-bug", "admin");
        // 异常信息
        bugSyncExtraService.setSyncErrorMsg("default-project-for-bug", "sync error!");
        String syncErrorMsg = bugSyncExtraService.getSyncErrorMsg("default-project-for-bug");
        Assertions.assertEquals(syncErrorMsg, "sync error!");
        bugSyncExtraService.deleteSyncErrorMsg("default-project-for-bug");
    }

    @Test
    @Order(98)
    void coverBugTests() {
        BugCustomFieldDTO field = new BugCustomFieldDTO();
        field.setId("test_field");
        field.setName("test");
        field.setValue("test");
        field.setType("MULTIPLE_SELECT");
        bugService.transferCustomToPlatformField(null, List.of(field), true);
        // 添加没有配置自定义映射字段的Jira缺陷
        removeApiFieldTmp();
        bugService.transferCustomToPlatformField("default-bug-template-id-not-exist", List.of(field), false);
        rollBackApiField();
    }

    @Test
    @Order(99)
    void coverExtraBugTests() throws Exception {
        // 批量删除
        BugBatchRequest request = new BugBatchRequest();
        request.setProjectId("default-project-for-bug");
        request.setSelectAll(false);
        request.setSelectIds(List.of("default-bug-id"));
        this.requestPost(BUG_BATCH_DELETE, request, status().is5xxServerError());

        // check一下同步状态
        bugSyncExtraService.deleteSyncKey("default-project-for-bug");
        bugSyncExtraService.setSyncErrorMsg("default-project-for-bug", "sync error!");
        bugSyncService.checkSyncStatus("default-project-for-bug");
        // 覆盖空Msg
        bugSyncService.checkSyncStatus("default-project-for-bug");

        // 同步全量缺陷
        BugSyncRequest syncRequest = new BugSyncRequest();
        syncRequest.setProjectId("default-project-for-bug");
        syncRequest.setPre(true);
        syncRequest.setCreateTime(1702021500000L);
        bugSyncExtraService.setSyncKey("default-project-for-bug");
        this.requestPostWithOk(BUG_SYNC_ALL, request);
        bugSyncExtraService.deleteSyncKey("default-project-for-bug");
        this.requestPostWithOk(BUG_SYNC_ALL, request);
    }

    /**
     * 生成请求过滤参数
     *
     * @return filter param
     */
    private Map<String, List<String>> buildRequestFilter() {
        Map<String, List<String>> filter = new HashMap<>();
        filter.put("custom_multiple_test_field", List.of("default", "default-1"));
        return filter;
    }

    /**
     * 生成高级搜索参数
     *
     * @return combine param
     */
    private Map<String, Object> buildRequestCombine() {
        Map<String, Object> combine = new HashMap<>();
        List<Map<String, Object>> customs = new ArrayList<>();
        Map<String, Object> custom = new HashMap<>();
        custom.put("id", "test_field");
        custom.put("operator", "in");
        custom.put("type", "array");
        custom.put("value", List.of("default", "default-1"));
        customs.add(custom);
        combine.put("customs", customs);
        return combine;
    }

    /**
     * 生成请求参数
     *
     * @param isUpdate 是否更新操作
     * @return 请求参数
     */
    private BugEditRequest buildRequest(boolean isUpdate) {
        BugEditRequest request = new BugEditRequest();
        request.setProjectId("default-project-for-bug");
        request.setTitle("default-bug-title");
        request.setDescription("default-bug-description");
        request.setTemplateId("default-bug-template");
        request.setLinkFileIds(List.of("default-bug-file-id-1"));
        if (isUpdate) {
            request.setId("default-bug-id");
            request.setUnLinkRefIds(List.of("default-file-association-id"));
            request.setDeleteLocalFileIds(List.of("default-bug-file-id"));
            request.setLinkFileIds(List.of("default-bug-file-id-2"));
        }
        BugCustomFieldDTO fieldDTO1 = new BugCustomFieldDTO();
        fieldDTO1.setId("custom-field");
        fieldDTO1.setName("oasis");
        BugCustomFieldDTO fieldDTO2 = new BugCustomFieldDTO();
        fieldDTO2.setId("test_field");
        fieldDTO2.setName(JSON.toJSONString(List.of("test")));
        BugCustomFieldDTO handleUserField = new BugCustomFieldDTO();
        handleUserField.setId("handleUser");
        handleUserField.setName("处理人");
        handleUserField.setValue("admin");
        BugCustomFieldDTO statusField = new BugCustomFieldDTO();
        statusField.setId("status");
        statusField.setName("状态");
        statusField.setValue("1");
        request.setCustomFields(List.of(fieldDTO1, fieldDTO2, handleUserField, statusField));
        return request;
    }

    /**
     * 生成添加Jira缺陷的请求参数
     *
     * @param isUpdate 是否更新
     * @return 缺陷编辑请求参数
     */
    private BugEditRequest buildJiraBugRequest(boolean isUpdate) {
        BugEditRequest request = new BugEditRequest();
        request.setProjectId("default-project-for-bug");
        request.setTitle("这是一个系统Jira模板创建的缺陷");
        request.setDescription("这是一段缺陷内容!!!!");
        request.setTemplateId("default-bug-template-id");
        BugCustomFieldDTO customFieldDTO1 = new BugCustomFieldDTO();
        customFieldDTO1.setId("custom-field-1");
        customFieldDTO1.setName("开发团队");
        customFieldDTO1.setValue("10068");
        customFieldDTO1.setType("SELECT");
        BugCustomFieldDTO customFieldDTO2 = new BugCustomFieldDTO();
        customFieldDTO2.setId("custom-field-2");
        customFieldDTO2.setName("同名验证");
        customFieldDTO2.setValue("10062");
        customFieldDTO2.setType("SELECT");
        request.setCustomFields(new ArrayList<>(List.of(customFieldDTO1, customFieldDTO2)));
        if (!isUpdate) {
            // 新增
            request.setLinkFileIds(List.of("default-bug-file-id-1"));
            BugCustomFieldDTO handleUserField = new BugCustomFieldDTO();
            handleUserField.setId("assignee");
            handleUserField.setName("处理人");
            handleUserField.setType("select");
            handleUserField.setValue("5f44bb528d89e300469effed");
            BugCustomFieldDTO statusField = new BugCustomFieldDTO();
            statusField.setId("status");
            statusField.setName("状态");
            statusField.setType("select");
            statusField.setValue("10003");
            request.getCustomFields().addAll(List.of(handleUserField, statusField));
        }
        if (isUpdate) {
            // 更新
            Bug addJiraBug = getAddJiraBug();
            request.setId(addJiraBug.getId());
            request.setLinkFileIds(List.of("default-bug-file-id-3"));

            BugCustomFieldDTO summaryField = new BugCustomFieldDTO();
            summaryField.setId("summary");
            summaryField.setName("摘要");
            summaryField.setType("input");
            summaryField.setValue("这是一段summary内容1!!!!");
            BugCustomFieldDTO descriptionField = new BugCustomFieldDTO();
            descriptionField.setId("description");
            descriptionField.setName("描述");
            descriptionField.setType("richText");
            descriptionField.setValue("这是一段描述内容!!!!");
            BugCustomFieldDTO statusField = new BugCustomFieldDTO();
            statusField.setId("status");
            statusField.setName("状态");
            statusField.setType("select");
            statusField.setValue("31");
            request.getCustomFields().addAll(List.of(summaryField, descriptionField, statusField));
        }
        return request;
    }

    /**
     * 添加Jira插件，供测试使用
     *
     * @throws Exception 异常
     */
    public void addJiraPlugin() throws Exception {
        PluginUpdateRequest request = new PluginUpdateRequest();
        File jiraTestFile = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/metersphere-jira-test.jar")).getPath());
        FileInputStream inputStream = new FileInputStream(jiraTestFile);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(jiraTestFile.getName(), jiraTestFile.getName(), "jar", inputStream);
        request.setName("测试插件-JIRA");
        request.setGlobal(true);
        request.setEnable(true);
        request.setCreateUser(ADMIN.name());
        pluginService.add(request, mockMultipartFile);
    }

    /**
     * 获取添加的Jira缺陷
     *
     * @return 缺陷
     */
    private Bug getAddJiraBug() {
        BugExample example = new BugExample();
        example.createCriteria().andTitleEqualTo("这是一个系统Jira模板创建的缺陷");
        return bugMapper.selectByExample(example).getFirst();
    }

    /**
     * 获取创建Jira缺陷时的本地文件
     *
     * @return 本地附件
     */
    private BugLocalAttachment getAddJiraLocalFile() {
        BugLocalAttachmentExample example = new BugLocalAttachmentExample();
        example.createCriteria().andBugIdEqualTo(getAddJiraBug().getId());
        return bugLocalAttachmentMapper.selectByExample(example).getFirst();
    }

    /**
     * 获取创建Jira缺陷时的关联文件
     *
     * @return 关联文件
     */
    private FileAssociation getAddJiraAssociateFile() {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdEqualTo(getAddJiraBug().getId()).andSourceTypeEqualTo(FileAssociationSourceUtil.SOURCE_TYPE_BUG);
        return fileAssociationMapper.selectByExample(example).getFirst();
    }

    /**
     * 获取File上传
     *
     * @return multipartFile
     */
    private MockMultipartFile getMockFile() {
        return new MockMultipartFile(
                "test-file.xlsx",
                "test-file.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "Hello, World!".getBytes());
    }

    /**
     * 重命名本地文件
     */
    private void renameLocalFile(String bugId) {
        BugLocalAttachmentExample example = new BugLocalAttachmentExample();
        example.createCriteria().andBugIdEqualTo(bugId);
        BugLocalAttachment record = new BugLocalAttachment();
        record.setFileName("test1");
        bugLocalAttachmentMapper.updateByExampleSelective(record, example);
        FileMetadata associatedFile = new FileMetadata();
        associatedFile.setId("default-bug-file-id-1");
        associatedFile.setName("test1");
        fileMetadataMapper.updateByPrimaryKeySelective(associatedFile);
    }

    /**
     * 临时移除API字段
     */
    private void removeApiFieldTmp() {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andIdIn(List.of("custom-field-1", "custom-field-2"));
        CustomField record = new CustomField();
        record.setScopeId("default-project-for-bug-tmp");
        customFieldMapper.updateByExampleSelective(record, example);
    }

    /**
     * 恢复API字段
     */
    private void rollBackApiField() {
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andIdIn(List.of("custom-field-1", "custom-field-2"));
        CustomField record = new CustomField();
        record.setScopeId("default-project-for-bug");
        customFieldMapper.updateByExampleSelective(record, example);
    }

    /**
     * 删除本地文件
     */
    private void deleteLocalFile(String bugId) {
        BugLocalAttachmentExample localExample = new BugLocalAttachmentExample();
        localExample.createCriteria().andBugIdEqualTo(bugId);
        bugLocalAttachmentMapper.deleteByExample(localExample);
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdEqualTo(bugId).andSourceTypeEqualTo(FileAssociationSourceUtil.SOURCE_TYPE_BUG);
        fileAssociationMapper.deleteByExample(example);
    }

    /**
     * 获取默认的 MultiValue 参数
     *
     * @param param 参数
     * @param file  文件
     * @return 文件参数
     */
    protected MultiValueMap<String, Object> getMultiPartParam(Object param, File file) {
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("files", file);
        paramMap.add("request", JSON.toJSONString(param));
        return paramMap;
    }
}