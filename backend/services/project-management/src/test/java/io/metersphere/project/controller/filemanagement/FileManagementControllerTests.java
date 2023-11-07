package io.metersphere.project.controller.filemanagement;

import io.metersphere.project.domain.*;
import io.metersphere.project.dto.filemanagement.request.*;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.FileModuleMapper;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.project.utils.FileManagementBaseUtils;
import io.metersphere.project.utils.FileManagementRequestUtils;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.TempFileUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FileManagementControllerTests extends BaseTest {
    private static Project project;

    private static List<BaseTreeNode> preliminaryTreeNodes = new ArrayList<>();

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
    private CommonProjectService commonProjectService;

    @BeforeEach
    public void initTestData() {
        //文件管理专用项目
        if (project == null) {
            AddProjectRequest initProject = new AddProjectRequest();
            initProject.setOrganizationId("100001");
            initProject.setName("文件管理专用项目");
            initProject.setDescription("建国创建的文件管理专用项目");
            initProject.setEnable(true);
            project = commonProjectService.add(initProject, "admin", "/organization-project/add", OperationLogModule.SETTING_ORGANIZATION_PROJECT);
        }
    }
    @Test
    @Order(1)
    public void emptyDataTest() throws Exception {
        //空数据下，检查模块树
        List<BaseTreeNode> treeNodes = this.getFileModuleTreeNode();
        //检查有没有默认节点
        boolean hasNode = false;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getId(), ModuleConstants.DEFAULT_NODE_ID)) {
                hasNode = true;
            }
            Assertions.assertNotNull(baseTreeNode.getParentId());
        }
        Assertions.assertTrue(hasNode);

        //空数据下，检查文件列表
        FileMetadataTableRequest request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
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

    private List<String> getFileType() throws Exception {
        MvcResult fileTypeResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE_TYPE, project.getId()));
        String returnData = fileTypeResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), String.class);
    }

    @Test
    @Order(2)
    public void addModuleTestSuccess() throws Exception {
        //根目录下创建节点(a1）
        FileModuleCreateRequest request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_MODULE_ADD, request);
        String returnId = mvcResult.getResponse().getContentAsString();
        Assertions.assertNotNull(returnId);
        List<BaseTreeNode> treeNodes = this.getFileModuleTreeNode();
        BaseTreeNode a1Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getName(), request.getName())) {
                a1Node = baseTreeNode;
            }
            Assertions.assertNotNull(baseTreeNode.getParentId());
        }
        Assertions.assertNotNull(a1Node);
        checkLog(a1Node.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD);

        //根目录下创建节点a2和a3，在a1下创建子节点a1-b1
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a2");
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_MODULE_ADD, request);

        request.setName("a3");
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_MODULE_ADD, request);

        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1");
        request.setParentId(a1Node.getId());
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_MODULE_ADD, request);

        treeNodes = this.getFileModuleTreeNode();
        BaseTreeNode a1b1Node = null;
        BaseTreeNode a2Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            Assertions.assertNotNull(baseTreeNode.getParentId());
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode childNode : baseTreeNode.getChildren()) {
                    if (StringUtils.equals(childNode.getName(), "a1-b1")) {
                        a1b1Node = childNode;
                    }
                    Assertions.assertNotNull(childNode.getParentId());
                }
            } else if (StringUtils.equals(baseTreeNode.getName(), "a2")) {
                a2Node = baseTreeNode;
            }
        }
        Assertions.assertNotNull(a2Node);
        Assertions.assertNotNull(a1b1Node);
        checkLog(a2Node.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD);
        checkLog(a1b1Node.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD);

        //a1节点下可以继续添加a1节点
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");
        request.setParentId(a1Node.getId());
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_MODULE_ADD, request);

        //继续创建a1下继续创建a1-a1-b1,
        treeNodes = this.getFileModuleTreeNode();
        BaseTreeNode a1ChildNode = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            Assertions.assertNotNull(baseTreeNode.getParentId());
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode childNode : baseTreeNode.getChildren()) {
                    Assertions.assertNotNull(childNode.getParentId());
                    if (StringUtils.equals(childNode.getName(), "a1")) {
                        a1ChildNode = childNode;
                    }
                }
            }
        }
        Assertions.assertNotNull(a1ChildNode);
        checkLog(a1ChildNode.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD);

        //a1的子节点a1下继续创建节点a1-a1-c1
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-a1-c1");
        request.setParentId(a1ChildNode.getId());
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_MODULE_ADD, request);
        treeNodes = this.getFileModuleTreeNode();
        BaseTreeNode a1a1c1Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            Assertions.assertNotNull(baseTreeNode.getParentId());
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode secondNode : baseTreeNode.getChildren()) {
                    Assertions.assertNotNull(secondNode.getParentId());
                    if (StringUtils.equals(secondNode.getName(), "a1") && CollectionUtils.isNotEmpty(secondNode.getChildren())) {
                        for (BaseTreeNode thirdNode : secondNode.getChildren()) {
                            Assertions.assertNotNull(thirdNode.getParentId());
                            if (StringUtils.equals(thirdNode.getName(), "a1-a1-c1")) {
                                a1a1c1Node = thirdNode;
                            }
                        }
                    }
                }
            }
        }
        Assertions.assertNotNull(a1a1c1Node);
        checkLog(a1a1c1Node.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD);

        //子节点a1-b1下继续创建节点a1-b1-c1
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1-c1");
        request.setParentId(a1b1Node.getId());
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_MODULE_ADD, request);
        treeNodes = this.getFileModuleTreeNode();
        BaseTreeNode a1b1c1Node = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode secondNode : baseTreeNode.getChildren()) {
                    if (StringUtils.equals(secondNode.getName(), "a1-b1") && CollectionUtils.isNotEmpty(secondNode.getChildren())) {
                        for (BaseTreeNode thirdNode : secondNode.getChildren()) {
                            if (StringUtils.equals(thirdNode.getName(), "a1-b1-c1")) {
                                a1b1c1Node = thirdNode;
                            }
                        }
                    }
                }
            }
        }
        Assertions.assertNotNull(a1b1c1Node);
        preliminaryTreeNodes = treeNodes;

        checkLog(a1b1c1Node.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD);
    }

    @Test
    @Order(3)
    public void addModuleTestError() throws Exception {
        this.preliminaryData();

        BaseTreeNode a1Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a1");
        assert a1Node != null;

        //参数校验
        FileModuleCreateRequest request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        this.requestPost(FileManagementRequestUtils.URL_MODULE_ADD, request).andExpect(status().isBadRequest());
        request = new FileModuleCreateRequest();
        request.setName("none");
        this.requestPost(FileManagementRequestUtils.URL_MODULE_ADD, request).andExpect(status().isBadRequest());
        request = new FileModuleCreateRequest();
        this.requestPost(FileManagementRequestUtils.URL_MODULE_ADD, request).andExpect(status().isBadRequest());
        request = new FileModuleCreateRequest();
        request.setParentId(null);
        this.requestPost(FileManagementRequestUtils.URL_MODULE_ADD, request).andExpect(status().isBadRequest());

        //父节点ID不存在的
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("ParentIsUUID");
        request.setParentId(IDGenerator.nextStr());
        this.requestPost(FileManagementRequestUtils.URL_MODULE_ADD, request).andExpect(status().is5xxServerError());

        //添加重复的a1节点
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1");
        this.requestPost(FileManagementRequestUtils.URL_MODULE_ADD, request).andExpect(status().is5xxServerError());

        //a1节点下添加重复的a1-b1节点
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1");
        request.setParentId(a1Node.getId());
        this.requestPost(FileManagementRequestUtils.URL_MODULE_ADD, request).andExpect(status().is5xxServerError());

        //子节点的项目ID和父节点的不匹配
        request = new FileModuleCreateRequest();
        request.setProjectId(IDGenerator.nextStr());
        request.setName("RandomUUID");
        request.setParentId(a1Node.getId());
        this.requestPost(FileManagementRequestUtils.URL_MODULE_ADD, request).andExpect(status().is5xxServerError());

        //项目ID和父节点的不匹配
        request = new FileModuleCreateRequest();
        request.setProjectId("100001100001");
        request.setName("RandomUUID");
        request.setParentId(a1Node.getId());
        this.requestPost(FileManagementRequestUtils.URL_MODULE_ADD, request).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(4)
    public void updateModuleTestSuccess() throws Exception {
        if (CollectionUtils.isEmpty(preliminaryTreeNodes)) {
            this.addModuleTestSuccess();
        }
        //更改名称
        BaseTreeNode a1Node = null;
        for (BaseTreeNode node : preliminaryTreeNodes) {
            if (StringUtils.equals(node.getName(), "a1")) {
                for (BaseTreeNode a1ChildrenNode : node.getChildren()) {
                    if (StringUtils.equals(a1ChildrenNode.getName(), "a1")) {
                        a1Node = a1ChildrenNode;
                    }
                }
            }
        }
        assert a1Node != null;
        FileModuleUpdateRequest updateRequest = new FileModuleUpdateRequest();
        updateRequest.setId(a1Node.getId());
        updateRequest.setName("a1-a1");
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_MODULE_UPDATE, updateRequest);

        preliminaryTreeNodes = this.getFileModuleTreeNode();
        checkLog(a1Node.getId(), OperationLogType.UPDATE, FileManagementRequestUtils.URL_MODULE_UPDATE);
    }

    @Test
    @Order(5)
    public void updateModuleTestError() throws Exception {
        BaseTreeNode a1Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a1-a1");
        assert a1Node != null;
        //反例-参数校验
        FileModuleUpdateRequest updateRequest = new FileModuleUpdateRequest();
        this.requestPost(FileManagementRequestUtils.URL_MODULE_UPDATE, updateRequest).andExpect(status().isBadRequest());

        //id不存在
        updateRequest = new FileModuleUpdateRequest();
        updateRequest.setId(IDGenerator.nextStr());
        updateRequest.setName(IDGenerator.nextStr());
        this.requestPost(FileManagementRequestUtils.URL_MODULE_UPDATE, updateRequest).andExpect(status().is5xxServerError());

        //名称重复   a1-a1改为a1-b1
        updateRequest = new FileModuleUpdateRequest();
        updateRequest.setId(a1Node.getId());
        updateRequest.setName("a1-b1");
        this.requestPost(FileManagementRequestUtils.URL_MODULE_UPDATE, updateRequest).andExpect(status().is5xxServerError());
    }

    //下面开始配合着文件管理，所以序号从10开始。如果有模块要补充的用例可以从6继续插入
    @Test
    @Order(10)
    public void fileUploadTestSuccess() throws Exception {
        this.preliminaryData();

        List<String> uploadedFileTypes = new ArrayList<>();

        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(project.getId());

        //导入正常文件
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/file_upload.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        String returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        checkLog(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD);
        FILE_ID_PATH.put(returnId, filePath);
        picFileId = returnId;
        uploadedFileTypes.add("JPG");

        //检查文件类型获取接口有没有获取到数据
        List<String> fileTypes = this.getFileType();
        Assertions.assertEquals(fileTypes.size(), uploadedFileTypes.size());
        for (String fileType : fileTypes) {
            Assertions.assertTrue(uploadedFileTypes.contains(fileType));
        }

        //在来个jar文件
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test-jar-1.jar")).getPath();
        file = new MockMultipartFile("file", "test-jar-1.jar", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        checkLog(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD);
        FILE_ID_PATH.put(returnId, filePath);
        jarFileId = returnId;
        uploadedFileTypes.add("jar");

        //在来个jar文件(状态开启）
        fileUploadRequest.setEnable(true);
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/test-jar-2.jar")).getPath();
        file = new MockMultipartFile("file", "test-jar-2.jar", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        checkLog(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD);
        FILE_ID_PATH.put(returnId, filePath);
        fileUploadRequest.setEnable(false);

        //小型图片文件，用于测试预览图下载
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/1182937072541700.jpg")).getPath();
        file = new MockMultipartFile("file", "1182937072541700.jpg", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        checkLog(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD);
        FILE_ID_PATH.put(returnId, filePath);

        //检查文件类型获取接口有没有获取到数据
        fileTypes = this.getFileType();
        Assertions.assertEquals(fileTypes.size(), uploadedFileTypes.size());
        for (String fileType : fileTypes) {
            Assertions.assertTrue(uploadedFileTypes.contains(fileType));
        }

        //上传隐藏文件 .yincangwenjian
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/.yincangwenjian")).getPath();
        file = new MockMultipartFile("file", ".yincangwenjian", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        checkLog(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD);
        FILE_ID_PATH.put(returnId, filePath);
        uploadedFileTypes.add("");

        //检查文件类型是不是为空
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(returnId);
        Assertions.assertEquals(fileMetadata.getName(), ".yincangwenjian");
        Assertions.assertEquals(fileMetadata.getType(), StringUtils.EMPTY);

        //文件上传到a1-a1节点
        BaseTreeNode a1a1Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a1-a1");
        fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(project.getId());
        fileUploadRequest.setModuleId(a1a1Node.getId());
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/txtFile.txt")).getPath();
        file = new MockMultipartFile("file", "txtFile.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        checkLog(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD);
        FILE_ID_PATH.put(returnId, filePath);
        uploadedFileTypes.add("txt");

        //检查文件类型获取接口有没有获取到数据
        fileTypes = this.getFileType();
        Assertions.assertEquals(fileTypes.size(), uploadedFileTypes.size());
        for (String fileType : fileTypes) {
            Assertions.assertTrue(uploadedFileTypes.contains(fileType));
        }

        //没后缀的文件 (同时上传到a1-a1节点)
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/noSuffixFile")).getPath();
        file = new MockMultipartFile("file", "noSuffixFile", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        checkLog(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD);
        FILE_ID_PATH.put(returnId, filePath);
        uploadedFileTypes.add(StringUtils.EMPTY);

        //检查文件类型获取接口有没有获取到数据
        fileTypes = this.getFileType();
        Assertions.assertEquals(fileTypes.size(), uploadedFileTypes.size());
        for (String fileType : fileTypes) {
            Assertions.assertTrue(uploadedFileTypes.contains(fileType));
        }
    }

    @Test
    @Order(11)
    public void fileUploadTestError() throws Exception {
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            this.fileUploadTestSuccess();
        }

        //参数校验-项目ID为空
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/error_test.txt")).getPath();

        MockMultipartFile file = new MockMultipartFile("file", "error_test.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        this.requestMultipart(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap).andExpect(status().isBadRequest());

        //参数校验-模块ID为空
        fileUploadRequest.setProjectId(project.getId());
        fileUploadRequest.setModuleId(null);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        this.requestMultipart(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap).andExpect(status().isBadRequest());

        //上传非jar文件但是enable为true
        fileUploadRequest.setProjectId(project.getId());
        fileUploadRequest.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
        fileUploadRequest.setEnable(true);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        this.requestMultipart(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap).andExpect(status().is5xxServerError());
        fileUploadRequest.setEnable(false);
        //模块不存在
        fileUploadRequest.setModuleId(IDGenerator.nextStr());
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        this.requestMultipart(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap).andExpect(status().is5xxServerError());

        //文件名重复
        fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(project.getId());
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/txtFile.txt")).getPath();
        file = new MockMultipartFile("file", "txtFile.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        this.requestMultipart(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap).andExpect(status().is5xxServerError());

        //首次上传不能创建空文件。因为要生成数据。 重新上传可以，因为不会修改文件名和类型
        file = new MockMultipartFile("file", "", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        this.requestMultipart(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap).andExpect(status().is5xxServerError());

    }

    @Test
    @Order(12)
    public void fileReUploadTestSuccess() throws Exception {
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            this.fileUploadTestSuccess();
        }
        for (String key : FILE_ID_PATH.keySet()) {
            reUploadFileId = key;
        }

        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(project.getId());

        //构建参数
        FileReUploadRequest fileReUploadRequest = new FileReUploadRequest();
        fileReUploadRequest.setFileId(reUploadFileId);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/file_re-upload.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_re-upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileReUploadRequest));

        //测试非minio文件不能重新上传
        FileMetadata updateModel = new FileMetadata();
        updateModel.setId(reUploadFileId);
        updateModel.setStorage(StorageType.GIT.name());
        fileMetadataMapper.updateByPrimaryKeySelective(updateModel);
        this.requestMultipart(FileManagementRequestUtils.URL_FILE_RE_UPLOAD, paramMap).andExpect(status().is5xxServerError());
        //测试完了改回去
        updateModel.setStorage(StorageType.MINIO.name());
        fileMetadataMapper.updateByPrimaryKeySelective(updateModel);

        //重新上传并修改文件版本
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_RE_UPLOAD, paramMap);
        String reUploadId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        checkLog(reUploadId, OperationLogType.UPDATE, FileManagementRequestUtils.URL_FILE_RE_UPLOAD);
        FILE_ID_PATH.put(reUploadId, filePath);
        FILE_VERSIONS_ID_MAP.put(reUploadId, reUploadFileId);
    }

    @Test
    @Order(13)
    public void fileReUploadTestError() throws Exception {
        //参数校验
        FileReUploadRequest fileReUploadRequest = new FileReUploadRequest();

        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/error_test.txt")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "error_test.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileReUploadRequest));

        this.requestMultipart(FileManagementRequestUtils.URL_FILE_RE_UPLOAD, paramMap).andExpect(status().isBadRequest());

        //旧文件不存在
        fileReUploadRequest.setFileId(IDGenerator.nextStr());
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileReUploadRequest));
        this.requestMultipart(FileManagementRequestUtils.URL_FILE_RE_UPLOAD, paramMap).andExpect(status().is5xxServerError());
    }

    @Test
    @Order(15)
    public void fileDownloadTestSuccess() throws Exception {
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            this.fileReUploadTestSuccess();
        }
        for (String fileMetadataId : FILE_ID_PATH.keySet()) {
            MvcResult mvcResult = this.downloadFile(String.format(FileManagementRequestUtils.URL_FILE_DOWNLOAD, fileMetadataId));
            byte[] fileBytes = mvcResult.getResponse().getContentAsByteArray();

            //通过MD5判断是否是同一个文件
            String fileMD5 = FileManagementBaseUtils.getFileMD5(new File(FILE_ID_PATH.get(fileMetadataId)));
            String downloadMD5 = FileManagementBaseUtils.getFileMD5(fileBytes);
            Assertions.assertEquals(fileMD5, downloadMD5);
        }
        //测试权限
        this.requestGetPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DOWNLOAD, String.format(FileManagementRequestUtils.URL_FILE_DOWNLOAD, picFileId));
    }

    @Test
    @Order(16)
    public void fileDownloadTestError() throws Exception {
        //下载不存在的文件
        mockMvc.perform(getRequestBuilder(FileManagementRequestUtils.URL_FILE_DOWNLOAD, IDGenerator.nextNum()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Order(14)
    public void filePageTestSuccess() throws Exception {
        this.preliminaryData();
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            //做一个重复上传过的文件的测试
            this.fileReUploadTestSuccess();
        }
        FileMetadataTableRequest request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
        }};
        this.filePageRequestAndCheck(request);

        //查找默认模块
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setModuleIds(new ArrayList<>() {{
                this.add(ModuleConstants.DEFAULT_NODE_ID);
            }});
        }};
        this.filePageRequestAndCheck(request);

        //查找有数据的a1-a1模块
        BaseTreeNode a1a1Node = FileManagementBaseUtils.getNodeByName(this.getFileModuleTreeNode(), "a1-a1");
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setModuleIds(new ArrayList<>() {{
                this.add(a1a1Node.getId());
            }});
        }};
        this.filePageRequestAndCheck(request);
        //查找没有数据的a1-b1模块

        BaseTreeNode a1b2Node = FileManagementBaseUtils.getNodeByName(this.getFileModuleTreeNode(), "a1-b1");
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setModuleIds(new ArrayList<>() {{
                this.add(a1b2Node.getId());
            }});
        }};
        this.filePageRequestAndCheck(request);

        //查找不存在的模块
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setModuleIds(new ArrayList<>() {{
                this.add(IDGenerator.nextStr());
            }});
        }};
        this.filePageRequestAndCheck(request);

        //使用已存在的文件类型过滤 区分大小写
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setFileType("JPG");
        }};
        this.filePageRequestAndCheck(request);

        //使用已存在的文件类型过滤 不区分大小写
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setFileType("JpG");
        }};
        this.filePageRequestAndCheck(request);

        //使用不存在的文件类型过滤
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setFileType("fire");
        }};
        this.filePageRequestAndCheck(request);

        //combine掺杂了奇奇怪怪的参数
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setFileType("JpG");
        }};
        request.setCombine(new HashMap<>() {{
            this.put(IDGenerator.nextStr(), IDGenerator.nextStr());
        }});
        this.filePageRequestAndCheck(request);

        //查找我的文件
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setFileType("JpG");
        }};
        request.setCombine(new HashMap<>() {{
            this.put("createUser", "admin");
        }});
        this.filePageRequestAndCheck(request);

        //查找任何一个人创建的文件
        request.setCombine(new HashMap<>() {{
            this.put("createUser", IDGenerator.nextNum());
        }});
        this.filePageRequestAndCheck(request, 0);
    }

    @Test
    @Order(17)
    public void fileBatchDownload() throws Exception {
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            this.fileReUploadTestSuccess();
        }

        //通过ID下载文件
        FileBatchProcessRequest batchProcessDTO = new FileBatchProcessRequest();
        batchProcessDTO.setSelectAll(false);
        batchProcessDTO.setProjectId(project.getId());
        batchProcessDTO.setSelectIds(new ArrayList<>(FILE_ID_PATH.keySet()));
        MvcResult mvcResult = this.batchDownloadFile(FileManagementRequestUtils.URL_FILE_BATCH_DOWNLOAD, batchProcessDTO);
        byte[] fileBytes = mvcResult.getResponse().getContentAsByteArray();
        Assertions.assertTrue(fileBytes.length > 0);

        //下载全部文件
        batchProcessDTO = new FileBatchProcessRequest();
        batchProcessDTO.setSelectAll(true);
        batchProcessDTO.setProjectId(project.getId());
        mvcResult = this.batchDownloadFile(FileManagementRequestUtils.URL_FILE_BATCH_DOWNLOAD, batchProcessDTO);
        fileBytes = mvcResult.getResponse().getContentAsByteArray();
        Assertions.assertTrue(fileBytes.length > 0);

        //全部文件大小超过默认配置(600M)的限制  事先存储20个大小为50M的数据，过后删除
        for (int i = 0; i < 20; i++) {
            String id = "test_" + i;
            String operator = "admin";

            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setName(id);
            fileMetadata.setId(id);
            fileMetadata.setType(org.apache.commons.lang3.StringUtils.EMPTY);
            fileMetadata.setStorage(StorageType.MINIO.name());
            fileMetadata.setProjectId(project.getId());
            fileMetadata.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
            long operationTime = System.currentTimeMillis();
            fileMetadata.setCreateTime(operationTime);
            fileMetadata.setCreateUser(operator);
            fileMetadata.setUpdateTime(operationTime);
            fileMetadata.setUpdateUser(operator);
            fileMetadata.setSize(52428800L);
            fileMetadata.setLatest(true);
            fileMetadata.setRefId(fileMetadata.getId());
            fileMetadata.setEnable(false);
            fileMetadataMapper.insert(fileMetadata);
        }

        mockMvc.perform(getPostRequestBuilder(FileManagementRequestUtils.URL_FILE_BATCH_DOWNLOAD, batchProcessDTO))
                .andExpect(status().is5xxServerError());

        for (int i = 0; i < 20; i++) {
            String id = "test_" + i;
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andIdEqualTo(id);
            fileMetadataMapper.deleteByExample(example);
        }
        //下载空文件
        batchProcessDTO = new FileBatchProcessRequest();
        batchProcessDTO.setSelectAll(false);
        batchProcessDTO.setProjectId(project.getId());
        batchProcessDTO.setSelectIds(new ArrayList<>() {{
            this.add(IDGenerator.nextStr());
        }});

        mockMvc.perform(getPostRequestBuilder(FileManagementRequestUtils.URL_FILE_BATCH_DOWNLOAD, batchProcessDTO))
                .andExpect(status().is5xxServerError());


        //权限判断
        batchProcessDTO = new FileBatchProcessRequest();
        batchProcessDTO.setSelectAll(false);
        batchProcessDTO.setProjectId(DEFAULT_PROJECT_ID);
        batchProcessDTO.setSelectIds(new ArrayList<>(FILE_ID_PATH.keySet()));
        this.requestPostPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DOWNLOAD, FileManagementRequestUtils.URL_FILE_BATCH_DOWNLOAD, batchProcessDTO);
    }

    @Test
    @Order(18)
    public void filePreviewImgDownload() throws Exception {
        this.preliminaryData();
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            //做一个重复上传过的文件的测试
            this.fileReUploadTestSuccess();
        }
        FileMetadataTableRequest request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(50);
            this.setProjectId(project.getId());
        }};
        //获取第一页的所有文件（一页50）
        MvcResult fileMvcResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_PAGE, request);
        Pager<List<FileInformationResponse>> pageResult = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(fileMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        List<FileInformationResponse> fileList = JSON.parseArray(JSON.toJSONString(pageResult.getList()), FileInformationResponse.class);
        for (FileInformationResponse fileDTO : fileList) {
            MvcResult originalResult = this.downloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_ORIGINAL, "admin", fileDTO.getId()));
            Assertions.assertTrue(originalResult.getResponse().getContentAsByteArray().length > 0);
            MvcResult compressedResult = this.downloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_COMPRESSED, "admin", fileDTO.getId()));
            byte[] fileBytes = compressedResult.getResponse().getContentAsByteArray();
            if (TempFileUtils.isImage(fileDTO.getFileType())) {
                if (StringUtils.equals(reUploadFileId, fileDTO.getId())) {
                    //重新上传的文件并不是图片
                    Assertions.assertEquals(fileBytes.length, 0);
                } else {
                    Assertions.assertTrue(fileBytes.length > 0);
                }
            } else {
                Assertions.assertEquals(fileBytes.length, 0);
            }
        }
        //测试重复获取
        for (FileInformationResponse fileDTO : fileList) {
            MvcResult originalResult = this.downloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_ORIGINAL, "admin", fileDTO.getId()));
            Assertions.assertTrue(originalResult.getResponse().getContentAsByteArray().length > 0);
            MvcResult compressedResult = this.downloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_COMPRESSED, "admin", fileDTO.getId()));
            byte[] fileBytes = compressedResult.getResponse().getContentAsByteArray();
            if (TempFileUtils.isImage(fileDTO.getFileType())) {
                if (StringUtils.equals(reUploadFileId, fileDTO.getId())) {
                    //重新上传的文件并不是图片
                    Assertions.assertEquals(fileBytes.length, 0);
                } else {
                    Assertions.assertTrue(fileBytes.length > 0);
                }
            } else {
                Assertions.assertEquals(fileBytes.length, 0);
            }
        }

        //权限测试


        //文件不存在（原图、缩略图两个接口校验）
        mockMvc.perform(getRequestBuilder(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_COMPRESSED, "admin", IDGenerator.nextNum())))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        mockMvc.perform(getRequestBuilder(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_ORIGINAL, "admin", IDGenerator.nextNum())))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        //用户不存在（原图、缩略图两个接口校验）
        mockMvc.perform(getRequestBuilder(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_COMPRESSED, IDGenerator.nextNum(), picFileId)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        mockMvc.perform(getRequestBuilder(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_ORIGINAL, IDGenerator.nextNum(), picFileId)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Order(20)
    public void fileDeleteSuccess() throws Exception {
        if (MapUtils.isEmpty(FILE_VERSIONS_ID_MAP)) {
            this.fileReUploadTestSuccess();
        }
        FileBatchProcessRequest fileBatchProcessRequest;
        //删除指定文件
        for (Map.Entry<String, String> entry : FILE_VERSIONS_ID_MAP.entrySet()) {
            String fileMetadataId = entry.getKey();
            String refId = entry.getValue();

            fileBatchProcessRequest = new FileBatchProcessRequest();
            fileBatchProcessRequest.setProjectId(project.getId());
            fileBatchProcessRequest.setSelectIds(new ArrayList<>() {{
                this.add(fileMetadataId);
            }});
            this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_DELETE, fileBatchProcessRequest);

            this.checkFileIsDeleted(fileMetadataId, refId);
            checkLog(fileMetadataId, OperationLogType.DELETE, FileManagementRequestUtils.URL_FILE_DELETE);
        }
        FILE_VERSIONS_ID_MAP.clear();

        //全部删除
        fileBatchProcessRequest = new FileBatchProcessRequest();
        fileBatchProcessRequest.setSelectAll(true);
        fileBatchProcessRequest.setProjectId(project.getId());
        fileBatchProcessRequest.setExcludeIds(new ArrayList<>() {{
            this.add(IDGenerator.nextStr());
        }});
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_DELETE, fileBatchProcessRequest);
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andProjectIdEqualTo(project.getId());
        Assertions.assertEquals(fileMetadataMapper.countByExample(example), 0);
        //重新上传，用于后续的测试
        this.fileUploadTestSuccess();
    }


    @Test
    @Order(21)
    public void fileUpdateSuccess() throws Exception {
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            this.fileReUploadTestSuccess();
        }
        String updateFileId = "";
        for (String id : FILE_ID_PATH.keySet()) {
            updateFileId = id;
        }

        //全都改
        FileMetadata oldFileMetadata = fileMetadataMapper.selectByPrimaryKey(updateFileId);
        FileUpdateRequest updateRequest = new FileUpdateRequest();
        updateRequest.setId(updateFileId);
        updateRequest.setName("update_" + updateFileId);
        updateRequest.setTags(new ArrayList<>() {{
            this.add("tag1");
        }});
        updateRequest.setDescription("updateDesc_" + updateFileId);
        BaseTreeNode a1a1Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a1-a1");
        updateRequest.setModuleId(a1a1Node.getId());
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest);
        this.checkFileInformation(updateFileId, oldFileMetadata, updateRequest);
        checkLog(updateRequest.getId(), OperationLogType.UPDATE, FileManagementRequestUtils.URL_FILE_UPDATE);

        //只改描述
        oldFileMetadata = fileMetadataMapper.selectByPrimaryKey(updateFileId);
        updateRequest = new FileUpdateRequest();
        updateRequest.setId(updateFileId);
        updateRequest.setDescription("UPDATE DESC AGAIN");
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest);
        this.checkFileInformation(updateFileId, oldFileMetadata, updateRequest);

        //判断更改jar文件的启用禁用
        oldFileMetadata = fileMetadataMapper.selectByPrimaryKey(jarFileId);
        updateRequest = new FileUpdateRequest();
        updateRequest.setId(jarFileId);
        updateRequest.setEnable(true);
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest);
        this.checkFileInformation(jarFileId, oldFileMetadata, updateRequest);

        //取消标签
        oldFileMetadata = fileMetadataMapper.selectByPrimaryKey(updateFileId);
        updateRequest = new FileUpdateRequest();
        updateRequest.setId(updateFileId);
        updateRequest.setTags(new ArrayList<>());
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest);
        this.checkFileInformation(updateFileId, oldFileMetadata, updateRequest);

        //判断什么也不改
        oldFileMetadata = fileMetadataMapper.selectByPrimaryKey(updateFileId);
        updateRequest = new FileUpdateRequest();
        updateRequest.setId(updateFileId);
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest);
        this.checkFileInformation(updateFileId, oldFileMetadata, updateRequest);
    }

    @Test
    @Order(22)
    public void fileUpdateError() throws Exception {

        //参数校验
        FileUpdateRequest updateRequest = new FileUpdateRequest();
        this.requestPost(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest).andExpect(status().isBadRequest());

        //文件不存在
        updateRequest.setId(IDGenerator.nextStr());
        this.requestPost(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest).andExpect(status().is5xxServerError());

        //判断更改非jar文件的启用禁用
        updateRequest = new FileUpdateRequest();
        updateRequest.setId(picFileId);
        updateRequest.setEnable(true);
        this.requestPost(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest).andExpect(status().is5xxServerError());

        //模块不存在
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            this.fileReUploadTestSuccess();
        }
        String updateFileId = "";
        for (String id : FILE_ID_PATH.keySet()) {
            updateFileId = id;
            break;
        }
        updateRequest.setId(updateFileId);
        updateRequest.setModuleId(IDGenerator.nextStr());
        this.requestPost(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest).andExpect(status().is5xxServerError());

    }

    @Test
    @Order(23)
    public void fileInformationTest() throws Exception {
        MvcResult fileTypeResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE, IDGenerator.nextNum()));
        String returnData = fileTypeResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        FileInformationResponse dto = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FileInformationResponse.class);
        Assertions.assertTrue(StringUtils.isEmpty(dto.getId()));
    }

    @Test
    @Order(24)
    public void changeJarEnableTest() throws Exception {
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            this.fileReUploadTestSuccess();
        }
        //测试启用
        this.requestGetWithOk(String.format(FileManagementRequestUtils.URL_CHANGE_JAR_ENABLE, jarFileId, true));
        this.checkFileEnable(jarFileId, true);
        this.checkLog(jarFileId, OperationLogType.UPDATE, "/project/file/jar-file-status");
        //测试禁用
        this.requestGetWithOk(String.format(FileManagementRequestUtils.URL_CHANGE_JAR_ENABLE, jarFileId, false));
        this.checkFileEnable(jarFileId, false);
        //文件不存在
        this.requestGet(String.format(FileManagementRequestUtils.URL_CHANGE_JAR_ENABLE, IDGenerator.nextNum(), true));
        //文件不是jar文件
        this.requestGet(String.format(FileManagementRequestUtils.URL_CHANGE_JAR_ENABLE, picFileId, true));
    }

    @Test
    @Order(80)
    public void moveTest() throws Exception {
        this.preliminaryData();
            /*
                *默认节点
                |
                ·a1 +
                |   |
                |   ·a1-b1   +
                |   |        |
                |   |        ·a1-b1-c1
                |   |
                |   *a1-a1   +（创建的时候是a1，通过修改改为a1-a1）
                |            |
                |            ·a1-a1-c1
                |
                ·a2
                |
                ·a3
            */
        BaseTreeNode a1Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a1");
        BaseTreeNode a2Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a2");
        BaseTreeNode a3Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a3");
        BaseTreeNode a1a1Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a1-a1");
        BaseTreeNode a1b1Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a1-b1");

        //父节点内移动-移动到首位 a1挪到a3后面
        NodeMoveRequest request = new NodeMoveRequest();
        {
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a3Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1Node.getId(), null, false);
        }
        //父节点内移动-移动到末位  在上面的基础上，a1挪到a2上面
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a2Node.getId(), null, false);
        }

        //父节点内移动-移动到中位 a1移动到a2-a3中间
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a1Node.getId(), a3Node.getId(), false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a1Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a2Node.getId(), null, false);
        }

        //跨节点移动-移动到首位   a3移动到a1-b1前面，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1b1Node.getId(), null, false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //跨节点移动-移动到末尾   a3移动到a1-a1后面，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1a1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1a1Node.getId(), a3Node.getId(), null, false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //跨节点移动-移动到中位   a3移动到a1-b1和a1-a1中间，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1b1Node.getId(), a3Node.getId(), a1a1Node.getId(), false);
            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //父节点内移动-a3移动到首位pos小于2，是否触发计算函数 （先手动更改a1的pos为2，然后移动a3到a1前面）
        {
            //更改pos
            FileModule updateModule = new FileModule();
            updateModule.setId(a1Node.getId());
            updateModule.setPos(2L);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1Node.getId(), null, true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //父节点内移动-移动到中位，前后节点pos差不大于2，是否触发计算函数（在上面的 a3-a1-a2的基础上， 先手动更改a1pos为3*64，a2的pos为3*64+2，然后移动a3到a1和a2中间）
        {
            //更改pos
            FileModule updateModule = new FileModule();
            updateModule.setId(a1Node.getId());
            updateModule.setPos(3 * 64L);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);
            updateModule.setId(a2Node.getId());
            updateModule.setPos(3 * 64 + 2L);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a3Node.getId(), a2Node.getId(), true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //跨节点移动-移动到首位pos小于2，是否触发计算函数（先手动更改a1-b1的pos为2，然后移动a3到a1-b1前面，最后再移动回来）
        {
            //更改pos
            FileModule updateModule = new FileModule();
            updateModule.setId(a1b1Node.getId());
            updateModule.setPos(2L);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1b1Node.getId(), null, true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //跨节点移动-移动到中位，前后节点pos差不大于2，是否触发计算函数先手动更改a1-a1的pos为a1-b1+2，然后移动a3到a1-a1前面，最后再移动回来）
        {
            //更改pos
            FileModule updateModule = new FileModule();
            updateModule.setId(a1b1Node.getId());
            updateModule.setPos(3 * 64L);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);
            updateModule.setId(a1a1Node.getId());
            updateModule.setPos(3 * 64 + 2L);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a1a1Node.getId());
            request.setDropPosition(-1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1b1Node.getId(), a3Node.getId(), a1a1Node.getId(), true);

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //移动到没有子节点的节点下  a3移动到a2下
        {
            //开始移动
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(0);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            FileModule a3Module = fileModuleMapper.selectByPrimaryKey(a3Node.getId());
            Assertions.assertEquals(a3Module.getParentId(), a2Node.getId());

            //移动回去
            request = new NodeMoveRequest();
            request.setDragNodeId(a3Node.getId());
            request.setDropNodeId(a2Node.getId());
            request.setDropPosition(1);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        checkLog(a1Node.getId(), OperationLogType.UPDATE, FileManagementRequestUtils.URL_MODULE_MOVE);
        checkLog(a3Node.getId(), OperationLogType.UPDATE, FileManagementRequestUtils.URL_MODULE_MOVE);
    }

    @Test
    @Order(81)
    public void moveFileTest() throws Exception {
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            this.fileUploadTestSuccess();
        }
        BaseTreeNode a1a1c1Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a1-a1-c1");
        //部分文件批量移动
        FileBatchMoveRequest moveRequest = new FileBatchMoveRequest();
        moveRequest.setMoveModuleId(a1a1c1Node.getId());
        moveRequest.setProjectId(project.getId());
        moveRequest.setSelectIds(new ArrayList<>() {{
            this.add(picFileId);
        }});
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_BATCH_UPDATE, moveRequest);
        this.checkFileModule(picFileId, a1a1c1Node.getId());
        checkLog(picFileId, OperationLogType.UPDATE, FileManagementRequestUtils.URL_FILE_BATCH_UPDATE);
        //所有文件批量移动
        moveRequest = new FileBatchMoveRequest();
        moveRequest.setMoveModuleId(a1a1c1Node.getId());
        moveRequest.setProjectId(project.getId());
        moveRequest.setSelectAll(true);
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_BATCH_UPDATE, moveRequest);
        this.checkFileModuleInProject(project.getId(), a1a1c1Node.getId());
        //所有文件再批量移动到相同目录
        moveRequest = new FileBatchMoveRequest();
        moveRequest.setMoveModuleId(a1a1c1Node.getId());
        moveRequest.setProjectId(project.getId());
        moveRequest.setSelectAll(true);
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_BATCH_UPDATE, moveRequest);
        this.checkFileModuleInProject(project.getId(), a1a1c1Node.getId());
        //没有要移动的文件
        moveRequest = new FileBatchMoveRequest();
        moveRequest.setMoveModuleId(a1a1c1Node.getId());
        moveRequest.setProjectId(project.getId());
        moveRequest.setSelectAll(false);
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_BATCH_UPDATE, moveRequest);
        //模块不存在
        moveRequest = new FileBatchMoveRequest();
        moveRequest.setMoveModuleId(IDGenerator.nextStr());
        moveRequest.setProjectId(project.getId());
        moveRequest.setSelectAll(true);
        this.requestPost(FileManagementRequestUtils.URL_FILE_BATCH_UPDATE, moveRequest).andExpect(status().is5xxServerError());
        //项目ID不填写
        moveRequest = new FileBatchMoveRequest();
        moveRequest.setMoveModuleId(IDGenerator.nextStr());
        this.requestPost(FileManagementRequestUtils.URL_FILE_BATCH_UPDATE, moveRequest).andExpect(status().isBadRequest());
        //模块ID不填写
        moveRequest = new FileBatchMoveRequest();
        moveRequest.setProjectId(project.getId());
        this.requestPost(FileManagementRequestUtils.URL_FILE_BATCH_UPDATE, moveRequest).andExpect(status().isBadRequest());
        //权限设置
        moveRequest = new FileBatchMoveRequest();
        moveRequest.setMoveModuleId(IDGenerator.nextStr());
        moveRequest.setProjectId(DEFAULT_PROJECT_ID);
        this.requestPostPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE, FileManagementRequestUtils.URL_FILE_BATCH_UPDATE, moveRequest);
    }

    @Test
    @Order(81)
    public void moveTestError() throws Exception {
        this.preliminaryData();
        BaseTreeNode a1Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a1");
        BaseTreeNode a2Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a2");
        //drag节点为空
        NodeMoveRequest request = new NodeMoveRequest();
        request.setDragNodeId(null);
        request.setDropNodeId(a1Node.getId());
        request.setDropPosition(1);
        this.requestPost(FileManagementRequestUtils.URL_MODULE_MOVE, request).andExpect(status().isBadRequest());
        //drag节点不存在
        request = new NodeMoveRequest();
        request.setDragNodeId(IDGenerator.nextStr());
        request.setDropNodeId(a1Node.getId());
        request.setDropPosition(1);
        this.requestPost(FileManagementRequestUtils.URL_MODULE_MOVE, request).andExpect(status().is5xxServerError());

        //drop节点为空
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(null);
        request.setDropPosition(1);
        this.requestPost(FileManagementRequestUtils.URL_MODULE_MOVE, request).andExpect(status().isBadRequest());

        //drop节点不存在
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(IDGenerator.nextStr());
        request.setDropPosition(1);
        this.requestPost(FileManagementRequestUtils.URL_MODULE_MOVE, request).andExpect(status().is5xxServerError());

        //position为0的时候节点不存在
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(IDGenerator.nextStr());
        request.setDropPosition(0);
        this.requestPost(FileManagementRequestUtils.URL_MODULE_MOVE, request).andExpect(status().is5xxServerError());

        //dragNode和dropNode一样
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(a1Node.getId());
        request.setDropPosition(1);
        this.requestPost(FileManagementRequestUtils.URL_MODULE_MOVE, request).andExpect(status().is5xxServerError());

        //position不是-1 0 1
        request = new NodeMoveRequest();
        request.setDragNodeId(a1Node.getId());
        request.setDropNodeId(a2Node.getId());
        request.setDropPosition(4);
        this.requestPost(FileManagementRequestUtils.URL_MODULE_MOVE, request).andExpect(status().is5xxServerError());
    }
    @Test
    @Order(90)
    public void deleteModuleTestSuccess() throws Exception {
        this.preliminaryData();

        // 删除没有文件的节点a1-b1-c1  检查是否级联删除根节点
        BaseTreeNode a1b1Node = FileManagementBaseUtils.getNodeByName(this.getFileModuleTreeNode(), "a1-b1");
        this.requestGetWithOk(String.format(FileManagementRequestUtils.URL_MODULE_DELETE, a1b1Node.getId()));
        this.checkModuleIsEmpty(a1b1Node.getId());
        checkLog(a1b1Node.getId(), OperationLogType.DELETE, FileManagementRequestUtils.URL_MODULE_DELETE);

        // 删除有文件的节点 a1-a1      检查是否级联删除根节点
        BaseTreeNode a1a1Node = FileManagementBaseUtils.getNodeByName(this.getFileModuleTreeNode(), "a1-a1");
        this.requestGetWithOk(String.format(FileManagementRequestUtils.URL_MODULE_DELETE, a1a1Node.getId()));
        this.checkModuleIsEmpty(a1a1Node.getId());
        checkLog(a1a1Node.getId(), OperationLogType.DELETE, FileManagementRequestUtils.URL_MODULE_DELETE);

        //删除不存在的节点
        this.requestGetWithOk(String.format(FileManagementRequestUtils.URL_MODULE_DELETE, IDGenerator.nextNum()));
        // 测试删除根节点
        this.requestGetWithOk(String.format(FileManagementRequestUtils.URL_MODULE_DELETE, ModuleConstants.DEFAULT_NODE_ID));

        //service层判断：测试删除空集合
        fileModuleService.deleteModule(new ArrayList<>());

        //service层判断：测试删除项目
        fileModuleService.deleteResources(project.getId());
    }

    private void checkModuleIsEmpty(String id) {
        FileModuleExample example = new FileModuleExample();
        example.createCriteria().andParentIdEqualTo(id);
        Assertions.assertEquals(fileModuleMapper.countByExample(example), 0);

        FileMetadataExample metadataExample = new FileMetadataExample();
        example = new FileModuleExample();
        example.createCriteria().andIdEqualTo(id);
        Assertions.assertEquals(fileModuleMapper.countByExample(example), 0);
        metadataExample.createCriteria().andModuleIdEqualTo(id);
        Assertions.assertEquals(fileMetadataMapper.countByExample(metadataExample), 0);
    }

    private void checkFileIsDeleted(String fileId, String refId) {
        FileMetadataExample metadataExample = new FileMetadataExample();
        metadataExample.createCriteria().andIdEqualTo(fileId);
        Assertions.assertEquals(fileMetadataMapper.countByExample(metadataExample), 0);

        metadataExample = new FileMetadataExample();
        metadataExample.createCriteria().andRefIdEqualTo(refId);
        Assertions.assertEquals(fileMetadataMapper.countByExample(metadataExample), 0);
    }

    public MvcResult responseFile(String url, MockMultipartFile file, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.multipart(url)
                        .file(file)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    protected MvcResult downloadFile(String url, Object... uriVariables) throws Exception {
        return mockMvc.perform(getRequestBuilder(url, uriVariables))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk()).andReturn();
    }

    protected MvcResult batchDownloadFile(String url, Object param) throws Exception {
        return mockMvc.perform(getPostRequestBuilder(url, param))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(status().isOk()).andReturn();
    }

    private List<BaseTreeNode> getFileModuleTreeNode() throws Exception {
        MvcResult result = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_MODULE_TREE, project.getId()));
        String returnData = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BaseTreeNode.class);
    }

    private void filePageRequestAndCheck(FileMetadataTableRequest request) throws Exception {
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_PAGE, request);
        Pager<List<FileInformationResponse>> pageResult = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        MvcResult moduleCountMvcResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_MODULE_COUNT, request);
        Map<String, Integer> moduleCountResult = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(moduleCountMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Map.class);
        FileManagementBaseUtils.checkFilePage(pageResult, moduleCountResult, request);
    }

    private void filePageRequestAndCheck(FileMetadataTableRequest request, int fileCount) throws Exception {
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_PAGE, request);
        Pager<List<FileInformationResponse>> pageResult = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                Pager.class);
        Assertions.assertEquals(pageResult.getTotal(), fileCount);
    }

    private void preliminaryData() throws Exception {
        if (CollectionUtils.isEmpty(preliminaryTreeNodes)) {
             /*
                这里需要获取修改过的树的结构。期望的最终结构是这样的（*为测试用例中挂载文件的节点， · 为空节点）：

                *默认节点
                |
                ·a1 +
                |   |
                |   ·a1-b1   +
                |   |        |
                |   |        ·a1-b1-c1
                |   |
                |   *a1-a1   +（创建的时候是a1，通过修改改为a1-a1）
                |   |        |
                |   |        ·a1-a1-c1(用于测试文件移动)
                |
                ·a2
                |
                ·a3
            */
            this.updateModuleTestSuccess();
        }
    }

    private void checkModulePos(String firstNode, String secondNode, String thirdNode, boolean isRecalculate) {
        FileModule firstModule = fileModuleMapper.selectByPrimaryKey(firstNode);
        FileModule secondModule = fileModuleMapper.selectByPrimaryKey(secondNode);
        FileModule thirdModule = null;
        Assertions.assertTrue(firstModule.getPos() < secondModule.getPos());
        if (StringUtils.isNotBlank(thirdNode)) {
            thirdModule = fileModuleMapper.selectByPrimaryKey(thirdNode);
            Assertions.assertTrue(secondModule.getPos() < thirdModule.getPos());
        }
        if (isRecalculate) {
            int limitPos = 64;
            Assertions.assertEquals(0, firstModule.getPos() % limitPos);
            Assertions.assertEquals(0, secondModule.getPos() % limitPos);
            if (thirdModule != null) {
                Assertions.assertEquals(0, thirdModule.getPos() % limitPos);
            }
        }
    }

    private void checkFileEnable(String fileId, boolean enable) {
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdEqualTo(fileId).andEnableEqualTo(enable);
        Assertions.assertTrue(fileMetadataMapper.countByExample(example) > 0);
    }

    private void checkFileModule(String picFileId, String moduleId) {
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdEqualTo(picFileId).andModuleIdEqualTo(moduleId);
        Assertions.assertTrue(fileMetadataMapper.countByExample(example) > 0);
    }

    private void checkFileInformation(String updateFileId, FileMetadata oldFileMetadata, FileUpdateRequest updateRequest) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(updateFileId);
        Assertions.assertNotNull(oldFileMetadata);

        if (StringUtils.isNotEmpty(updateRequest.getDescription())) {
            Assertions.assertTrue(StringUtils.equals(fileMetadata.getDescription(), updateRequest.getDescription()));
        } else {
            Assertions.assertEquals(oldFileMetadata.getDescription(), fileMetadata.getDescription());
        }

        if (StringUtils.isNotEmpty(updateRequest.getName())) {
            Assertions.assertTrue(StringUtils.equals(fileMetadata.getName(), updateRequest.getName()));
        } else {
            Assertions.assertEquals(oldFileMetadata.getName(), fileMetadata.getName());
        }

        if (StringUtils.isNotEmpty(updateRequest.getModuleId())) {
            Assertions.assertTrue(StringUtils.equals(fileMetadata.getModuleId(), updateRequest.getModuleId()));
        } else {
            Assertions.assertEquals(oldFileMetadata.getModuleId(), fileMetadata.getModuleId());
        }

        if (updateRequest.getEnable() != null) {
            Assertions.assertEquals(fileMetadata.getEnable(), updateRequest.getEnable());
        } else {
            Assertions.assertEquals(oldFileMetadata.getEnable(), fileMetadata.getEnable());
        }

        if (!CollectionUtils.isEmpty(updateRequest.getTags())) {
            Assertions.assertTrue(CollectionUtils.isEqualCollection(JSON.parseArray(fileMetadata.getTags(), String.class), updateRequest.getTags()));
        } else {
            List<String> fileTags = fileMetadata.getTags() == null ? new ArrayList<>() : JSON.parseArray(fileMetadata.getTags(), String.class);
            List<String> oldTags = oldFileMetadata.getTags() == null ? new ArrayList<>() : JSON.parseArray(oldFileMetadata.getTags(), String.class);
            Assertions.assertTrue(CollectionUtils.isEqualCollection(fileTags, oldTags));
        }
    }

    private void checkFileModuleInProject(String projectId, String moduleId) {
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andModuleIdNotEqualTo(moduleId).andLatestEqualTo(true);
        Assertions.assertEquals(fileMetadataMapper.countByExample(example), 0);
    }
}
