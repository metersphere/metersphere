package io.metersphere.project.controller.filemanagement;

import io.metersphere.project.domain.Project;
import io.metersphere.project.dto.filemanagement.request.FileMetadataTableRequest;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.FileModuleMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.project.utils.FileManagementRequestUtils;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FileRepositoryControllerTest extends BaseTest {

    private static final String FILE_TEST_PROJECT_ID = "1507121382013";
    private static Project project;

    private static List<BaseTreeNode> repositoryTreeNodes = new ArrayList<>();

    private static final Map<String, String> FILE_ID_PATH = new LinkedHashMap<>();

    private static final Map<String, String> FILE_VERSIONS_ID_MAP = new HashMap<>();

    private static String reUploadFileId;

    private static String picFileId;
    private static String jarFileId;

    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private FileModuleMapper fileModuleMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private ProjectMapper projectMapper;

    @BeforeEach
    public void initTestData() {
        //文件管理专用项目
        if (project == null) {
            project = projectMapper.selectByPrimaryKey(FILE_TEST_PROJECT_ID);
        }
        if (project == null) {
            Project initProject = new Project();
            initProject.setId(FILE_TEST_PROJECT_ID);
            initProject.setNum(null);
            initProject.setOrganizationId("100001");
            initProject.setName("文件管理专用项目");
            initProject.setDescription("建国创建的文件管理专用项目");
            initProject.setCreateUser("admin");
            initProject.setUpdateUser("admin");
            initProject.setCreateTime(System.currentTimeMillis());
            initProject.setUpdateTime(System.currentTimeMillis());
            initProject.setEnable(true);
            initProject.setModuleSetting("[\"apiTest\",\"uiTest\"]");
            projectMapper.insertSelective(initProject);
            project = projectMapper.selectByPrimaryKey(initProject.getId());
        }
    }

    @Test
    @Order(1)
    public void emptyDataTest() throws Exception {
        //空数据下，检查模块树
        List<BaseTreeNode> treeNodes = this.getFileModuleTreeNode();
        //不能有默认节点
        boolean hasNode = false;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getId(), ModuleConstants.DEFAULT_NODE_ID)) {
                hasNode = true;
            }
            Assertions.assertNotNull(baseTreeNode.getParentId());
        }
        Assertions.assertTrue(!hasNode);

        //空数据下，检查文件列表
        FileMetadataTableRequest request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setCombine(new HashMap<>() {{
                this.put("storage", StorageType.GIT.name());
            }});
        }};
        MvcResult pageResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_PAGE, request);
        String returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Pager<List<FileInformationResponse>> result = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(result.getCurrent(), request.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(result.getList())).size() <= request.getPageSize());

        //此时该接口数量应该为空
        List<String> fileTypes = this.getFileType();
        Assertions.assertTrue(fileTypes.isEmpty());
    }

    @Test
    @Order(10)
    public void repositoryListTest() throws Exception {
        this.getFileModuleTreeNode();
        //权限校验
        this.requestGetPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ, String.format(FileManagementRequestUtils.URL_FILE_REPOSITORY_LIST, DEFAULT_PROJECT_ID));
    }

    @Test
    @Order(20)
    public void repositoryFileTypeTest() throws Exception {
        this.getFileType();
        //权限校验
        this.requestGetPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ, String.format(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_TYPE, DEFAULT_PROJECT_ID));
    }

    private List<BaseTreeNode> getFileModuleTreeNode() throws Exception {
        MvcResult result = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE_REPOSITORY_LIST, project.getId()));
        String returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BaseTreeNode.class);
    }

    private List<String> getFileType() throws Exception {
        MvcResult fileTypeResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_TYPE, project.getId()));
        String returnData = fileTypeResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), String.class);
    }
}
