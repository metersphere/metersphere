package io.metersphere.bug.controller;

import io.metersphere.bug.dto.BugCustomFieldDTO;
import io.metersphere.bug.dto.BugDTO;
import io.metersphere.bug.dto.request.BugBatchRequest;
import io.metersphere.bug.dto.request.BugBatchUpdateRequest;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.dto.request.BugPageRequest;
import io.metersphere.project.dto.ProjectTemplateOptionDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.utils.Pager;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugControllerTests extends BaseTest {

    public static final String BUG_PAGE = "/bug/page";
    public static final String BUG_ADD = "/bug/add";
    public static final String BUG_UPDATE = "/bug/update";
    public static final String BUG_DELETE = "/bug/delete";
    public static final String BUG_TEMPLATE_OPTION = "/bug/template/option";
    public static final String BUG_TEMPLATE_DETAIL = "/bug/template";
    public static final String BUG_BATCH_DELETE = "/bug/batch-delete";
    public static final String BUG_BATCH_UPDATE = "/bug/batch-update";
    public static final String BUG_FOLLOW = "/bug/follow";
    public static final String BUG_UN_FOLLOW = "/bug/unfollow";

    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_bug.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void testBugPageSuccess() throws Exception {
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
        BugDTO bugDTO = JSON.parseArray(JSON.toJSONString(pageData.getList()), BugDTO.class).get(0);
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
        BugDTO maxBugDTO = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), BugDTO.class).get(0);
        Assertions.assertTrue(maxBugDTO.getId().contains("default"));
    }

    @Test
    @Order(1)
    void testBugPageEmptySuccess() throws Exception {
        BugPageRequest bugPageRequest = new BugPageRequest();
        bugPageRequest.setCurrent(1);
        bugPageRequest.setPageSize(10);
        bugPageRequest.setKeyword("default-x");
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
        // cover combine
        bugPageRequest.setFilter(null);
        Map<String, Object> combine = new HashMap<>();
        List<Map<String, Object>> customs = new ArrayList<>();
        Map<String, Object> custom = new HashMap<>();
        custom.put("id", "test_field");
        custom.put("operator", "in");
        custom.put("type", "multipleMember");
        custom.put("value", StringUtils.EMPTY);
        customs.add(custom);
        Map<String, Object> currentUserCustom = new HashMap<>();
        currentUserCustom.put("id", "test_field");
        currentUserCustom.put("operator", "current user");
        currentUserCustom.put("type", "multipleMember");
        currentUserCustom.put("value", "current user");
        customs.add(currentUserCustom);
        combine.put("customs", customs);
        bugPageRequest.setCombine(combine);
        this.requestPostWithOkAndReturn(BUG_PAGE, bugPageRequest);
        custom.put("id", "custom-field");
        custom.put("operator", "like");
        custom.put("type", "textarea");
        custom.put("value", "oasis");
        customs.clear();
        customs.add(custom);
        combine.put("customs", customs);
        bugPageRequest.setCombine(combine);
        this.requestPostWithOkAndReturn(BUG_PAGE, bugPageRequest);
        combine.put("customs", null);
        bugPageRequest.setCombine(combine);
        this.requestPostWithOkAndReturn(BUG_PAGE, bugPageRequest);
        // cover combine current user
        custom.clear();
        custom.put("operator", "current user");
        custom.put("value", "current user");
        combine.put("handleUser", custom);
        currentUserCustom.clear();
        currentUserCustom.put("operator", "in");
        currentUserCustom.put("value", List.of("admin"));
        combine.put("createUser", currentUserCustom);
        bugPageRequest.setCombine(combine);
        this.requestPostWithOkAndReturn(BUG_PAGE, bugPageRequest);
    }

    @Test
    @Order(2)
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
    @Order(3)
    void testAddBugSuccess() throws Exception {
        BugEditRequest request = buildRequest(false);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipartWithOkAndReturn(BUG_ADD, paramMap);
    }

    @Test
    @Order(4)
    void testAddBugError() throws Exception {
        BugEditRequest request = buildRequest(false);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        // 项目ID为空
        request.setProjectId(null);
        MultiValueMap<String, Object> paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipart(BUG_ADD, paramMap).andExpect(status().isBadRequest());
        // 标题为空
        request.setProjectId("default-project-for-bug");
        request.setTitle(null);
        paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipart(BUG_ADD, paramMap).andExpect(status().isBadRequest());
        // 处理人为空
        request.setTitle("default-bug-title");
        request.setHandleUser(null);
        paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipart(BUG_ADD, paramMap).andExpect(status().isBadRequest());
        // 模板为空
        request.setHandleUser("admin");
        request.setTemplateId(null);
        paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipart(BUG_ADD, paramMap).andExpect(status().isBadRequest());
        // 状态为空
        request.setTemplateId("default-bug-template");
        request.setStatus(null);
        paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipart(BUG_ADD, paramMap).andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void testUpdateBugSuccess() throws Exception {
        BugEditRequest request = buildRequest(true);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipartWithOkAndReturn(BUG_UPDATE, paramMap);
        // 第二次更新, no-file
        MultiValueMap<String, Object> noFileParamMap = new LinkedMultiValueMap<>();
        request.setLinkFileIds(null);
        noFileParamMap.add("request", JSON.toJSONString(request));
        this.requestMultipartWithOkAndReturn(BUG_UPDATE, noFileParamMap);
    }

    @Test
    @Order(6)
    void testUpdateBugError() throws Exception {
        BugEditRequest request = buildRequest(true);
        request.setId("default-bug-id-not-exist");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipart(BUG_UPDATE, paramMap).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(7)
    void testUpdateBugWithEmptyField() throws Exception {
        BugEditRequest request = buildRequest(true);
        request.setCustomFieldMap(null);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipartWithOkAndReturn(BUG_UPDATE, paramMap);
    }

    @Test
    @Order(8)
    void testGetBugTemplateOption() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(BUG_TEMPLATE_OPTION + "?projectId=default-project-for-bug");
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(sortData, ResultHolder.class);
        List<ProjectTemplateOptionDTO> templateOptionDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), ProjectTemplateOptionDTO.class);
        // 默认模板断言
        Assertions.assertTrue(templateOptionDTOS.stream().anyMatch(ProjectTemplateOptionDTO::getEnableDefault));
    }

    @Test
    @Order(9)
    void testGetBugTemplateDetailSuccess() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(BUG_TEMPLATE_DETAIL + "/default-bug-template-id" + "?projectId=default-project-for-bug");
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(sortData, ResultHolder.class);
        TemplateDTO templateDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TemplateDTO.class);
        Assertions.assertNotNull(templateDTO);
        Assertions.assertEquals("default-bug-template-id", templateDTO.getId());
        // 获取默认模板
        MvcResult defaultResult = this.requestGetWithOkAndReturn(BUG_TEMPLATE_DETAIL + "/default-bug-template-id-not-exist" + "?projectId=default-project-for-bug");
        String defaultData = defaultResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder defaultResultHolder = JSON.parseObject(defaultData, ResultHolder.class);
        TemplateDTO defaultTemplate = JSON.parseObject(JSON.toJSONString(defaultResultHolder.getData()), TemplateDTO.class);
        Assertions.assertNotNull(defaultTemplate);
        Assertions.assertEquals("default-bug-template-id", defaultTemplate.getId());
    }

    @Test
    @Order(10)
    void testBatchUpdateBugSuccess() throws Exception {
        BugBatchUpdateRequest request = new BugBatchUpdateRequest();
        request.setProjectId("default-project-for-bug");
        // 全选, 编辑所有数据
        request.setSelectAll(true);
        // TAG追加
        request.setTag(JSON.toJSONString(List.of("TAG", "TEST_TAG")));
        request.setAppend(true);
        this.requestPost(BUG_BATCH_UPDATE, request, status().isOk());
        // TAG覆盖
        request.setTag(JSON.toJSONString(List.of("A", "B")));
        request.setAppend(false);
        this.requestPost(BUG_BATCH_UPDATE, request, status().isOk());
        // TAG追加
        request.setTag(JSON.toJSONString(List.of("C", "D")));
        request.setAppend(true);
        this.requestPost(BUG_BATCH_UPDATE, request, status().isOk());
        // 处理人修改
        request.setTag(null);
        request.setHandleUser("default-admin");
        this.requestPost(BUG_BATCH_UPDATE, request, status().isOk());
        // 自定义字段追加
        BugCustomFieldDTO field = new BugCustomFieldDTO();
        field.setId("test_field");
        field.setValue(JSON.toJSONString(List.of("test1")));
        request.setCustomField(field);
        this.requestPost(BUG_BATCH_UPDATE, request, status().isOk());
        // 自定义字段覆盖
        request.setAppend(false);
        this.requestPost(BUG_BATCH_UPDATE, request, status().isOk());
        // 勾选部分
        request.setSelectAll(false);
        request.setIncludeBugIds(List.of("default-bug-id"));
        this.requestPost(BUG_BATCH_UPDATE, request, status().isOk());
    }

    @Test
    @Order(11)
    void testBatchUpdateEmptyBugSuccess() throws Exception {
        BugBatchUpdateRequest request = new BugBatchUpdateRequest();
        request.setProjectId("default-project-for-bug");
        request.setCombine(buildRequestCombine());
        // 全选, 空数据
        request.setSelectAll(true);
        request.setTag(JSON.toJSONString(List.of("TAG", "TEST_TAG")));
        request.setAppend(true);
        this.requestPost(BUG_BATCH_UPDATE, request, status().is5xxServerError());
        request.setSelectAll(false);
        request.setIncludeBugIds(List.of("not-exist-bug-id"));
        this.requestPost(BUG_BATCH_UPDATE, request, status().is5xxServerError());
        request.setSelectAll(false);
        request.setIncludeBugIds(null);
        this.requestPost(BUG_BATCH_UPDATE, request, status().is5xxServerError());
    }

    @Test
    @Order(12)
    void testDeleteBugSuccess() throws Exception {
        this.requestGet(BUG_DELETE + "/default-bug-id", status().isOk());
        // 非Local缺陷
        this.requestGet(BUG_DELETE + "/default-bug-id-tapd1", status().isOk());
    }

    @Test
    @Order(13)
    void testDeleteBugError() throws Exception {
        this.requestGet(BUG_DELETE + "/default-bug-id-not-exist", status().is5xxServerError());
    }

    @Test
    @Order(14)
    void testBatchDeleteEmptyBugSuccess() throws Exception {
        BugBatchRequest request = new BugBatchRequest();
        request.setProjectId("default-project-for-bug");
        request.setCombine(buildRequestCombine());
        // 全选, 空数据
        request.setSelectAll(true);
        this.requestPost(BUG_BATCH_DELETE, request, status().isOk());
        // 勾选部分, 空数据
        request.setSelectAll(false);
        this.requestPost(BUG_BATCH_DELETE, request, status().is5xxServerError());
    }

    @Test
    @Order(15)
    void testFollowBug() throws Exception {
        // 关注的缺陷存在
        this.requestGet(BUG_FOLLOW + "/default-bug-id-single", status().isOk());
        // 关注的缺陷不存在
        this.requestGet(BUG_FOLLOW + "/default-bug-id-not-exist", status().is5xxServerError());
    }

    @Test
    @Order(16)
    void testUnFollowBug() throws Exception {
        // 取消关注的缺陷存在
        this.requestGet(BUG_UN_FOLLOW + "/default-bug-id-single", status().isOk());
        // 取消关注的缺陷不存在
        this.requestGet(BUG_UN_FOLLOW + "/default-bug-id-not-exist", status().is5xxServerError());
    }

    @Test
    @Order(20)
    void testBatchDeleteBugSuccess() throws Exception {
        BugBatchRequest request = new BugBatchRequest();
        request.setProjectId("default-project-for-bug");
        // 全选, 删除所有
        request.setSelectAll(true);
        this.requestPost(BUG_BATCH_DELETE, request, status().isOk());
        // 非Local的缺陷删除
        request.setProjectId("default-project-for-bug-no-local");
        this.requestPost(BUG_BATCH_DELETE, request, status().isOk());
        // 勾选部分
        request.setSelectAll(false);
        request.setIncludeBugIds(List.of("default-bug-id-single"));
        this.requestPost(BUG_BATCH_DELETE, request, status().isOk());
    }

    /**
     * 生成请求过滤参数
     * @return filter param
     */
    private Map<String, List<String>> buildRequestFilter() {
        Map<String, List<String>> filter = new HashMap<>();
        filter.put("custom_multiple_test_field", List.of("default", "default1"));
        return filter;
    }

    /**
     * 生成高级搜索参数
     * @return combine param
     */
    private Map<String, Object> buildRequestCombine() {
        Map<String, Object> combine = new HashMap<>();
        List<Map<String, Object>> customs = new ArrayList<>();
        Map<String, Object> custom = new HashMap<>();
        custom.put("id", "test_field");
        custom.put("operator", "in");
        custom.put("type", "multipleSelect");
        custom.put("value", JSON.toJSONString(List.of("default", "default1")));
        customs.add(custom);
        combine.put("customs", customs);
        return combine;
    }

    /**
     * 生成请求参数
     * @param isUpdate 是否更新操作
     * @return 请求参数
     */
    private BugEditRequest buildRequest(boolean isUpdate) {
        BugEditRequest request = new BugEditRequest();
        request.setProjectId("default-project-for-bug");
        request.setTitle("default-bug-title");
        request.setDescription("default-bug-description");
        request.setHandleUser("admin");
        request.setTemplateId("default-bug-template");
        request.setStatus("prepare");
        request.setLinkFileIds(List.of("default-bug-file-id-1"));
        Map<String, String> customFieldMap = new HashMap<>();
        customFieldMap.put("custom-field", "oasis");
        customFieldMap.put("test_field", JSON.toJSONString(List.of("test")));
        if (isUpdate) {
            request.setId("default-bug-id");
            request.setUnLinkRefIds(List.of("default-bug-file-id-1"));
            request.setDeleteLocalFileIds(List.of("default-bug-file-id"));
            request.setLinkFileIds(List.of("default-bug-file-id-2"));
        }
        request.setCustomFieldMap(customFieldMap);
        return request;
    }
}
