package io.metersphere.bug.controller;

import io.metersphere.bug.dto.BugDTO;
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

    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_bug.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testBugPageSuccess() throws Exception {
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
    public void testBugPageEmptySuccess() throws Exception {
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
    }

    @Test
    @Order(2)
    public void testBugPageError() throws Exception {
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
    public void testAddBugSuccess() throws Exception {
        BugEditRequest request = buildRequest(false);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipartWithOkAndReturn(BUG_ADD, paramMap);
    }

    @Test
    @Order(4)
    public void testAddBugError() throws Exception {
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
    public void testUpdateBugSuccess() throws Exception {
        BugEditRequest request = buildRequest(true);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipartWithOkAndReturn(BUG_UPDATE, paramMap);
    }

    @Test
    @Order(6)
    public void testUpdateBugError() throws Exception {
        BugEditRequest request = buildRequest(true);
        request.setId("default-bug-id-not-exist");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipart(BUG_UPDATE, paramMap).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(7)
    public void testUpdateBugWithEmptyField() throws Exception {
        BugEditRequest request = buildRequest(true);
        request.setCustomFieldMap(null);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test.xlsx")).getPath();
        File file = new File(filePath);
        MultiValueMap<String, Object> paramMap = getDefaultMultiPartParam(request, file);
        this.requestMultipartWithOkAndReturn(BUG_UPDATE, paramMap);
    }

    @Test
    @Order(8)
    public void testDeleteBugSuccess() throws Exception {
        this.requestGet(BUG_DELETE + "/default-bug-id", status().isOk());
        // 非Local缺陷
        this.requestGet(BUG_DELETE + "/default-bug-id-tapd", status().isOk());
    }

    @Test
    @Order(9)
    public void testDeleteBugError() throws Exception {
        this.requestGet(BUG_DELETE + "/default-bug-id-not-exist", status().is5xxServerError());
    }

    @Test
    @Order(10)
    public void testGetBugTemplateOption() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(BUG_TEMPLATE_OPTION + "?projectId=default-project-for-bug");
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(sortData, ResultHolder.class);
        List<ProjectTemplateOptionDTO> templateOptionDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), ProjectTemplateOptionDTO.class);
        // 默认模板断言
        Assertions.assertTrue(templateOptionDTOS.stream().anyMatch(ProjectTemplateOptionDTO::getEnableDefault));
    }

    @Test
    @Order(11)
    public void testGetBugTemplateDetailSuccess() throws Exception {
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
        request.setTag(JSON.toJSONString(List.of("TAG", "DEFAULT-TAG")));
        request.setLinkFileIds(List.of("default-bug-file-id-1", "default-bug-file-id-3"));
        Map<String, String> customFieldMap = new HashMap<>();
        customFieldMap.put("custom-field", "oasis");
        if (isUpdate) {
            request.setId("default-bug-id");
            request.setUnLinkFileIds(List.of("default-bug-file-id-1"));
            request.setDeleteLocalFileIds(List.of("default-bug-file-id"));
            request.setLinkFileIds(List.of("default-bug-file-id-2"));
            customFieldMap.put("test_field", JSON.toJSONString(List.of("test")));
        }
        request.setCustomFieldMap(customFieldMap);
        return request;
    }
}
