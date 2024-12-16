package io.metersphere.project.controller.filemanagement;

import io.metersphere.project.domain.*;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.filemanagement.FileAssociationDTO;
import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.dto.filemanagement.request.*;
import io.metersphere.project.dto.filemanagement.response.FileAssociationResponse;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.dto.filemanagement.response.FileVersionResponse;
import io.metersphere.project.mapper.FileAssociationMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.FileModuleMapper;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.system.service.FileService;
import io.metersphere.project.utils.FileManagementBaseUtils;
import io.metersphere.project.utils.FileManagementRequestUtils;
import io.metersphere.project.utils.FileMetadataUtils;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.TempFileUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.AddProjectRequest;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.CommonProjectService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.CheckLogModel;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FileManagementControllerTests extends BaseTest {
    private static Project project;

    private static BaseTreeNode A1B1C1_CHILD_NODE;

    private static List<BaseTreeNode> preliminaryTreeNodes = new ArrayList<>();

    private static final Map<String, String> FILE_ID_PATH = new LinkedHashMap<>();

    private static final Map<String, String> FILE_VERSIONS_ID_MAP = new HashMap<>();

    private static String reUploadFileId;

    private static String picFileId;
    private static String jarFileId;

    private static String fileAssociationOldFileId;
    private static String fileAssociationNewFileId;
    private static String fileAssociationNewFilesOne;
    private static String fileAssociationNewFilesTwo;
    private static String fileAssociationNewFilesThree;
    private static String fileAssociationNewFilesFour;
    private static Map<String, List<String>> sourceAssociationFileMap = new HashMap<>();

    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private FileModuleMapper fileModuleMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private CommonProjectService commonProjectService;

    private static List<CheckLogModel> LOG_CHECK_LIST = new ArrayList<>();

    @BeforeEach
    public void initTestData() {
        //文件管理专用项目
        if (project == null) {
            AddProjectRequest initProject = new AddProjectRequest();
            initProject.setOrganizationId("100001");
            initProject.setName("文件管理专用项目");
            initProject.setDescription("建国创建的文件管理专用项目");
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
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1Node.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD)
        );
        //测试a1无法获取存储库详情
        this.requestGet(String.format(FileManagementRequestUtils.URL_FILE_REPOSITORY_INFO, a1Node.getId())).andExpect(status().is5xxServerError());

        //根目录下创建节点a2和a3，在a1下创建子节点a1-b1   parentId设置为小写
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a2");
        request.setParentId("none");
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

        LOG_CHECK_LIST.add(
                new CheckLogModel(a2Node.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD)
        );
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1b1Node.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD)
        );

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
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1ChildNode.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD)
        );

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
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1a1c1Node.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD)
        );
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

        //子节点a1-b1下继续创建节点a1-b1-c1
        request = new FileModuleCreateRequest();
        request.setProjectId(project.getId());
        request.setName("a1-b1-c1");
        request.setParentId(a1b1c1Node.getId());
        this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_MODULE_ADD, request);
        treeNodes = this.getFileModuleTreeNode();
        BaseTreeNode a1b1c1ChildNode = null;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode secondNode : baseTreeNode.getChildren()) {
                    if (StringUtils.equals(secondNode.getName(), "a1-b1") && CollectionUtils.isNotEmpty(secondNode.getChildren())) {
                        for (BaseTreeNode thirdNode : secondNode.getChildren()) {
                            if (StringUtils.equals(thirdNode.getName(), "a1-b1-c1") && CollectionUtils.isNotEmpty(thirdNode.getChildren())) {
                                a1b1c1ChildNode = thirdNode.getChildren().getFirst();
                            }
                        }
                    }
                }
            }
        }
        Assertions.assertNotNull(a1b1c1ChildNode);
        A1B1C1_CHILD_NODE = a1b1c1ChildNode;
        preliminaryTreeNodes = treeNodes;

        LOG_CHECK_LIST.add(
                new CheckLogModel(a1b1c1Node.getId(), OperationLogType.ADD, FileManagementRequestUtils.URL_MODULE_ADD)
        );


        /**
         创建210个节点
         */
        String parentId = null;
        for (int i = 0; i < 210; i++) {
            FileModuleCreateRequest perfRequest = new FileModuleCreateRequest();
            perfRequest.setProjectId(project.getId());
            perfRequest.setName("500-test-root-" + i);
            if (StringUtils.isNotEmpty(parentId)) {
                perfRequest.setParentId(parentId);
            }
            if (i < 200) {
                MvcResult result = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_MODULE_ADD, perfRequest);
                ResultHolder holder = JSON.parseObject(result.getResponse().getContentAsString(), ResultHolder.class);
                if (i % 50 == 0) {
                    //到20换下一层级
                    parentId = holder.getData().toString();
                }
            }
        }
        treeNodes = this.getFileModuleTreeNode();
        preliminaryTreeNodes = treeNodes;
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
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1Node.getId(), OperationLogType.UPDATE, FileManagementRequestUtils.URL_MODULE_UPDATE)
        );
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
        LOG_CHECK_LIST.add(
                new CheckLogModel(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD)
        );
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
        LOG_CHECK_LIST.add(
                new CheckLogModel(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD)
        );
        //判断数据库里启用状态是否正确
        FileMetadata jarFileMeta = fileMetadataMapper.selectByPrimaryKey(returnId);
        Assertions.assertEquals(jarFileMeta.getEnable(), fileUploadRequest.isEnable());
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
        LOG_CHECK_LIST.add(
                new CheckLogModel(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD)
        );
        //判断数据库里启用状态是否正确
        jarFileMeta = fileMetadataMapper.selectByPrimaryKey(returnId);
        Assertions.assertEquals(jarFileMeta.getEnable(), fileUploadRequest.isEnable());
        FILE_ID_PATH.put(returnId, filePath);
        fileUploadRequest.setEnable(false);

        //小型图片文件，用于测试预览图下载
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/icon_file-unknow_colorful.svg")).getPath();
        file = new MockMultipartFile("file", "icon_file-unknow_colorful.svg", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        LOG_CHECK_LIST.add(
                new CheckLogModel(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD)
        );
        FILE_ID_PATH.put(returnId, filePath);

        //svg文件，用于测试预览图下载
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/1182937072541700.jpg")).getPath();
        file = new MockMultipartFile("file", "1182937072541700.jpg", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        LOG_CHECK_LIST.add(
                new CheckLogModel(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD)
        );
        FILE_ID_PATH.put(returnId, filePath);
        uploadedFileTypes.add("svg");

        //检查文件类型获取接口有没有获取到数据
        fileTypes = this.getFileType();
        Assertions.assertEquals(fileTypes.size(), uploadedFileTypes.size());
        for (String fileType : fileTypes) {
            Assertions.assertTrue(uploadedFileTypes.contains(fileType));
        }

        //上传隐藏文件: .yincangwenjian
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/.yincangwenjian")).getPath();
        file = new MockMultipartFile("file", ".yincangwenjian", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        LOG_CHECK_LIST.add(
                new CheckLogModel(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD)
        );
        FILE_ID_PATH.put(returnId, filePath);
        uploadedFileTypes.add(FileMetadataUtils.FILE_TYPE_EMPTY);

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
        LOG_CHECK_LIST.add(
                new CheckLogModel(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD)
        );
        FILE_ID_PATH.put(returnId, filePath);
        uploadedFileTypes.add("txt");

        //检查文件类型获取接口有没有获取到数据
        fileTypes = this.getFileType();
        Assertions.assertEquals(fileTypes.size(), uploadedFileTypes.size());
        for (String fileType : fileTypes) {
            Assertions.assertTrue(uploadedFileTypes.contains(fileType));
        }

        /* 没后缀的文件和后缀是.unknown的文件 (同时上传到a1-a1节点) */
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/noSuffixFile")).getPath();
        file = new MockMultipartFile("file", "noSuffixFile", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        LOG_CHECK_LIST.add(
                new CheckLogModel(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD)
        );
        FILE_ID_PATH.put(returnId, filePath);

        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/noSuffixFile.unknown")).getPath();
        file = new MockMultipartFile("file", "noSuffixFile.unknown", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        LOG_CHECK_LIST.add(
                new CheckLogModel(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD)
        );
        FILE_ID_PATH.put(returnId, filePath);

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

        //上传大于50M的文件
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/largeFile.zip")).getPath();
        file = new MockMultipartFile("file", "largeFile.zip", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(project.getId());
        fileUploadRequest.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
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
            if (StringUtils.isEmpty(fileAssociationNewFilesOne)) {
                fileAssociationNewFilesOne = key;
            } else if (StringUtils.isEmpty(fileAssociationNewFilesTwo)) {
                fileAssociationNewFilesTwo = key;
            } else if (StringUtils.isEmpty(fileAssociationNewFilesThree)) {
                fileAssociationNewFilesThree = key;
            } else if (StringUtils.isEmpty(fileAssociationNewFilesFour)) {
                fileAssociationNewFilesFour = key;
            } else {
                reUploadFileId = key;
            }
        }

        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(project.getId());
        fileAssociationOldFileId = reUploadFileId;
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
        LOG_CHECK_LIST.add(
                new CheckLogModel(reUploadId, OperationLogType.UPDATE, FileManagementRequestUtils.URL_FILE_RE_UPLOAD)
        );
        FILE_ID_PATH.put(reUploadId, filePath);
        FILE_VERSIONS_ID_MAP.put(reUploadId, reUploadFileId);
        fileAssociationNewFileId = reUploadId;
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
            MvcResult mvcResult = this.requestGetDownloadFile(String.format(FileManagementRequestUtils.URL_FILE_DOWNLOAD, fileMetadataId), null);
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
//        mockMvc.perform(getRequestBuilder(FileManagementRequestUtils.URL_FILE_DOWNLOAD, IDGenerator.nextNum()))
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().is5xxServerError());
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

        MediaType zipMediaType = MediaType.parseMediaType("application/zip;charset=UTF-8");

        MvcResult mvcResult = this.requestPostDownloadFile(FileManagementRequestUtils.URL_FILE_BATCH_DOWNLOAD, zipMediaType, batchProcessDTO);
        byte[] fileBytes = mvcResult.getResponse().getContentAsByteArray();
        Assertions.assertTrue(fileBytes.length > 0);

        //下载全部文件
        batchProcessDTO = new FileBatchProcessRequest();
        batchProcessDTO.setSelectAll(true);
        batchProcessDTO.setProjectId(project.getId());
        mvcResult = this.requestPostDownloadFile(FileManagementRequestUtils.URL_FILE_BATCH_DOWNLOAD, zipMediaType, batchProcessDTO);
        fileBytes = mvcResult.getResponse().getContentAsByteArray();
        Assertions.assertTrue(fileBytes.length > 0);

        //重新下载全部文件
        mvcResult = this.requestPostDownloadFile(FileManagementRequestUtils.URL_FILE_BATCH_DOWNLOAD, zipMediaType, batchProcessDTO);
        fileBytes = mvcResult.getResponse().getContentAsByteArray();
        Assertions.assertTrue(fileBytes.length > 0);

        //删除存储的临时文件，再重新下载
        FILE_ID_PATH.forEach((k, v) -> TempFileUtils.deleteTmpFile(k));
        mvcResult = this.requestPostDownloadFile(FileManagementRequestUtils.URL_FILE_BATCH_DOWNLOAD, zipMediaType, batchProcessDTO);
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
            MvcResult originalResult = this.requestGetDownloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_ORIGINAL, "admin", fileDTO.getId()), null);
            Assertions.assertTrue(originalResult.getResponse().getContentAsByteArray().length > 0);
            MvcResult compressedResult;
            if (StringUtils.equalsIgnoreCase(fileDTO.getFileType(), "svg")) {
                compressedResult = this.requestGetDownloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_COMPRESSED, "admin", fileDTO.getId()), MediaType.valueOf("image/svg+xml"));
            } else {
                compressedResult = this.requestGetDownloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_COMPRESSED, "admin", fileDTO.getId()), null);
            }

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
        FileInformationResponse testFileDTO = null;
        for (FileInformationResponse fileDTO : fileList) {
            MvcResult originalResult = this.requestGetDownloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_ORIGINAL, "admin", fileDTO.getId()), null);
            Assertions.assertTrue(originalResult.getResponse().getContentAsByteArray().length > 0);

            MvcResult compressedResult;
            if (StringUtils.equalsIgnoreCase(fileDTO.getFileType(), "svg")) {
                compressedResult = this.requestGetDownloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_COMPRESSED, "admin", fileDTO.getId()), MediaType.valueOf("image/svg+xml"));
            } else {
                testFileDTO = fileDTO;
                compressedResult = this.requestGetDownloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_COMPRESSED, "admin", fileDTO.getId()), null);
            }
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

        //临时文件以及Minio中不存在预览图
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(testFileDTO.getId());
        fileRequest.setFolder(DefaultRepositoryDir.getFileManagementPreviewDir(testFileDTO.getProjectId()));
        fileRequest.setStorage(testFileDTO.getStorage());
        fileService.deleteFile(fileRequest);
        TempFileUtils.deleteTmpFile(testFileDTO.getId());

        MvcResult compressedResult = this.requestGetDownloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_COMPRESSED, "admin", testFileDTO.getId()), null);
        byte[] fileBytes = compressedResult.getResponse().getContentAsByteArray();
        Assertions.assertTrue(fileBytes.length > 0);

        //minio里也没有
        fileService.deleteFile(fileRequest);
        fileRequest.setFolder(DefaultRepositoryDir.getFileManagementDir(testFileDTO.getProjectId()));
        fileService.deleteFile(fileRequest);
        TempFileUtils.deleteTmpFile(testFileDTO.getId());

        compressedResult = this.requestGetDownloadFile(String.format(FileManagementRequestUtils.URL_FILE_PREVIEW_COMPRESSED, "admin", testFileDTO.getId()), null);
        fileBytes = compressedResult.getResponse().getContentAsByteArray();
        Assertions.assertEquals(fileBytes.length, 0);

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
        updateRequest.setTags(new LinkedHashSet<>() {{
            this.add("tag1");
        }});
        updateRequest.setDescription("updateDesc_" + updateFileId);
        BaseTreeNode a1a1Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a1-a1");
        updateRequest.setModuleId(a1a1Node.getId());
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest);
        this.checkFileInformation(updateFileId, oldFileMetadata, updateRequest);
        LOG_CHECK_LIST.add(
                new CheckLogModel(updateRequest.getId(), OperationLogType.UPDATE, FileManagementRequestUtils.URL_FILE_UPDATE)
        );
        //检查表格里查询到的有没有tag
        FileUpdateRequest finalUpdateRequest = updateRequest;
        FileMetadataTableRequest request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.initKeyword(finalUpdateRequest.getName());
        }};
        MvcResult pageResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_PAGE, request);
        String returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Pager<List<FileInformationResponse>> result = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        List<FileInformationResponse> fileList = JSON.parseArray(JSON.toJSONString(result.getList()), FileInformationResponse.class);
        for (FileInformationResponse response : fileList) {
            if (StringUtils.equals(response.getId(), updateRequest.getId())) {
                Assertions.assertTrue(response.getTags().contains("tag1"));
            }
        }
        //检查get接口能不能获取到
        MvcResult fileTypeResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE, updateRequest.getId()));
        returnData = fileTypeResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        FileInformationResponse dto = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FileInformationResponse.class);
        Assertions.assertTrue(dto.getTags().contains("tag1"));

        //只改描述
        oldFileMetadata = fileMetadataMapper.selectByPrimaryKey(updateFileId);
        updateRequest = new FileUpdateRequest();
        updateRequest.setId(updateFileId);
        updateRequest.setDescription("UPDATE DESC AGAIN");
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest);
        this.checkFileInformation(updateFileId, oldFileMetadata, updateRequest);

        //取消标签
        oldFileMetadata = fileMetadataMapper.selectByPrimaryKey(updateFileId);
        updateRequest = new FileUpdateRequest();
        updateRequest.setId(updateFileId);
        updateRequest.setTags(new LinkedHashSet<>());
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
        updateRequest.setTags(new LinkedHashSet<>());
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

        if (StringUtils.isAnyEmpty(fileAssociationOldFileId, fileAssociationNewFileId, fileAssociationNewFilesOne, fileAssociationNewFilesTwo, fileAssociationNewFilesThree)) {
            this.fileReUploadTestSuccess();
        }
        fileTypeResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE, fileAssociationNewFilesTwo));
        returnData = fileTypeResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        dto = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FileInformationResponse.class);
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
        Assertions.assertTrue(StringUtils.isEmpty(dto.getBranch()));
        Assertions.assertTrue(StringUtils.isEmpty(dto.getCommitId()));
        Assertions.assertTrue(StringUtils.isEmpty(dto.getCommitMessage()));
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
        LOG_CHECK_LIST.add(
                new CheckLogModel(jarFileId, OperationLogType.UPDATE, "/project/file/jar-file-status")
        );
        //测试禁用
        this.requestGetWithOk(String.format(FileManagementRequestUtils.URL_CHANGE_JAR_ENABLE, jarFileId, false));
        this.checkFileEnable(jarFileId, false);
        //文件不存在
        this.requestGet(String.format(FileManagementRequestUtils.URL_CHANGE_JAR_ENABLE, IDGenerator.nextNum(), true));
        //文件不是jar文件
        this.requestGet(String.format(FileManagementRequestUtils.URL_CHANGE_JAR_ENABLE, picFileId, true));
    }

    @Test
    @Order(29)
    public void fileVersionTest() throws Exception {
        if (StringUtils.isAnyEmpty(fileAssociationOldFileId, fileAssociationNewFileId, fileAssociationNewFilesOne, fileAssociationNewFilesTwo, fileAssociationNewFilesThree)) {
            this.fileReUploadTestSuccess();
        }
        MvcResult result = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE_VERSION, fileAssociationNewFileId));
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        List<FileVersionResponse> fileVersionResponseList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), FileVersionResponse.class);

        Assertions.assertTrue(fileVersionResponseList.size() > 1);
        boolean hasOldVersion = false;
        boolean hasNewVersion = false;

        for (FileVersionResponse fileVersionResponse : fileVersionResponseList) {
            if (StringUtils.equals(fileVersionResponse.getId(), fileAssociationOldFileId)) {
                hasOldVersion = true;
            } else if (StringUtils.equals(fileVersionResponse.getId(), fileAssociationNewFileId)) {
                hasNewVersion = true;
            }
        }
        Assertions.assertTrue(hasOldVersion && hasNewVersion);
        //权限测试
        this.requestGetPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ, String.format(FileManagementRequestUtils.URL_FILE_VERSION, fileAssociationNewFileId));
    }

    /*
      30-80之间是测试文件关联的
     */
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private FileAssociationService fileAssociationService;

    @Test
    @Order(30)
    public void fileAssociationCheckFileVersionTest() throws Exception {
        if (StringUtils.isAnyEmpty(fileAssociationOldFileId, fileAssociationNewFileId, fileAssociationNewFilesOne, fileAssociationNewFilesTwo, fileAssociationNewFilesThree)) {
            this.fileReUploadTestSuccess();
        }
        //1.没有要更新的文件
        List<String> fileIdList = new ArrayList<>() {{
            this.add(fileAssociationNewFileId);
            this.add(fileAssociationNewFilesOne);
        }};
        List<String> newVersionFileIdList = fileAssociationService.checkFilesVersion(fileIdList);
        Assertions.assertTrue(CollectionUtils.isEmpty(newVersionFileIdList));
        //2.有要更新的文件
        fileIdList = new ArrayList<>() {{
            this.add(fileAssociationOldFileId);
            this.add(fileAssociationNewFilesOne);
        }};
        newVersionFileIdList = fileAssociationService.checkFilesVersion(fileIdList);
        Assertions.assertEquals(newVersionFileIdList.size(), 1);
        Assertions.assertTrue(newVersionFileIdList.contains(fileAssociationOldFileId));
        //3.参数为空
        boolean error = false;
        try {
            fileAssociationService.checkFilesVersion(new ArrayList<>());
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);
    }

    /**
     * 文件关联的相关测试。由于要注入sql，拆分多个单元测试方法时无法保证sql的注入性。所以合在一个方法中。
     *
     * @throws Exception
     */
    @Test
    @Order(31)
    @Sql(scripts = {"/dml/init_bug.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void fileAssociationTests() throws Exception {
        //检查文件版本
        fileAssociationCheckFileVersionTest();
        //预备：手动关联缺陷过期文件. bug-id-1和bug-id-2用来后续测试更新， bug-id-3用于本轮测试
        List<String> oldFileIdList = new ArrayList<>();
        oldFileIdList.add(this.addFileAssociation("sty-file-association-bug-id-2", "BUG", fileAssociationOldFileId));
        this.saveSourceAssociationId("sty-file-association-bug-id-2", oldFileIdList);

        oldFileIdList = new ArrayList<>();
        oldFileIdList.add(this.addFileAssociation("sty-file-association-bug-id-4", "BUG", fileAssociationOldFileId));
        this.saveSourceAssociationId("sty-file-association-bug-id-4", oldFileIdList);

        //文件关联展示接口测试
        this.associationFileTableShow();
        //文件关联
        this.associationFile();
        //文件更新
        this.associationUpgrade();
        //文件转存并关联
        this.transferAndAssociation();
        //文件管理页面-查询关联文件
        this.fileAssociationControllerPage();
        //文件管理页面-更新关联文件
        this.fileAssociationControllerUpgrade();
        //文件管理页面-取消关联文件
        this.fileAssociationControllerDelete();
        //文件取消关联
        this.associationDelete();
    }

    @Test
    @Order(40)
    public void fileDeleteSuccess() throws Exception {
        if (MapUtils.isEmpty(FILE_VERSIONS_ID_MAP)) {
            this.fileReUploadTestSuccess();
        }
        //测试中涉及到全部删除。删除文件时FileAssociation不会删除。在删除前先把相关数据全部查询出来，用于全部删除之后的数据检查对比
        FileAssociationExample fileAssociationExample = new FileAssociationExample();
        fileAssociationExample.createCriteria().andFileIdIn(new ArrayList<>() {{
            this.add(fileAssociationNewFilesOne);
            this.add(fileAssociationNewFilesTwo);
            this.add(fileAssociationNewFilesThree);
            this.add(fileAssociationNewFilesFour);
            this.add(fileAssociationNewFileId);
            this.add(fileAssociationOldFileId);
        }});
        List<FileAssociation> beforeDeletedList = fileAssociationMapper.selectByExample(fileAssociationExample);
        beforeDeletedList.forEach(item -> {
            Assertions.assertFalse(item.getDeleted());
            Assertions.assertNull(item.getDeletedFileName());
        });

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
            LOG_CHECK_LIST.add(
                    new CheckLogModel(fileMetadataId, OperationLogType.DELETE, FileManagementRequestUtils.URL_FILE_DELETE)
            );
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

        //检查fileAssociation
        List<FileAssociation> aftreDeletedList = fileAssociationMapper.selectByExample(fileAssociationExample);
        Assertions.assertEquals(beforeDeletedList.size(), aftreDeletedList.size());
        aftreDeletedList.forEach(item -> {
            Assertions.assertTrue(item.getDeleted());
            Assertions.assertNotNull(item.getDeletedFileName());
        });

        //重新上传，用于后续的测试
        this.fileUploadTestSuccess();
    }

    private void associationFileTableShow() throws Exception {
        //从没关联过，则除了oldId都不展示
        FileMetadataTableRequest tableRequest = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(50);
            this.setProjectId(project.getId());
        }};
        MvcResult pageResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_PAGE, tableRequest);
        String returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Pager<List<FileInformationResponse>> tableResult = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        List<String> hiddenIds = new ArrayList<>() {{
            this.add(fileAssociationOldFileId);
        }};
        List<FileInformationResponse> list = JSON.parseArray(JSON.toJSONString(tableResult.getList()), FileInformationResponse.class);
        for (FileInformationResponse fileInformationResponse : list) {
            Assertions.assertFalse(hiddenIds.contains(fileInformationResponse.getId()));
        }
        //关联过oldFileID，则oldFileID和NewId都不展示
        tableRequest = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(50);
            this.setProjectId(project.getId());
            this.setCombine(new HashMap<>() {{
                this.put("hiddenIds", new ArrayList<>() {{
                    this.add(fileAssociationOldFileId);
                }});
            }});
        }};
        pageResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_PAGE, tableRequest);
        returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        tableResult = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        hiddenIds = new ArrayList<>() {{
            this.add(fileAssociationOldFileId);
            this.add(fileAssociationNewFileId);
        }};
        list = JSON.parseArray(JSON.toJSONString(tableResult.getList()), FileInformationResponse.class);
        for (FileInformationResponse fileInformationResponse : list) {
            Assertions.assertFalse(hiddenIds.contains(fileInformationResponse.getId()));
        }
        //关联过newId，则关联的newId和oldId都不展示
        tableRequest = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(50);
            this.setProjectId(project.getId());
            this.setCombine(new HashMap<>() {{
                this.put("hiddenIds", new ArrayList<>() {{
                    this.add(fileAssociationNewFilesOne);
                    this.add(fileAssociationNewFilesTwo);
                }});
            }});
        }};
        pageResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_PAGE, tableRequest);
        returnData = pageResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        tableResult = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        hiddenIds = new ArrayList<>() {{
            this.add(fileAssociationOldFileId);
            this.add(fileAssociationNewFilesOne);
            this.add(fileAssociationNewFilesTwo);
        }};
        list = JSON.parseArray(JSON.toJSONString(tableResult.getList()), FileInformationResponse.class);
        for (FileInformationResponse fileInformationResponse : list) {
            Assertions.assertFalse(hiddenIds.contains(fileInformationResponse.getId()));
        }
    }

    private void fileAssociationControllerDelete() throws Exception {
        //删除bug-id-4的
        FileAssociationDeleteRequest deleteRequest = new FileAssociationDeleteRequest();
        deleteRequest.setAssociationIds(sourceAssociationFileMap.get("sty-file-association-bug-id-4"));
        deleteRequest.setProjectId(project.getId());
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_ASSOCIATION_DELETE, deleteRequest);
        int returnCount = Integer.parseInt(JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString());
        Assertions.assertEquals(returnCount, sourceAssociationFileMap.get("sty-file-association-bug-id-4").size());
        //重复删除
        mvcResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_ASSOCIATION_DELETE, deleteRequest);
        returnCount = Integer.parseInt(JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString());
        Assertions.assertEquals(returnCount, 0);
        //参数不合法
        deleteRequest = new FileAssociationDeleteRequest();
        deleteRequest.setProjectId(project.getId());
        deleteRequest.setAssociationIds(null);
        this.requestPost(FileManagementRequestUtils.URL_FILE_ASSOCIATION_DELETE, deleteRequest).andExpect(status().isBadRequest());
        deleteRequest.setAssociationIds(new ArrayList<>());
        this.requestPost(FileManagementRequestUtils.URL_FILE_ASSOCIATION_DELETE, deleteRequest).andExpect(status().isBadRequest());

        deleteRequest = new FileAssociationDeleteRequest();
        deleteRequest.setAssociationIds(sourceAssociationFileMap.get("sty-file-association-bug-id-4"));
        this.requestPost(FileManagementRequestUtils.URL_FILE_ASSOCIATION_DELETE, deleteRequest).andExpect(status().isBadRequest());


        FileAssociationDeleteRequest permissionRequest = new FileAssociationDeleteRequest();
        permissionRequest.setAssociationIds(sourceAssociationFileMap.get("sty-file-association-bug-id-4"));
        permissionRequest.setProjectId(DEFAULT_PROJECT_ID);
        this.requestPostPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE, FileManagementRequestUtils.URL_FILE_ASSOCIATION_DELETE, permissionRequest);
    }

    private void fileAssociationControllerPage() throws Exception {
        //关联过的
        MvcResult mvcResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE_ASSOCIATION_LIST, fileAssociationNewFilesOne));
        List<FileAssociationResponse> fileAssociationResponseList = JSON.parseArray(JSON.toJSONString(JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()), FileAssociationResponse.class);
        Assertions.assertEquals(fileAssociationResponseList.size(), 2);//这个文件只关联了id-2和id-3
        //没有关联过
        mvcResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE_ASSOCIATION_LIST, fileAssociationNewFilesFour));
        fileAssociationResponseList = JSON.parseArray(JSON.toJSONString(JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()), FileAssociationResponse.class);
        Assertions.assertEquals(fileAssociationResponseList.size(), 0);
        //数据不存在
        this.requestGet(String.format(FileManagementRequestUtils.URL_FILE_ASSOCIATION_LIST, "sty-file-association-bug-id-0")).andExpect(status().is5xxServerError());

        this.requestGetPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ, String.format(FileManagementRequestUtils.URL_FILE_ASSOCIATION_LIST, fileAssociationNewFilesOne));
    }

    private void fileAssociationControllerUpgrade() throws Exception {
        String associationId = sourceAssociationFileMap.get("sty-file-association-bug-id-2").getFirst();
        MvcResult mvcResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE_ASSOCIATION_UPGRADE, project.getId(), associationId));
        String fileId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdEqualTo(fileId).andLatestEqualTo(true);
        Assertions.assertEquals(fileMetadataMapper.countByExample(example), 1);

        //重复更新
        mvcResult = this.requestGetWithOkAndReturn(String.format(FileManagementRequestUtils.URL_FILE_ASSOCIATION_UPGRADE, project.getId(), associationId));
        String newFileId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        Assertions.assertEquals(newFileId, fileId);

        this.requestGetPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE, String.format(FileManagementRequestUtils.URL_FILE_ASSOCIATION_UPGRADE, DEFAULT_PROJECT_ID, associationId));
    }


    private void saveSourceAssociationId(String sourceId, List<String> associationIdList) {
        if (CollectionUtils.isEmpty(associationIdList)) {
            return;
        }
        if (sourceAssociationFileMap.containsKey(sourceId)) {
            sourceAssociationFileMap.get(sourceId).addAll(associationIdList);
            sourceAssociationFileMap.get(sourceId).stream().distinct().collect(Collectors.toList());
        } else {
            sourceAssociationFileMap.put(sourceId, associationIdList);
        }
    }

    public void associationFile() {
        //关联id-1和id-3 （不覆盖）
        List<String> fileIdList = new ArrayList<>() {{
            this.add(fileAssociationNewFilesOne);
            this.add(fileAssociationNewFilesTwo);
        }};
        FileLogRecord fileLogRecord = FileLogRecord.builder()
                .logModule(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .operator("admin")
                .projectId(project.getId())
                .build();
        List<String> associationIdList = fileAssociationService.association("sty-file-association-bug-id-3", FileAssociationSourceUtil.SOURCE_TYPE_BUG, fileIdList, fileLogRecord);
        this.checkFileAssociation("sty-file-association-bug-id-3", fileIdList);
        this.saveSourceAssociationId("sty-file-association-bug-id-3", associationIdList);
        //重新关联（检查是否会重复插入数据）
        associationIdList = fileAssociationService.association("sty-file-association-bug-id-3", FileAssociationSourceUtil.SOURCE_TYPE_BUG, fileIdList, fileLogRecord);
        this.checkFileAssociation("sty-file-association-bug-id-3", fileIdList);
        this.saveSourceAssociationId("sty-file-association-bug-id-3", associationIdList);

        //关联id-3 关联旧文件
        fileIdList = new ArrayList<>() {{
            this.add(fileAssociationOldFileId);
        }};
        associationIdList = fileAssociationService.association("sty-file-association-bug-id-3", FileAssociationSourceUtil.SOURCE_TYPE_BUG, fileIdList, fileLogRecord);
        this.checkFileAssociation("sty-file-association-bug-id-3", fileIdList);
        this.saveSourceAssociationId("sty-file-association-bug-id-3", associationIdList);

        //关联id-2 （含有不存在的ID)
        List<String> bug2IdList = new ArrayList<>() {{
            this.add(fileAssociationNewFilesOne);
            this.add(fileAssociationNewFilesTwo);
            this.add(fileAssociationNewFilesThree);
            this.add(IDGenerator.nextStr());
        }};
        associationIdList = fileAssociationService.association("sty-file-association-bug-id-2", FileAssociationSourceUtil.SOURCE_TYPE_BUG, bug2IdList, fileLogRecord);
        Assertions.assertEquals(bug2IdList.size() - 1, associationIdList.size());
        this.saveSourceAssociationId("sty-file-association-bug-id-2", associationIdList);

        //反例：
        // 文件参数为空
        boolean error = false;
        try {
            fileAssociationService.association("sty-file-association-bug-id-3", FileAssociationSourceUtil.SOURCE_TYPE_BUG, null, fileLogRecord);
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);
        error = false;
        try {
            fileAssociationService.association("sty-file-association-bug-id-3", FileAssociationSourceUtil.SOURCE_TYPE_BUG, new ArrayList<>(), fileLogRecord);
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);
        //资源不存在
        error = false;
        try {
            fileAssociationService.association("sty-file-association-bug-id-3", FileAssociationSourceUtil.SOURCE_TYPE_BUG, new ArrayList<>() {{

            }}, fileLogRecord);
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);

    }

    public void associationUpgrade() {
        FileLogRecord fileLogRecord = FileLogRecord.builder()
                .logModule(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .operator("admin")
                .projectId(project.getId())
                .build();

        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andFileIdEqualTo(fileAssociationOldFileId).andSourceIdEqualTo("sty-file-association-bug-id-4");
        FileAssociation upgradeFileAssociation = fileAssociationMapper.selectByExample(example).getFirst();
        Assertions.assertTrue(sourceAssociationFileMap.get("sty-file-association-bug-id-4").contains(upgradeFileAssociation.getId()));
        //当前文件不是最新的
        fileAssociationService.upgrade(upgradeFileAssociation.getId(), fileLogRecord);
        FileAssociation newAssociation1 = fileAssociationMapper.selectByPrimaryKey(upgradeFileAssociation.getId());
        Assertions.assertEquals(newAssociation1.getFileId(), fileAssociationNewFileId);

        //当前文件是最新的
        fileAssociationService.upgrade(upgradeFileAssociation.getId(), fileLogRecord);
        FileAssociation newAssociation2 = fileAssociationMapper.selectByPrimaryKey(upgradeFileAssociation.getId());
        Assertions.assertEquals(newAssociation1.getFileId(), newAssociation2.getFileId());


        //关联ID不存在
        boolean error = false;
        try {
            fileAssociationService.upgrade(IDGenerator.nextStr(), fileLogRecord);
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);

        //使用bug-id-2测试： 1.关联表中的文件ID不存在
        example.clear();
        example.createCriteria().andFileIdEqualTo(fileAssociationOldFileId).andSourceIdEqualTo("sty-file-association-bug-id-2");
        FileAssociation upgrade2 = fileAssociationMapper.selectByExample(example).getFirst();
        //先把文件id改成别的，测试完成改回来
        String originalFileId = upgrade2.getFileId();
        String originalRefId = upgrade2.getFileRefId();
        upgrade2.setFileId(IDGenerator.nextStr());
        upgrade2.setFileRefId(upgrade2.getFileId());
        fileAssociationMapper.updateByPrimaryKeySelective(upgrade2);
        error = false;
        try {
            fileAssociationService.upgrade(upgrade2.getId(), fileLogRecord);
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);
        upgrade2.setFileId(originalFileId);
        upgrade2.setFileRefId(originalRefId);
        fileAssociationMapper.updateByPrimaryKeySelective(upgrade2);

        //使用bug-id-2测试： 脏数据->refId找不到最新文件
        FileMetadata updateMetadata = new FileMetadata();
        updateMetadata.setId(fileAssociationNewFileId);
        updateMetadata.setLatest(false);
        fileMetadataMapper.updateByPrimaryKeySelective(updateMetadata);
        error = false;
        try {
            fileAssociationService.upgrade(upgrade2.getId(), fileLogRecord);
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);
        updateMetadata.setLatest(true);
        fileMetadataMapper.updateByPrimaryKeySelective(updateMetadata);
    }

    public void transferAndAssociation() throws Exception {
        FileLogRecord fileLogRecord = FileLogRecord.builder()
                .logModule(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .operator("admin")
                .projectId(project.getId())
                .build();
        //关联正常文件
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/file_upload.JPG")).getPath();
        String fileID = fileAssociationService.transferAndAssociation(new FileAssociationDTO("testTransferFile", "testTransferFile.jpg", TempFileUtils.getFile(filePath), "sty-file-association-bug-id-4", FileAssociationSourceUtil.SOURCE_TYPE_BUG, fileLogRecord));
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdEqualTo(fileID).andNameEqualTo("testTransferFile").andTypeEqualTo("JPG");
        Assertions.assertEquals(fileMetadataMapper.countByExample(example), 1);
        //重复转存检查是否报错
        boolean error = false;
        try {
            fileID = fileAssociationService.transferAndAssociation(new FileAssociationDTO("testTransferFile", "testTransferFile.jpg", TempFileUtils.getFile(filePath), "sty-file-association-bug-id-4", FileAssociationSourceUtil.SOURCE_TYPE_BUG, fileLogRecord));
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);

        //测试没有后缀的文件名
        fileID = fileAssociationService.transferAndAssociation(new FileAssociationDTO("testTransfer", "testTransfer", TempFileUtils.getFile(filePath), "sty-file-association-bug-id-4", FileAssociationSourceUtil.SOURCE_TYPE_BUG, fileLogRecord));
        example.clear();
        example.createCriteria().andIdEqualTo(fileID).andNameEqualTo("testTransfer");
        Assertions.assertEquals(fileMetadataMapper.countByExample(example), 1);
        //资源不存在
        error = false;
        try {
            fileAssociationService.transferAndAssociation(new FileAssociationDTO("testTransferFile", "testTransferFile.jpg", TempFileUtils.getFile(filePath), IDGenerator.nextStr(), FileAssociationSourceUtil.SOURCE_TYPE_BUG, fileLogRecord));
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);
        //文件名称不合法
        error = false;
        try {
            fileAssociationService.transferAndAssociation(new FileAssociationDTO("testTransfer/File", "testTransfer/File.jpg",  TempFileUtils.getFile(filePath), IDGenerator.nextStr(), FileAssociationSourceUtil.SOURCE_TYPE_BUG, fileLogRecord));
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);

        //文件名称非法
        error = false;
        try {
            fileMetadataService.transferFile(StringUtils.EMPTY, StringUtils.EMPTY, null, null, null, null);
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);
    }

    public void associationDelete() {
        FileLogRecord fileLogRecord = FileLogRecord.builder()
                .logModule(OperationLogModule.PROJECT_FILE_MANAGEMENT)
                .operator("admin")
                .projectId(project.getId())
                .build();

        //1.正常删除  资源为bug-2
        FileAssociationExample example = new FileAssociationExample();
        example.clear();
        example.createCriteria().andSourceIdEqualTo("sty-file-association-bug-id-2");
        List<FileAssociation> bug1AssociationList = fileAssociationMapper.selectByExample(example);
        List<String> idList = bug1AssociationList.stream().map(FileAssociation::getId).collect(Collectors.toList());
        int deleteCount = fileAssociationService.deleteByIds(idList, fileLogRecord);
        Assertions.assertEquals(idList.size(), deleteCount);
        example.clear();
        example.createCriteria().andIdIn(idList);
        Assertions.assertEquals(fileAssociationMapper.countByExample(example), 0);

        //删除完了重新关联，继续做测试
        fileAssociationService.association("sty-file-association-bug-id-2", FileAssociationSourceUtil.SOURCE_TYPE_BUG, new ArrayList<>() {{
            this.add(fileAssociationNewFilesOne);
            this.add(fileAssociationNewFilesTwo);
            this.add(fileAssociationNewFilesThree);
            this.add(fileAssociationNewFilesFour);
        }}, fileLogRecord);

        //2.入参集合为空
        deleteCount = fileAssociationService.deleteByIds(new ArrayList<>(), fileLogRecord);
        Assertions.assertEquals(0, deleteCount);
        deleteCount = fileAssociationService.deleteByIds(null, fileLogRecord);
        Assertions.assertEquals(0, deleteCount);

        //3.里面包含一条已经文件已经删除了的ID 资源为bug-2
        example.clear();
        example.createCriteria().andSourceIdEqualTo("sty-file-association-bug-id-2").andFileIdEqualTo(fileAssociationNewFilesThree);
        FileAssociation association = fileAssociationMapper.selectByExample(example).getFirst();
        //先把文件id改成别的，测试完成改回来
        association.setFileId(IDGenerator.nextStr());
        association.setFileRefId(association.getFileId());
        fileAssociationMapper.updateByPrimaryKeySelective(association);
        idList = new ArrayList<>() {{
            this.add(association.getId());
        }};
        fileAssociationService.deleteByIds(idList, fileLogRecord);
        example.clear();
        example.createCriteria().andIdIn(idList);
        Assertions.assertEquals(fileAssociationMapper.countByExample(example), 0);

        //4.入参集合包括1条不存在的关联ID 资源为bug-2
        example.clear();
        example.createCriteria().andSourceIdEqualTo("sty-file-association-bug-id-2");
        List<FileAssociation> bug2AssociationList = fileAssociationMapper.selectByExample(example);
        idList = bug2AssociationList.stream().map(FileAssociation::getId).collect(Collectors.toList());
        idList.add(IDGenerator.nextStr());
        deleteCount = fileAssociationService.deleteByIds(idList, fileLogRecord);
        Assertions.assertEquals(idList.size() - 1, deleteCount);
        example.clear();
        example.createCriteria().andIdIn(idList);
        Assertions.assertEquals(fileAssociationMapper.countByExample(example), 0);

        //5.直接删除sourceId为bug-3的
        List<String> bug2IdList = new ArrayList<>() {{
            this.add(fileAssociationNewFilesOne);
            this.add(fileAssociationNewFilesTwo);
            this.add(fileAssociationNewFilesThree);
            this.add(fileAssociationNewFilesFour);
        }};
        fileAssociationService.association("sty-file-association-bug-id-3", FileAssociationSourceUtil.SOURCE_TYPE_BUG, bug2IdList, fileLogRecord);
        fileAssociationService.association("sty-file-association-bug-id-2", FileAssociationSourceUtil.SOURCE_TYPE_BUG, bug2IdList, fileLogRecord);
        example.clear();
        example.createCriteria().andSourceIdEqualTo("sty-file-association-bug-id-3");
        long beforeDelete = fileAssociationMapper.countByExample(example);
        fileAssociationService.deleteBySourceIds(new ArrayList<>() {{
            this.add("sty-file-association-bug-id-2");
            this.add("sty-file-association-bug-id-3");
        }}, fileLogRecord);
        long afterDelete = fileAssociationMapper.countByExample(example);
        Assertions.assertTrue(beforeDelete > afterDelete);
        Assertions.assertEquals(afterDelete, 0);

        //参数测试
        Assertions.assertEquals(fileAssociationService.deleteBySourceIds(null, fileLogRecord), 0);
        Assertions.assertEquals(fileAssociationService.deleteBySourceIdAndFileIds(null, new ArrayList<>(), fileLogRecord), 0);
        Assertions.assertEquals(fileAssociationService.deleteBySourceIdAndFileIds(IDGenerator.nextStr(), new ArrayList<>() {{
            this.add(IDGenerator.nextStr());
        }}, fileLogRecord), 0);
        //重新关联，用来测试文件删除是否会级联删除。使用bug-3
        fileAssociationService.association("sty-file-association-bug-id-3", FileAssociationSourceUtil.SOURCE_TYPE_BUG, bug2IdList, fileLogRecord);
        fileAssociationService.association("sty-file-association-bug-id-2", FileAssociationSourceUtil.SOURCE_TYPE_BUG, bug2IdList, fileLogRecord);
    }


    /**
     * @param sourceId   资源ID
     * @param fileIdList 关联的文件集合
     */
    private void checkFileAssociation(String sourceId, List<String> fileIdList) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdEqualTo(sourceId).andFileIdIn(fileIdList);
        long count = fileAssociationMapper.countByExample(example);
        Assertions.assertEquals(count, fileIdList.size());
    }

    private String addFileAssociation(String sourceId, String sourceType, String fileId) {
        FileAssociation fileAssociation = new FileAssociation();
        fileAssociation.setId(IDGenerator.nextStr());
        fileAssociation.setFileId(fileId);
        fileAssociation.setFileRefId(fileId);
        fileAssociation.setSourceId(sourceId);
        fileAssociation.setSourceType(sourceType);
        fileAssociation.setCreateTime(System.currentTimeMillis());
        fileAssociation.setCreateUser("admin");
        fileAssociation.setUpdateTime(System.currentTimeMillis());
        fileAssociation.setUpdateUser("admin");
        fileAssociation.setFileVersion(fileId);
        fileAssociationMapper.insertSelective(fileAssociation);
        return fileAssociation.getId();
    }


    @Test
    @Order(40)
    @Sql(scripts = {"/dml/delete_bug.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void fileAssociationTestOver() throws Exception {

    }

    /*
    80以后是文件、模块的移动和删除
     */
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
                |   |           |
                |   |           ·a1-b1-c1
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

        //测试a1b1c1（child)节点无法移动到a1b1c1同级别
        {
            request.setDragNodeId(A1B1C1_CHILD_NODE.getId());
            request.setDropNodeId(a1b1Node.getId());
            request.setDropPosition(0);
            ResultActions resultActions = this.requestPost(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            resultActions.andExpect(status().is5xxServerError());
            MvcResult mvcResult = resultActions.andReturn();
            ResultHolder resultHolder = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
            Assertions.assertEquals(resultHolder.getMessage(), Translator.get("node.name.repeat"));
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

        LOG_CHECK_LIST.add(
                new CheckLogModel(a1Node.getId(), OperationLogType.UPDATE, FileManagementRequestUtils.URL_MODULE_MOVE)
        );
        LOG_CHECK_LIST.add(
                new CheckLogModel(a3Node.getId(), OperationLogType.UPDATE, FileManagementRequestUtils.URL_MODULE_MOVE)
        );
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
        LOG_CHECK_LIST.add(
                new CheckLogModel(picFileId, OperationLogType.UPDATE, FileManagementRequestUtils.URL_FILE_BATCH_UPDATE)
        );
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
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1b1Node.getId(), OperationLogType.DELETE, FileManagementRequestUtils.URL_MODULE_DELETE)
        );

        // 删除有文件的节点 a1-a1      检查是否级联删除根节点
        BaseTreeNode a1a1Node = FileManagementBaseUtils.getNodeByName(this.getFileModuleTreeNode(), "a1-a1");
        this.requestGetWithOk(String.format(FileManagementRequestUtils.URL_MODULE_DELETE, a1a1Node.getId()));
        this.checkModuleIsEmpty(a1a1Node.getId());
        LOG_CHECK_LIST.add(
                new CheckLogModel(a1a1Node.getId(), OperationLogType.DELETE, FileManagementRequestUtils.URL_MODULE_DELETE)
        );

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
                |   |            |
                |   |            ·a1-b1-c1
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

        if (updateRequest.getTags() != null) {
            Assertions.assertTrue(CollectionUtils.isEqualCollection(fileMetadata.getTags(), updateRequest.getTags()));
        } else {
            List<String> fileTags = fileMetadata.getTags() == null ? new ArrayList<>() : fileMetadata.getTags();
            List<String> oldTags = oldFileMetadata.getTags() == null ? new ArrayList<>() : oldFileMetadata.getTags();
            Assertions.assertTrue(CollectionUtils.isEqualCollection(fileTags, oldTags));
        }
    }

    private void checkFileModuleInProject(String projectId, String moduleId) {
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andModuleIdNotEqualTo(moduleId).andLatestEqualTo(true);
        Assertions.assertEquals(fileMetadataMapper.countByExample(example), 0);
    }

    @Resource
    private FileService fileService;

    @Test
    @Order(91)
    public void testQuery() throws Exception {
        fileAssociationService.getFiles("TEST");
        fileAssociationService.getFileAssociations(Collections.singletonList("TEST"), FileAssociationSourceUtil.SOURCE_TYPE_FUNCTIONAL_CASE);


        //测试FileRequest的folder判断
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(IDGenerator.nextStr());
        fileRequest.setStorage(StorageType.MINIO.name());
        fileRequest.setFolder(IDGenerator.nextStr());
        boolean error = false;
        try {
            fileService.deleteFile(fileRequest);
        } catch (Exception e) {
            error = true;
        }
        Assertions.assertTrue(error);

        List<ModuleCountDTO> treeNodeList = new ArrayList<>() {{
            this.add(new ModuleCountDTO(IDGenerator.nextStr(), 1));
            this.add(new ModuleCountDTO(IDGenerator.nextStr(), 2));
            this.add(new ModuleCountDTO(IDGenerator.nextStr(), 3));
        }};
        fileModuleService.getAllCount(treeNodeList);
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

        // 接口模块单测调过了，这里增加项目模块通过率
        fileAssociationService.getByFileIdAndSourceId("a", "b");
    }

    @Test
    @Order(101)
    public void addCover() throws Exception {
        fileMetadataService.getByFileIds(List.of("a"));
    }
}
