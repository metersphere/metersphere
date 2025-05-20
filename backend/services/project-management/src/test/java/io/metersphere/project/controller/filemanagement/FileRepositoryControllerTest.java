package io.metersphere.project.controller.filemanagement;

import io.metersphere.project.domain.*;
import io.metersphere.project.dto.filemanagement.request.*;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.dto.filemanagement.response.FileRepositoryResponse;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.FileMetadataRepositoryMapper;
import io.metersphere.project.mapper.FileModuleMapper;
import io.metersphere.project.mapper.FileModuleRepositoryMapper;
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
import io.metersphere.system.utils.CheckLogModel;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FileRepositoryControllerTest extends BaseTest {

    /*
    private static ProjectDTO project;

    private static final String GITEA_URL = "https://gitea.com/meterspherecodetest/code-test.git";
    private static final String GITEA_TOKEN = "f5e34c45e998291909e0897a76a1f1ae42095e3f";

    private static final List<String> fileList = new ArrayList<>();

    private static String repositoryId;
    private static String picFileId;

    @Resource
    private FileModuleMapper fileModuleMapper;
    @Resource
    private FileModuleRepositoryMapper fileModuleRepositoryMapper;
    @Resource
    private FileMetadataRepositoryMapper fileMetadataRepositoryMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private CommonProjectService commonProjectService;

    private static List<CheckLogModel> LOG_CHECK_LIST = new ArrayList<>();

    @BeforeEach
    public void initTestData() {
        //文件管理专用项目
        if (project == null) {
            AddProjectRequest initProject = new AddProjectRequest();
            initProject.setOrganizationId("100001");
            initProject.setName("文件管理存储库专用项目");
            initProject.setDescription("建国创建的文件管理存储库专用项目");
            initProject.setEnable(true);
            initProject.setUserIds(List.of("admin"));
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
        connectRequest.setToken(GITEA_TOKEN);
        connectRequest.setUrl(GITEA_URL);
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest);

        //参数测试：没有token
        connectRequest = new FileRepositoryConnectRequest();
        connectRequest.setUrl(GITEA_URL);
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest).andExpect(status().isBadRequest());

        //参数测试：没有url
        connectRequest = new FileRepositoryConnectRequest();
        connectRequest.setToken(GITEA_TOKEN);
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest).andExpect(status().isBadRequest());

        //错误测试：错误的url
        connectRequest = new FileRepositoryConnectRequest();
        connectRequest.setToken(GITEA_TOKEN);
        connectRequest.setUrl("https://gitea.com/" + IDGenerator.nextStr() + "/error-test.git");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest).andExpect(status().is5xxServerError());
        //错误测试：错误的token
        connectRequest = new FileRepositoryConnectRequest();
        connectRequest.setToken("error-token");
        connectRequest.setUrl(GITEA_URL);
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CONNECT, connectRequest).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(3)
    public void moduleAddTest() throws Exception {
        FileRepositoryCreateRequest createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEA);
        createRequest.setToken(GITEA_TOKEN);
        createRequest.setUrl(GITEA_URL);
        //先测试名称长度过长 和 url过长
        StringBuilder repositoryName = new StringBuilder();
        while (repositoryName.length() < 256) {
            repositoryName.append("t");
        }
        createRequest.setName(repositoryName.toString());
        ResultActions badRequestAction = this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest);
        badRequestAction.andExpect(status().isBadRequest());
        ResultHolder badRequestHolder = JSON.parseObject(badRequestAction.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        Assertions.assertFalse(StringUtils.contains(badRequestHolder.getMessage(), "Exception"));

        StringBuilder repositoryTestUrl = new StringBuilder();
        while (repositoryTestUrl.length() < 256) {
            repositoryTestUrl.append("t");
        }
        createRequest.setName("GITEA存储库");
        createRequest.setUrl(repositoryTestUrl.toString());
        badRequestAction = this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest);
        badRequestHolder = JSON.parseObject(badRequestAction.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        Assertions.assertFalse(StringUtils.contains(badRequestHolder.getMessage(), "Exception"));

        createRequest.setName("GITEA存储库");
        createRequest.setUrl(GITEA_URL);
        MvcResult result = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest);
        String returnStr = result.getResponse().getContentAsString();
        ResultHolder rh = JSON.parseObject(returnStr, ResultHolder.class);
        repositoryId = rh.getData().toString();
        this.checkFileRepository(repositoryId, createRequest.getProjectId(), createRequest.getName(), createRequest.getPlatform(), createRequest.getUrl(), createRequest.getToken(), createRequest.getUserName());
        LOG_CHECK_LIST.add(
                new CheckLogModel(repositoryId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE)
        );

        //测试获取详情
        MvcResult mvcResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE_REPOSITORY_INFO, repositoryId));
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        FileRepositoryResponse response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FileRepositoryResponse.class);
        Assertions.assertEquals(response.getId(), repositoryId);
        Assertions.assertEquals(response.getName(), createRequest.getName());
        Assertions.assertEquals(response.getPlatform(), createRequest.getPlatform());
        Assertions.assertEquals(response.getToken(), GITEA_TOKEN);
        Assertions.assertEquals(response.getUrl(), GITEA_URL);


        //参数测试： 没有url
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEA);
        createRequest.setToken(GITEA_TOKEN);
        createRequest.setName("GITEA存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().isBadRequest());
        //参数测试： 没有token
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEA);
        createRequest.setUrl(GITEA_URL);
        createRequest.setName("GITEA存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().isBadRequest());
        //参数测试： 没有projectId
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEA);
        createRequest.setUrl(GITEA_URL);
        createRequest.setToken(GITEA_TOKEN);
        createRequest.setName("GITEA存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().isBadRequest());
        //参数测试： 没有platform
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setUrl(GITEA_URL);
        createRequest.setToken(GITEA_TOKEN);
        createRequest.setName("GITEA存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().isBadRequest());

        //报错测试： 名称重复
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEA);
        createRequest.setUrl(GITEA_URL);
        createRequest.setToken(GITEA_TOKEN);
        createRequest.setName("GITEA存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().is5xxServerError());
        //报错测试： platform不合法
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(IDGenerator.nextStr());
        createRequest.setUrl(GITEA_URL);
        createRequest.setToken(GITEA_TOKEN);
        createRequest.setName("GITEA存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().is5xxServerError());

        //报错测试： gitee仓库，不填写用户名
        createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEE);
        createRequest.setUrl("gitee/test/url");
        createRequest.setToken("gitee-token");
        createRequest.setName("Gitee无用户名存储库");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest).andExpect(status().is5xxServerError());

        //测试获取没有数据的详情
        this.requestGet(String.format(FileManagementRequestUtils.URL_FILE_REPOSITORY_INFO, IDGenerator.nextStr())).andExpect(status().is5xxServerError());
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
        createRequest.setName("GITEA存储库改个名字");
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest);
        this.checkFileRepository(repositoryId, project.getId(), "GITEA存储库改个名字", ModuleConstants.NODE_TYPE_GITEA, GITEA_URL, GITEA_TOKEN, null);
        LOG_CHECK_LIST.add(
                new CheckLogModel(repositoryId, OperationLogType.UPDATE, FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE)
        );
        //修改用户名
        FileModuleRepository updateModel = new FileModuleRepository();
        updateModel.setFileModuleId(repositoryId);
        updateModel.setUserName("提前修改用户名");
        fileModuleRepositoryMapper.updateByPrimaryKeySelective(updateModel);

        createRequest = new FileRepositoryUpdateRequest();
        createRequest.setId(repositoryId);
        createRequest.setUserName("GITEA-USERNAME");
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest);
        this.checkFileRepository(repositoryId, project.getId(), "GITEA存储库改个名字", ModuleConstants.NODE_TYPE_GITEA, GITEA_URL, GITEA_TOKEN, "GITEA-USERNAME");

        //修改token
        updateModel = new FileModuleRepository();
        updateModel.setFileModuleId(repositoryId);
        updateModel.setToken("newToken");
        fileModuleRepositoryMapper.updateByPrimaryKeySelective(updateModel);
        createRequest = new FileRepositoryUpdateRequest();
        createRequest.setId(repositoryId);
        createRequest.setToken(GITEA_TOKEN);
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest);
        this.checkFileRepository(repositoryId, project.getId(), "GITEA存储库改个名字", ModuleConstants.NODE_TYPE_GITEA, GITEA_URL, GITEA_TOKEN, null);

        //没有修改的
        createRequest = new FileRepositoryUpdateRequest();
        createRequest.setId(repositoryId);
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest);
        this.checkFileRepository(repositoryId, project.getId(), "GITEA存储库改个名字", ModuleConstants.NODE_TYPE_GITEA, GITEA_URL, GITEA_TOKEN, null);

        //全部改回来
        createRequest = new FileRepositoryUpdateRequest();
        createRequest.setId(repositoryId);
        createRequest.setToken(GITEA_TOKEN);
        createRequest.setName("GITEA存储库");
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest);
        this.checkFileRepository(repositoryId, project.getId(), "GITEA存储库", ModuleConstants.NODE_TYPE_GITEA, GITEA_URL, GITEA_TOKEN, null);

        //文件id不存在
        createRequest = new FileRepositoryUpdateRequest();
        createRequest.setId(IDGenerator.nextNum().toString());
        createRequest.setName("TEST-NAME");
        this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_UPDATE, createRequest).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(5)
    public void moduleDeleteTest() throws Exception {
        if (StringUtils.isEmpty(repositoryId)) {
            this.moduleAddTest();
        }

        this.requestGetWithOk(String.format(FileManagementRequestUtils.URL_MODULE_DELETE, repositoryId));
        this.checkRepositoryDeleted(repositoryId);
        LOG_CHECK_LIST.add(
                new CheckLogModel(repositoryId, OperationLogType.DELETE, FileManagementRequestUtils.URL_MODULE_DELETE)
        );

        //重新添加
        FileRepositoryCreateRequest createRequest = new FileRepositoryCreateRequest();
        createRequest.setProjectId(project.getId());
        createRequest.setPlatform(ModuleConstants.NODE_TYPE_GITEA);
        createRequest.setUrl(GITEA_URL);
        createRequest.setToken(GITEA_TOKEN);
        createRequest.setName("GITEA存储库");
        MvcResult result = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE, createRequest);
        String returnStr = result.getResponse().getContentAsString();
        ResultHolder rh = JSON.parseObject(returnStr, ResultHolder.class);
        repositoryId = rh.getData().toString();
        this.checkFileRepository(repositoryId, createRequest.getProjectId(), createRequest.getName(), createRequest.getPlatform(), createRequest.getUrl(), createRequest.getToken(), createRequest.getUserName());
        LOG_CHECK_LIST.add(
                new CheckLogModel(repositoryId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_REPOSITORY_CREATE)
        );

    }

    private void checkRepositoryDeleted(String repositoryId) {
        FileModuleRepositoryExample repositoryExample = new FileModuleRepositoryExample();
        repositoryExample.createCriteria().andFileModuleIdEqualTo(repositoryId);
        Assertions.assertEquals(fileModuleRepositoryMapper.countByExample(repositoryExample), 0);

        FileModuleExample example = new FileModuleExample();
        example.createCriteria().andIdEqualTo(repositoryId);
        Assertions.assertEquals(fileModuleMapper.countByExample(example), 0);
    }

    private void checkRepositoryFileDeleted(String fileId) {
        FileMetadataRepositoryExample repositoryExample = new FileMetadataRepositoryExample();
        repositoryExample.createCriteria().andFileMetadataIdEqualTo(fileId);
        Assertions.assertEquals(fileMetadataRepositoryMapper.countByExample(repositoryExample), 0);

        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdEqualTo(fileId);
        Assertions.assertEquals(fileMetadataMapper.countByExample(example), 0);
    }

    @Test
    @Order(10)
    public void repositoryListTest() throws Exception {
        this.getFileModuleTreeNode();
        //权限校验
        this.requestGetPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ, String.format(FileManagementRequestUtils.URL_FILE_REPOSITORY_LIST, DEFAULT_PROJECT_ID));
    }

    @Test
    @Order(11)
    public void repositoryAddFileTest() throws Exception {
        if (StringUtils.isEmpty(repositoryId)) {
            this.moduleAddTest();
        }
        //测试主分支的文件
        String branch = "main";
        String filePath = "README.en.md";
        RepositoryFileAddRequest request = new RepositoryFileAddRequest();
        request.setBranch(branch);
        request.setFilePath(filePath);
        request.setModuleId(repositoryId);
        MvcResult result = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request);
        String fileId = JSON.parseObject(result.getResponse().getContentAsString(), ResultHolder.class).getData().toString();
        this.checkRepositoryFile(fileId, request);
        LOG_CHECK_LIST.add(
                new CheckLogModel(fileId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD)
        );
        getFileMessage(fileId);
        fileList.add(fileId);

        //测试其他分支的README.en.md
        String otherBranch = "develop";
        request = new RepositoryFileAddRequest();
        request.setBranch(otherBranch);
        request.setFilePath(filePath);
        request.setModuleId(repositoryId);
        result = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request);
        fileId = JSON.parseObject(result.getResponse().getContentAsString(), ResultHolder.class).getData().toString();
        this.checkRepositoryFile(fileId, request);
        getFileMessage(fileId);
        fileList.add(fileId);

        //测试其他分支的多层目录的文件
        String folderFilePath1 = "test-folder/gitea/test.txt";
        request = new RepositoryFileAddRequest();
        request.setBranch(otherBranch);
        request.setFilePath(folderFilePath1);
        request.setModuleId(repositoryId);
        result = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request);
        fileId = JSON.parseObject(result.getResponse().getContentAsString(), ResultHolder.class).getData().toString();
        this.checkRepositoryFile(fileId, request);
        getFileMessage(fileId);
        fileList.add(fileId);
        //测试隐藏文件
        String folderFilePath2 = "test-folder/.keep";
        request = new RepositoryFileAddRequest();
        request.setBranch(otherBranch);
        request.setFilePath(folderFilePath2);
        request.setModuleId(repositoryId);
        result = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request);
        fileId = JSON.parseObject(result.getResponse().getContentAsString(), ResultHolder.class).getData().toString();
        this.checkRepositoryFile(fileId, request);
        getFileMessage(fileId);
        fileList.add(fileId);
        //测试添加jar包并且启用
        request = new RepositoryFileAddRequest();
        request.setBranch(branch);
        request.setFilePath("jar-test/notJar.jar");
        request.setEnable(true);
        request.setModuleId(repositoryId);
        result = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request);
        fileId = JSON.parseObject(result.getResponse().getContentAsString(), ResultHolder.class).getData().toString();
        this.checkRepositoryFile(fileId, request);
        getFileMessage(fileId);
        fileList.add(fileId);
        //获取图片信息
        request = new RepositoryFileAddRequest();
        request.setBranch(otherBranch);
        request.setFilePath("1095388459180046.jpg");
        request.setModuleId(repositoryId);
        result = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request);
        fileId = JSON.parseObject(result.getResponse().getContentAsString(), ResultHolder.class).getData().toString();
        this.checkRepositoryFile(fileId, request);
        getFileMessage(fileId);
        this.picFileId = fileId;
        fileList.add(fileId);
        {
            //重复添加测试
            request = new RepositoryFileAddRequest();
            request.setBranch(otherBranch);
            request.setFilePath(folderFilePath2);
            request.setModuleId(repositoryId);
            this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request).andExpect(status().is5xxServerError());

            //测试添加非jar包并且启用
            request = new RepositoryFileAddRequest();
            request.setBranch(branch);
            request.setFilePath("README.md");
            request.setEnable(true);
            request.setModuleId(repositoryId);
            this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request).andExpect(status().is5xxServerError());
        }

        {
            //测试不存在的文件
            request = new RepositoryFileAddRequest();
            request.setBranch(otherBranch);
            request.setFilePath(IDGenerator.nextStr());
            request.setModuleId(repositoryId);
            this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request).andExpect(status().is5xxServerError());
            //测试不存在的分支
            request = new RepositoryFileAddRequest();
            request.setBranch(IDGenerator.nextStr());
            request.setFilePath(folderFilePath2);
            request.setModuleId(repositoryId);
            this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request).andExpect(status().is5xxServerError());
            //不存在的moduleId
            request = new RepositoryFileAddRequest();
            request.setBranch(otherBranch);
            request.setFilePath(folderFilePath2);
            request.setModuleId(IDGenerator.nextStr());
            this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request).andExpect(status().is5xxServerError());
        }

        {
            //参数测试
            request = new RepositoryFileAddRequest();
            request.setFilePath(folderFilePath2);
            request.setModuleId(repositoryId);
            this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request).andExpect(status().isBadRequest());

            request = new RepositoryFileAddRequest();
            request.setBranch(IDGenerator.nextStr());
            request.setModuleId(repositoryId);
            this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request).andExpect(status().isBadRequest());

            request = new RepositoryFileAddRequest();
            request.setBranch(IDGenerator.nextStr());
            request.setFilePath(folderFilePath2);
            this.requestPost(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_ADD, request).andExpect(status().isBadRequest());
        }

        //检查前台的页面查询
        //空数据下，检查文件列表
        FileMetadataTableRequest tableRequest = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setCombine(new HashMap<>() {{
                this.put("storage", StorageType.GIT.name());
            }});
        }};
        MvcResult pageResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_PAGE, tableRequest);
        String returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Pager<List<FileInformationResponse>> tableResult = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        Assertions.assertEquals(tableResult.getTotal(), fileList.size());

    }

    private void getFileMessage(String fileId) throws Exception {
        MvcResult fileTypeResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE, fileId));
        String returnData = fileTypeResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        FileInformationResponse dto = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FileInformationResponse.class);
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getId()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getName()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getProjectId()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getModuleName()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getModuleId()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getCreateUser()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getUpdateUser()));
        Assertions.assertTrue(dto.getUpdateTime() > 0);
        Assertions.assertTrue(dto.getCreateTime() > 0);
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getStorage()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getRefId()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getFileVersion()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getFilePath()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getBranch()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getCommitId()));
        Assertions.assertTrue(StringUtils.isNotEmpty(dto.getCommitMessage()));
    }

    @Test
    @Order(12)
    public void repositoryGetFileTest() throws Exception {
        if (StringUtils.isEmpty(picFileId)) {
            this.repositoryAddFileTest();
        }
        //下载文件
        MvcResult originalResult = this.downloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_ORIGINAL, "admin", picFileId));
        byte[] fileBytes = originalResult.getResponse().getContentAsByteArray();
        Assertions.assertTrue(fileBytes.length > 0);
        //预览文件
        originalResult = this.downloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_ORIGINAL, "admin", picFileId));
        Assertions.assertTrue(originalResult.getResponse().getContentAsByteArray().length > 0);
        MvcResult compressedResult = this.downloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_COMPRESSED, "admin", picFileId));
        Assertions.assertTrue(compressedResult.getResponse().getContentAsByteArray().length > 0);
    }

    @Test
    @Order(12)
    public void repositoryPullFileTest() throws Exception {
        if (StringUtils.isEmpty(picFileId)) {
            this.repositoryAddFileTest();
        }
        MvcResult mvcResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_PULL, picFileId));
        String fileId = JSON.parseObject(mvcResult.getResponse().getContentAsString(), ResultHolder.class).getData().toString();
        //此时没有更新记录，应当相等
        Assertions.assertEquals(fileId, picFileId);

        //手动更改过去的commit id 达到pull更新的效果
        FileMetadataRepository updateRepository = new FileMetadataRepository();
        updateRepository.setFileMetadataId(picFileId);
        updateRepository.setCommitId(IDGenerator.nextStr());
        fileMetadataRepositoryMapper.updateByPrimaryKeySelective(updateRepository);
        mvcResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_PULL, picFileId));
        fileId = JSON.parseObject(mvcResult.getResponse().getContentAsString(), ResultHolder.class).getData().toString();
        this.checkRepositoryFile(picFileId, fileId);
    }


    private void checkRepositoryFile(String fileId, RepositoryFileAddRequest request) {
        FileMetadataRepository repository = fileMetadataRepositoryMapper.selectByPrimaryKey(fileId);
        Assertions.assertEquals(repository.getBranch(), request.getBranch());
        Assertions.assertNotNull(repository.getCommitId());
        Assertions.assertNotNull(repository.getCommitMessage());
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        Assertions.assertEquals(fileMetadata.getPath(), request.getFilePath());
        Assertions.assertEquals(fileMetadata.getStorage(), StorageType.GIT.name());
    }

    private void checkRepositoryFile(String fileId, String newFileId) {
        FileMetadata oldMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        FileMetadataRepository oldRepository = fileMetadataRepositoryMapper.selectByPrimaryKey(fileId);
        FileMetadata newMetadata = fileMetadataMapper.selectByPrimaryKey(newFileId);
        FileMetadataRepository newRepository = fileMetadataRepositoryMapper.selectByPrimaryKey(newFileId);

        Assertions.assertNotEquals(fileId, newFileId);
        Assertions.assertEquals(oldMetadata.getName(), newMetadata.getName());
        Assertions.assertEquals(oldMetadata.getPath(), newMetadata.getPath());
        Assertions.assertEquals(oldMetadata.getStorage(), newMetadata.getStorage());
        Assertions.assertEquals(oldMetadata.getModuleId(), newMetadata.getModuleId());
        Assertions.assertEquals(oldMetadata.getType(), newMetadata.getType());
        Assertions.assertEquals(oldMetadata.getRefId(), newMetadata.getRefId());

        Assertions.assertEquals(oldRepository.getBranch(), newRepository.getBranch());
        Assertions.assertNotEquals(oldRepository.getFileMetadataId(), newRepository.getFileMetadataId());
    }

    protected MvcResult downloadFile(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(getRequestBuilder(url, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    @Order(20)
    public void repositoryFileTypeTest() throws Exception {
        this.getFileType();
        //权限校验
        this.requestGetPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ, String.format(FileManagementRequestUtils.URL_FILE_REPOSITORY_FILE_TYPE, DEFAULT_PROJECT_ID));
    }

    @Test
    @Order(99)
    public void repositoryFileDeleteTest() throws Exception {
        if (CollectionUtils.isEmpty(fileList)) {
            this.repositoryAddFileTest();
        }
        FileBatchProcessRequest fileBatchProcessRequest = new FileBatchProcessRequest();
        fileBatchProcessRequest.setProjectId(project.getId());
        fileBatchProcessRequest.setSelectIds(fileList);
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_DELETE, fileBatchProcessRequest);
        for (String fileId : fileList) {
            LOG_CHECK_LIST.add(
                    new CheckLogModel(fileId, OperationLogType.DELETE, FileManagementRequestUtils.URL_FILE_DELETE)
            );
            this.checkRepositoryFileDeleted(fileId);
        }
    }

    @Test
    @Order(100)
    public void testLog() throws Exception {
        Thread.sleep(5000);
        for (CheckLogModel checkLogModel : LOG_CHECK_LIST) {
            if (org.apache.commons.lang3.StringUtils.isEmpty(checkLogModel.getUrl())) {
                this.checkLog(checkLogModel.getResourceId(), checkLogModel.getOperationType());
            } else {
                this.checkLog(checkLogModel.getResourceId(), checkLogModel.getOperationType(), checkLogModel.getUrl());
            }
        }
    }

    private void checkFileRepository(String repositoryId, String projectId, String name, String platform, String url, String token, String userName) {
        FileModule module = fileModuleMapper.selectByPrimaryKey(repositoryId);
        FileModuleRepository repository = fileModuleRepositoryMapper.selectByPrimaryKey(repositoryId);
        Assertions.assertEquals(module.getModuleType(), ModuleConstants.NODE_TYPE_GIT);
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

    */
}