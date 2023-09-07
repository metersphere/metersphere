package io.metersphere.project.controller.filemanagement;

import io.metersphere.project.domain.*;
import io.metersphere.project.dto.FileTableResult;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.FileModuleMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.request.filemanagement.*;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.project.utils.FileManagementBaseUtils;
import io.metersphere.project.utils.FileManagementRequestUtils;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.dto.BaseTreeNode;
import io.metersphere.sdk.dto.request.NodeMoveRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.uid.UUID;
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

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FileManagementControllerTests extends BaseTest {

    private static Project project;

    private static List<BaseTreeNode> preliminaryTreeNodes = new ArrayList<>();

    private static final Map<String, String> FILE_ID_PATH = new LinkedHashMap<>();

    private static final Map<String, String> FILE_VERSIONS_ID_MAP = new HashMap<>();

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
        if (project == null) {
            Project initProject = new Project();
            initProject.setId(UUID.randomUUID().toString());
            initProject.setNum(null);
            initProject.setOrganizationId("100001");
            initProject.setName("建国创建的项目");
            initProject.setDescription("建国创建的项目");
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
        //检查有没有默认节点
        boolean hasNode = false;
        for (BaseTreeNode baseTreeNode : treeNodes) {
            if (StringUtils.equals(baseTreeNode.getId(), ModuleConstants.DEFAULT_NODE_ID)) {
                hasNode = true;
            }
        }
        Assertions.assertTrue(hasNode);

        //空数据下，检查文件列表
        FileMetadataTableRequest request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
        }};
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        FileTableResult result = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FileTableResult.class);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(result.getTableData().getCurrent(), request.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(result.getTableData().getList())).size() <= request.getPageSize());
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
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode childNode : baseTreeNode.getChildren()) {
                    if (StringUtils.equals(childNode.getName(), "a1-b1")) {
                        a1b1Node = childNode;
                    }
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
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode childNode : baseTreeNode.getChildren()) {
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
            if (StringUtils.equals(baseTreeNode.getName(), "a1") && CollectionUtils.isNotEmpty(baseTreeNode.getChildren())) {
                for (BaseTreeNode secondNode : baseTreeNode.getChildren()) {
                    if (StringUtils.equals(secondNode.getName(), "a1") && CollectionUtils.isNotEmpty(secondNode.getChildren())) {
                        for (BaseTreeNode thirdNode : secondNode.getChildren()) {
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
        request.setParentId(UUID.randomUUID().toString());
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
        request.setProjectId(UUID.randomUUID().toString());
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
        updateRequest.setId(UUID.randomUUID().toString());
        updateRequest.setName(UUID.randomUUID().toString());
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

        //文件上传到a1-a1节点
        BaseTreeNode a1a1Node = FileManagementBaseUtils.getNodeByName(preliminaryTreeNodes, "a1-a1");
        fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(project.getId());
        fileUploadRequest.setModuleId(a1a1Node.getId());
        filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/txtFile.txt")).getPath();
        file = new MockMultipartFile("file", "txtFile.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
        returnId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        checkLog(returnId, OperationLogType.ADD, FileManagementRequestUtils.URL_FILE_UPLOAD);
        FILE_ID_PATH.put(returnId, filePath);
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

        //模块不存在
        fileUploadRequest.setModuleId(UUID.randomUUID().toString());
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
        String existFileId = null;
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            this.fileUploadTestSuccess();
        }
        for (String key : FILE_ID_PATH.keySet()) {
            existFileId = key;
        }

        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(project.getId());

        //重新上传并修改文件版本
        FileReUploadRequest fileReUploadRequest = new FileReUploadRequest();
        fileReUploadRequest.setFileId(existFileId);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/file_re-upload.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_re-upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileReUploadRequest));

        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(FileManagementRequestUtils.URL_FILE_RE_UPLOAD, paramMap);
        String reUploadId = JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData().toString();
        checkLog(reUploadId, OperationLogType.UPDATE, FileManagementRequestUtils.URL_FILE_RE_UPLOAD);
        FILE_ID_PATH.put(reUploadId, filePath);
        FILE_VERSIONS_ID_MAP.put(reUploadId, existFileId);
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
        fileReUploadRequest.setFileId(UUID.randomUUID().toString());
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
    }

    @Test
    @Order(16)
    public void fileDownloadTestError() throws Exception {
        //下载不存在的文件
        mockMvc.perform(getRequestBuilder(FileManagementRequestUtils.URL_FILE_DOWNLOAD, UUID.randomUUID()))
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
        this.filePageRequestAndCheck(request, true);

        //查找默认模块
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setModuleIds(new ArrayList<>() {{
                this.add(ModuleConstants.DEFAULT_NODE_ID);
            }});
        }};
        this.filePageRequestAndCheck(request, true);

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
        this.filePageRequestAndCheck(request, true);
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
        this.filePageRequestAndCheck(request, false);

        //查找不存在的模块
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setModuleIds(new ArrayList<>() {{
                this.add(UUID.randomUUID().toString());
            }});
        }};
        this.filePageRequestAndCheck(request, false);

        //使用已存在的文件类型过滤 区分大小写
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setFileTypes(new ArrayList<>() {{
                this.add("JPG");
            }});
        }};
        this.filePageRequestAndCheck(request, true);

        //使用已存在的文件类型过滤 不区分大小写
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setFileTypes(new ArrayList<>() {{
                this.add("JpG");
            }});
        }};
        this.filePageRequestAndCheck(request, true);

        //使用不存在的文件类型过滤
        request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(project.getId());
            this.setFileTypes(new ArrayList<>() {{
                this.add("fire");
            }});
        }};
        this.filePageRequestAndCheck(request, false);
    }

    @Test
    @Order(17)
    public void fileBatchDownload() throws Exception {
        if (MapUtils.isEmpty(FILE_ID_PATH)) {
            this.fileReUploadTestSuccess();
        }
        //下载全部文件
        FileBatchProcessDTO batchProcessDTO = new FileBatchProcessDTO();
        batchProcessDTO.setSelectAll(true);
        batchProcessDTO.setProjectId(project.getId());
        MvcResult mvcResult = this.batchDownloadFile(FileManagementRequestUtils.URL_FILE_BATCH_DOWNLOAD, batchProcessDTO);
        byte[] fileBytes = mvcResult.getResponse().getContentAsByteArray();
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
        batchProcessDTO = new FileBatchProcessDTO();
        batchProcessDTO.setSelectAll(false);
        batchProcessDTO.setProjectId(project.getId());
        batchProcessDTO.setSelectIds(new ArrayList<>() {{
            this.add(UUID.randomUUID().toString());
        }});

        mockMvc.perform(getPostRequestBuilder(FileManagementRequestUtils.URL_FILE_BATCH_DOWNLOAD, batchProcessDTO))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Order(20)
    public void fileDeleteSuccess() throws Exception {
        if (MapUtils.isEmpty(FILE_VERSIONS_ID_MAP)) {
            this.fileReUploadTestSuccess();
        }
        FileBatchProcessDTO fileBatchProcessDTO;
        //删除指定文件
        for (Map.Entry<String, String> entry : FILE_VERSIONS_ID_MAP.entrySet()) {
            String fileMetadataId = entry.getKey();
            String refId = entry.getValue();

            fileBatchProcessDTO = new FileBatchProcessDTO();
            fileBatchProcessDTO.setProjectId(project.getId());
            fileBatchProcessDTO.setSelectIds(new ArrayList<>() {{
                this.add(fileMetadataId);
            }});
            this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_DELETE, fileBatchProcessDTO);

            this.checkFileIsDeleted(fileMetadataId, refId);
            checkLog(fileMetadataId, OperationLogType.DELETE, FileManagementRequestUtils.URL_FILE_DELETE);
        }
        FILE_VERSIONS_ID_MAP.clear();

        //全部删除
        fileBatchProcessDTO = new FileBatchProcessDTO();
        fileBatchProcessDTO.setSelectAll(true);
        fileBatchProcessDTO.setProjectId(project.getId());
        fileBatchProcessDTO.setExcludeIds(new ArrayList<>() {{
            this.add(UUID.randomUUID().toString());
        }});
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_DELETE, fileBatchProcessDTO);
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andProjectIdEqualTo(project.getId());
        Assertions.assertTrue(fileMetadataMapper.countByExample(example) == 0);
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
        checkLog(updateRequest.getId(), OperationLogType.UPDATE, FileManagementRequestUtils.URL_FILE_UPDATE);

        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(updateFileId);
        Assertions.assertNotNull(fileMetadata);
        Assertions.assertEquals(updateRequest.getName(), fileMetadata.getName());
        Assertions.assertEquals(updateRequest.getDescription(), fileMetadata.getDescription());
        Assertions.assertEquals(updateRequest.getModuleId(), fileMetadata.getModuleId());
        List<String> list = JSON.parseArray(fileMetadata.getTags(), String.class);
        Assertions.assertTrue(list.size() == 1);
        Assertions.assertTrue(StringUtils.equals(list.get(0), "tag1"));

        //判断什么也不改
        updateRequest = new FileUpdateRequest();
        updateRequest.setId(updateFileId);
        this.requestPostWithOk(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest);

        FileMetadata fileMetadata2 = fileMetadataMapper.selectByPrimaryKey(updateFileId);
        Assertions.assertNotNull(fileMetadata2);
        Assertions.assertEquals(fileMetadata2.getName(), fileMetadata.getName());
        Assertions.assertEquals(fileMetadata2.getDescription(), fileMetadata.getDescription());
        Assertions.assertEquals(fileMetadata2.getModuleId(), fileMetadata.getModuleId());
        List<String> list2 = JSON.parseArray(fileMetadata2.getTags(), String.class);
        Assertions.assertTrue(list.size() == 1);
        Assertions.assertTrue(StringUtils.equals(list2.get(0), "tag1"));

    }

    @Test
    @Order(22)
    public void fileUpdateError() throws Exception {

        //参数校验
        FileUpdateRequest updateRequest = new FileUpdateRequest();
        this.requestPost(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest).andExpect(status().isBadRequest());

        //文件不存在
        updateRequest.setId(UUID.randomUUID().toString());
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
        updateRequest.setModuleId(UUID.randomUUID().toString());
        this.requestPost(FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest).andExpect(status().is5xxServerError());

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
            request.setNodeId(a1Node.getId());
            request.setPreviousNodeId(a3Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1Node.getId(), null, false);
        }
        //父节点内移动-移动到末位  在上面的基础上，a1挪到a2上面
        {
            request = new NodeMoveRequest();
            request.setNodeId(a1Node.getId());
            request.setNextNodeId(a2Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a2Node.getId(), null, false);
        }

        //父节点内移动-移动到中位 a1移动到a2-a3中间
        {
            request = new NodeMoveRequest();
            request.setNodeId(a1Node.getId());
            request.setPreviousNodeId(a2Node.getId());
            request.setNextNodeId(a3Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a1Node.getId(), a3Node.getId(), false);
            //移动回去
            request = new NodeMoveRequest();
            request.setNodeId(a1Node.getId());
            request.setNextNodeId(a2Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a2Node.getId(), null, false);
        }

        //跨节点移动-移动到首位   a3移动到a1-b1前面，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setNextNodeId(a1b1Node.getId());
            request.setParentId(a1Node.getId());
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1b1Node.getId(), null, false);
            //移动回去
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a2Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //跨节点移动-移动到末尾   a3移动到a1-a1后面，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a1a1Node.getId());
            request.setParentId(a1Node.getId());
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1a1Node.getId(), a3Node.getId(), null, false);
            //移动回去
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a2Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //跨节点移动-移动到中位   a3移动到a1-b1和a1-a1中间，然后移动回来;
        {
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a1b1Node.getId());
            request.setNextNodeId(a1a1Node.getId());
            request.setParentId(a1Node.getId());
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1b1Node.getId(), a3Node.getId(), a1a1Node.getId(), false);
            //移动回去
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a2Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //父节点内移动-a3移动到首位pos小于2，是否触发计算函数 （先手动更改a1的pos为2，然后移动a3到a1前面）
        {
            //更改pos
            FileModule updateModule = new FileModule();
            updateModule.setId(a1Node.getId());
            updateModule.setPos(2);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setNextNodeId(a1Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1Node.getId(), null, true);

            //移动回去
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a2Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        //父节点内移动-移动到中位，前后节点pos差不大于2，是否触发计算函数（在上面的 a3-a1-a2的基础上， 先手动更改a1pos为3*64，a2的pos为3*64+2，然后移动a3到a1和a2中间）
        {
            //更改pos
            FileModule updateModule = new FileModule();
            updateModule.setId(a1Node.getId());
            updateModule.setPos(3 * 64);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);
            updateModule.setId(a2Node.getId());
            updateModule.setPos(3 * 64 + 2);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a1Node.getId());
            request.setNextNodeId(a2Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1Node.getId(), a3Node.getId(), a2Node.getId(), true);

            //移动回去
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a2Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //跨节点移动-移动到首位pos小于2，是否触发计算函数（先手动更改a1-b1的pos为2，然后移动a3到a1-b1前面，最后再移动回来）
        {
            //更改pos
            FileModule updateModule = new FileModule();
            updateModule.setId(a1b1Node.getId());
            updateModule.setPos(2);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setNextNodeId(a1b1Node.getId());
            request.setParentId(a1Node.getId());
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a3Node.getId(), a1b1Node.getId(), null, true);

            //移动回去
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a2Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //跨节点移动-移动到中位，前后节点pos差不大于2，是否触发计算函数先手动更改a1-a1的pos为a1-b1+2，然后移动a3到a1-a1前面，最后再移动回来）
        {
            //更改pos
            FileModule updateModule = new FileModule();
            updateModule.setId(a1b1Node.getId());
            updateModule.setPos(3 * 64);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);
            updateModule.setId(a1a1Node.getId());
            updateModule.setPos(3 * 64 + 2);
            fileModuleMapper.updateByPrimaryKeySelective(updateModule);

            //开始移动
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a1b1Node.getId());
            request.setNextNodeId(a1a1Node.getId());
            request.setParentId(a1Node.getId());
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a1b1Node.getId(), a3Node.getId(), a1a1Node.getId(), true);

            //移动回去
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a2Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }
        //移动到没有子节点的节点下  a3移动到a2下
        {
            //开始移动
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setParentId(a2Node.getId());
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            FileModule a3Module = fileModuleMapper.selectByPrimaryKey(a3Node.getId());
            Assertions.assertEquals(a3Module.getParentId(), a2Node.getId());

            //移动回去
            request = new NodeMoveRequest();
            request.setNodeId(a3Node.getId());
            request.setPreviousNodeId(a2Node.getId());
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostWithOk(FileManagementRequestUtils.URL_MODULE_MOVE, request);
            this.checkModulePos(a2Node.getId(), a3Node.getId(), null, false);
        }

        checkLog(a1Node.getId(), OperationLogType.UPDATE, FileManagementRequestUtils.URL_MODULE_MOVE);
        checkLog(a3Node.getId(), OperationLogType.UPDATE, FileManagementRequestUtils.URL_MODULE_MOVE);
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
        this.requestGetWithOk(String.format(FileManagementRequestUtils.URL_MODULE_DELETE, UUID.randomUUID()));
        // 测试删除根节点
        this.requestGetWithOk(String.format(FileManagementRequestUtils.URL_MODULE_DELETE, ModuleConstants.DEFAULT_NODE_ID));

        //service层判断：测试删除空集合
        fileModuleService.deleteModule(new ArrayList<>());
    }

    private void checkModuleIsEmpty(String id) {
        FileModuleExample example = new FileModuleExample();
        example.createCriteria().andParentIdEqualTo(id);
        Assertions.assertTrue(fileModuleMapper.countByExample(example) == 0);

        FileMetadataExample metadataExample = new FileMetadataExample();
        example = new FileModuleExample();
        example.createCriteria().andIdEqualTo(id);
        Assertions.assertTrue(fileModuleMapper.countByExample(example) == 0);
        metadataExample.createCriteria().andModuleIdEqualTo(id);
        Assertions.assertTrue(fileMetadataMapper.countByExample(metadataExample) == 0);
    }

    private void checkFileIsDeleted(String fileId, String refId) {
        FileMetadataExample metadataExample = new FileMetadataExample();
        metadataExample.createCriteria().andIdEqualTo(fileId);
        Assertions.assertTrue(fileMetadataMapper.countByExample(metadataExample) == 0);

        metadataExample = new FileMetadataExample();
        metadataExample.createCriteria().andRefIdEqualTo(refId);
        Assertions.assertTrue(fileMetadataMapper.countByExample(metadataExample) == 0);
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

    private void filePageRequestAndCheck(FileMetadataTableRequest request, Boolean hasData) throws Exception {
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FileManagementRequestUtils.URL_FILE_PAGE, request);
        FileTableResult result = JSON.parseObject(JSON.toJSONString(
                        JSON.parseObject(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class).getData()),
                FileTableResult.class);
        FileManagementBaseUtils.checkFilePage(result, request, hasData);
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
                |   |        ·a1-a1-c1
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
}
