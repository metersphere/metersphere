package io.metersphere.project.controller.filemanagement;

import io.metersphere.project.request.filemanagement.*;
import io.metersphere.project.utils.FileManagementBaseUtils;
import io.metersphere.project.utils.FileManagementRequestUtils;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.request.NodeMoveRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Objects;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FileManagementPermissionControllerTests extends BaseTest {
    private static final String TEST_ID = "test";

    @Test
    @Order(1)
    public void addModuleTestSuccess() throws Exception {
        FileModuleCreateRequest request = new FileModuleCreateRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("a1");
        this.requestPostPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD, FileManagementRequestUtils.URL_MODULE_ADD, request);
    }

    @Test

    @Order(2)
    public void updateModuleTestSuccess() throws Exception {
        FileModuleUpdateRequest updateRequest = new FileModuleUpdateRequest();
        updateRequest.setId(TEST_ID);
        updateRequest.setName("a1-a1");
        this.requestPostPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE, FileManagementRequestUtils.URL_MODULE_UPDATE, updateRequest);
    }

    @Test
    @Order(3)
    public void fileUploadTestSuccess() throws Exception {
        //这个权限无法校验
        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(DEFAULT_PROJECT_ID);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/file_upload.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileUploadRequest));
        this.requestMultipartPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD, FileManagementRequestUtils.URL_FILE_UPLOAD, paramMap);
    }


    @Test
    @Order(4)
    public void fileReUploadTestSuccess() throws Exception {

        FileUploadRequest fileUploadRequest = new FileUploadRequest();
        fileUploadRequest.setProjectId(DEFAULT_PROJECT_ID);

        //重新上传并修改文件版本
        FileReUploadRequest fileReUploadRequest = new FileReUploadRequest();
        fileReUploadRequest.setFileId(TEST_ID);
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("file/file_re-upload.JPG")).getPath();
        MockMultipartFile file = new MockMultipartFile("file", "file_re-upload.JPG", MediaType.APPLICATION_OCTET_STREAM_VALUE, FileManagementBaseUtils.getFileBytes(filePath));
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", file);
        paramMap.add("request", JSON.toJSONString(fileReUploadRequest));

        this.requestMultipartPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE, FileManagementRequestUtils.URL_FILE_RE_UPLOAD, paramMap);
    }

    @Test
    @Order(5)
    public void filePageTestSuccess() throws Exception {
        FileMetadataTableRequest request = new FileMetadataTableRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
            this.setProjectId(DEFAULT_PROJECT_ID);
        }};
        this.requestPostPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ, FileManagementRequestUtils.URL_FILE_PAGE, request);
    }


    @Test
    public void fileDeleteSuccess() throws Exception {
        FileBatchProcessDTO fileBatchProcessDTO = new FileBatchProcessDTO();
        fileBatchProcessDTO.setProjectId(DEFAULT_PROJECT_ID);
        fileBatchProcessDTO.setSelectIds(new ArrayList<>() {{
            this.add(TEST_ID);
        }});
        this.requestPostPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DELETE, FileManagementRequestUtils.URL_FILE_DELETE, fileBatchProcessDTO);
    }

    @Test
    public void fileUpdateSuccess() throws Exception {

        FileUpdateRequest updateRequest = new FileUpdateRequest();
        updateRequest.setId(TEST_ID);
        updateRequest.setName("update_" + TEST_ID);
        updateRequest.setTags(new ArrayList<>() {{
            this.add("tag1");
        }});
        this.requestPostPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE, FileManagementRequestUtils.URL_FILE_UPDATE, updateRequest);
    }


    @Test
    public void moveTest() throws Exception {

        {
            NodeMoveRequest request = new NodeMoveRequest();
            request.setNodeId(TEST_ID);
            request.setParentId(ModuleConstants.ROOT_NODE_PARENT_ID);
            this.requestPostPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE, FileManagementRequestUtils.URL_MODULE_MOVE, request);
        }
    }

    @Test
    @Order(90)
    public void deleteModuleTestSuccess() throws Exception {
        this.requestGetPermissionTest(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DELETE, String.format(FileManagementRequestUtils.URL_MODULE_DELETE, TEST_ID));
    }


}
