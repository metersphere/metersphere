package io.metersphere.project.controller.filemanagement;

import io.metersphere.project.domain.FileModule;
import io.metersphere.project.domain.FileModuleRepository;
import io.metersphere.project.dto.filemanagement.request.FileMetadataTableRequest;
import io.metersphere.project.dto.filemanagement.request.FileRepositoryConnectRequest;
import io.metersphere.project.dto.filemanagement.request.FileRepositoryCreateRequest;
import io.metersphere.project.dto.filemanagement.request.FileRepositoryUpdateRequest;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.FileModuleMapper;
import io.metersphere.project.mapper.FileModuleRepositoryMapper;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.project.utils.FileManagementRequestUtils;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FileRepositoryControllerTest extends BaseTest {

    private static ProjectDTO project;

    private static final String GITEE_URL = "https://gitee.com/testformeterspere/gitee-test.git";
    private static final String GITEE_USERNAME = "testformetersphere";
    private static final String GITEE_TOKEN = "4548d369bb595738d726512742e4478f";

    private static List<BaseTreeNode> repositoryTreeNodes = new ArrayList<>();

    private static final Map<String, String> FILE_ID_PATH = new LinkedHashMap<>();

    private static final Map<String, String> FILE_VERSIONS_ID_MAP = new HashMap<>();

    private static String reUploadFileId;

    private static String repositoryId;
    private static String jarFileId;

    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private FileModuleMapper fileModuleMapper;
    @Resource
    private FileModuleRepositoryMapper fileModuleRepositoryMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private CommonProjectService commonProjectService;

    @BeforeEach
    public void initTestData() {
        //文件管理专用项目
        if (project == null) {
            AddProjectRequest initProject = new AddProjectRequest();
            initProject.setOrganizationId("100001");
            initProject.setName("文件管理存储库专用项目");
            initProject.setDescription("建国创建的文件管理存储库专用项目");
            initProject.setEnable(true);
            project = commonProjectService.add(initProject, "admin", "/organization-project/add", OperationLogModule.SETTING_ORGANIZATION_PROJECT);
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
    @Order(2)
    public void repositoryConnectTest() throws Exception {
        FileRepositoryConnectRequest connectRequest = new FileRepositoryConnectRequest();
        connectRequest.setToken(GITEE_TOKEN);
        connectRequest.setUrl(GITEE_URL);
        connectRequest.setUserName(GITEE_USERNAME);
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest);

        //参数测试：没有token
        connectRequest = new FileRepositoryConnectRequest();
        connectRequest.setUrl(GITEE_URL);
        connectRequest.setUserName(GITEE_USERNAME);
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest).andExpect(status().isBadRequest());

        //参数测试：没有url
        connectRequest = new FileRepositoryConnectRequest();
        connectRequest.setToken(GITEE_TOKEN);
        connectRequest.setUserName(GITEE_USERNAME);
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest).andExpect(status().isBadRequest());

        //错误测试：错误的url
        connectRequest = new FileRepositoryConnectRequest();
        connectRequest.setToken(GITEE_TOKEN);
        connectRequest.setUrl("https://gitee.com/testformeterspere-error/error-test.git");
        connectRequest.setUserName(GITEE_USERNAME);
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest).andExpect(status().is5xxServerError());
        //错误测试：错误的token
        connectRequest = new FileRepositoryConnectRequest();
        connectRequest.setToken("error-token");
        connectRequest.setUrl(GITEE_URL);
        connectRequest.setUserName(GITEE_USERNAME);
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest).andExpect(status().is5xxServerError());
        //错误测试：没有userName
        connectRequest = new FileRepositoryConnectRequest();
        connectRequest.setToken(GITEE_TOKEN);
        connectRequest.setUrl(GITEE_URL);
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest).andExpect(status().is5xxServerError());
        //错误测试：错误的userName
        connectRequest = new FileRepositoryConnectRequest();
        connectRequest.setToken(GITEE_TOKEN);
        connectRequest.setUrl(GITEE_URL);
        connectRequest.setUserName("errorUserName");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(3)
    public void moduleAddTest() throws Exception {
        FileRepositoryCreateRequest createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEE);
        createRequest.setUrl(GITEE_URL);
        createRequest.setUserName(GITEE_USERNAME);
        createRequest.setToken(GITEE_TOKEN);
        createRequest.setName("Gitee存储库");
        MvcResult result = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest);
        String returnStr = result.getResponse().getContentAsString();
        ResultHolder rh = JSON.parseObject(returnStr, ResultHolder.class);
        repositoryId = rh.getData().toString();
        this.checkFileRepository(repositoryId, createRequest.getProjectId(), createRequest.getName(), createRequest.getPlatform(), createRequest.getUrl(), createRequest.getToken(), createRequest.getUserName());
        this.checkLog(repositoryId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE);
        //参数测试： 没有url
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEE);
        createRequest.setUserName(GITEE_USERNAME);
        createRequest.setToken(GITEE_TOKEN);
        createRequest.setName("Gitee存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().isBadRequest());
        //参数测试： 没有token
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEE);
        createRequest.setUrl(GITEE_URL);
        createRequest.setUserName(GITEE_USERNAME);
        createRequest.setName("Gitee存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().isBadRequest());
        //参数测试： 没有projectId
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEE);
        createRequest.setUrl(GITEE_URL);
        createRequest.setUserName(GITEE_USERNAME);
        createRequest.setToken(GITEE_TOKEN);
        createRequest.setName("Gitee存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().isBadRequest());
        //参数测试： 没有platform
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setUrl(GITEE_URL);
        createRequest.setUserName(GITEE_USERNAME);
        createRequest.setToken(GITEE_TOKEN);
        createRequest.setName("Gitee存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().isBadRequest());

        //报错测试： 名称重复
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEE);
        createRequest.setUrl(GITEE_URL);
        createRequest.setUserName(GITEE_USERNAME);
        createRequest.setToken(GITEE_TOKEN);
        createRequest.setName("Gitee存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().is5xxServerError());
        //报错测试： platform不合法
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(IDGenerator.nextStr());
        createRequest.setUrl(GITEE_URL);
        createRequest.setUserName(GITEE_USERNAME);
        createRequest.setToken(GITEE_TOKEN);
        createRequest.setName("Gitee存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().is5xxServerError());
        //报错测试： 上述的gitee仓库，不填写用户名
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEE);
        createRequest.setUrl(GITEE_URL);
        createRequest.setToken(GITEE_TOKEN);
        createRequest.setName("Gitee存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().is5xxServerError());

        //测试整体过程中没有修改数据成功
        this.checkFileRepository(repositoryId, createRequest.getProjectId(), createRequest.getName(), createRequest.getPlatform(), createRequest.getUrl(), createRequest.getToken(), createRequest.getUserName());
    }

    @Test
    @Order(4)
    public void moduleUpdateTest() throws Exception {
        if (StringUtils.isEmpty(repositoryId)) {
            this.moduleAddTest();
        }

        //修改文件名
        FileRepositoryUpdateRequest createRequest = new FileRepositoryUpdateRequest();
        createRequest.setId(repositoryId);
        createRequest.setName("Gitee存储库改个名字");
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest);
        this.checkFileRepository(repositoryId, project.getId(), "Gitee存储库改个名字", ModuleConstants.NODE_TYPE_GITEE, GITEE_URL, GITEE_TOKEN, GITEE_USERNAME);
        this.checkLog(repositoryId, OperationLogType.UPDATE, FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE);
        //修改用户名
        FileModuleRepository updateModel = new FileModuleRepository();
        updateModel.setFileModuleId(repositoryId);
        updateModel.setUserName("提前修改用户名");
        fileModuleRepositoryMapper.updateByPrimaryKeySelective(updateModel);

        createRequest = new FileRepositoryUpdateRequest();
        createRequest.setId(repositoryId);
        createRequest.setUserName(GITEE_USERNAME);
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest);
        this.checkFileRepository(repositoryId, project.getId(), "Gitee存储库改个名字", ModuleConstants.NODE_TYPE_GITEE, GITEE_URL, GITEE_TOKEN, GITEE_USERNAME);

        //修改token
        updateModel = new FileModuleRepository();
        updateModel.setFileModuleId(repositoryId);
        updateModel.setToken("newToken");
        fileModuleRepositoryMapper.updateByPrimaryKeySelective(updateModel);
        createRequest = new FileRepositoryUpdateRequest();
        createRequest.setId(repositoryId);
        createRequest.setToken(GITEE_TOKEN);
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest);
        this.checkFileRepository(repositoryId, project.getId(), "Gitee存储库改个名字", ModuleConstants.NODE_TYPE_GITEE, GITEE_URL, GITEE_TOKEN, GITEE_USERNAME);

        //没有修改的
        createRequest = new FileRepositoryUpdateRequest();
        createRequest.setId(repositoryId);
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest);
        this.checkFileRepository(repositoryId, project.getId(), "Gitee存储库改个名字", ModuleConstants.NODE_TYPE_GITEE, GITEE_URL, GITEE_TOKEN, GITEE_USERNAME);

        //全部改回来
        createRequest = new FileRepositoryUpdateRequest();
        createRequest.setId(repositoryId);
        createRequest.setUserName(GITEE_USERNAME);
        createRequest.setToken(GITEE_TOKEN);
        createRequest.setName("Gitee存储库");
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest);
        this.checkFileRepository(repositoryId, project.getId(), "Gitee存储库", ModuleConstants.NODE_TYPE_GITEE, GITEE_URL, GITEE_TOKEN, GITEE_USERNAME);

        //文件id不存在
        createRequest = new FileRepositoryUpdateRequest();
        createRequest.setId(IDGenerator.nextNum().toString());
        createRequest.setName("TEST-NAME");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest).andExpect(status().is5xxServerError());
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

    private void checkFileRepository(String repositoryId, String projectId, String name, String platform, String url, String token, String userName) {
        FileModule module = fileModuleMapper.selectByPrimaryKey(repositoryId);
        FileModuleRepository repository = fileModuleRepositoryMapper.selectByPrimaryKey(repositoryId);
        if (StringUtils.isNotEmpty(projectId)) {
            Assertions.assertEquals(module.getProjectId(), projectId);
        }
        if (StringUtils.isNotEmpty(name)) {
            Assertions.assertEquals(module.getName(), name);
        }
        if (StringUtils.isNotEmpty(platform)) {
            Assertions.assertEquals(repository.getPlatform(), platform);
        }
        if (StringUtils.isNotEmpty(url)) {
            Assertions.assertEquals(repository.getUrl(), url);
        }
        if (StringUtils.isNotEmpty(token)) {
            Assertions.assertEquals(repository.getToken(), token);
        }
        if (StringUtils.isNotEmpty(userName)) {
            Assertions.assertEquals(repository.getUserName(), userName);
        }
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
