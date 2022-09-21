package io.metersphere.metadata.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.ByteUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtFileMetadataMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestFileMapper;
import io.metersphere.commons.constants.ApiTestConstants;
import io.metersphere.commons.constants.FileAssociationType;
import io.metersphere.commons.constants.FileModuleTypeConstants;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.metadata.utils.GitRepositoryUtils;
import io.metersphere.metadata.utils.MetadataUtils;
import io.metersphere.metadata.vo.*;
import io.metersphere.metadata.vo.repository.FileInfoDTO;
import io.metersphere.metadata.vo.repository.FileRelevanceCaseDTO;
import io.metersphere.metadata.vo.repository.FileVersionDTO;
import io.metersphere.metadata.vo.repository.GitFileAttachInfo;
import io.metersphere.performance.request.QueryProjectFileRequest;
import io.metersphere.service.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileMetadataService {
    @Resource
    private ExtFileMetadataMapper extFileMetadataMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileManagerService fileManagerService;
    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private LoadTestFileMapper loadTestFileMapper;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private FileContentMapper fileContentMapper;
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private UserService userService;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private TestCaseMapper testCaseMapper;
    @Resource
    private ExtLoadTestFileMapper extLoadTestFileMapper;

    public List<FileMetadata> create(FileMetadataCreateRequest fileMetadata, List<MultipartFile> files) {
        List<FileMetadata> result = new ArrayList<>();
        if (fileMetadata == null) {
            fileMetadata = new FileMetadataCreateRequest();
        }
        if (StringUtils.equals(StorageConstants.GIT.name(), fileMetadata.getStorage())) {
            fileMetadata.setPath(StringUtils.trim(fileMetadata.getPath()));
            this.validateGitFile(fileMetadata);
            FileModule fileModule = fileModuleService.get(fileMetadata.getModuleId());
            GitRepositoryUtils repositoryUtils = new GitRepositoryUtils(
                    fileModule.getRepositoryPath(), fileModule.getRepositoryUserName(), fileModule.getRepositoryToken());
            GitFileAttachInfo gitFileInfo = repositoryUtils.selectLastCommitIdByBranch(fileMetadata.getRepositoryBranch(), fileMetadata.getRepositoryPath());
            if (gitFileInfo != null) {
                fileMetadata.setName(MetadataUtils.getFileNameByRemotePath(fileMetadata.getRepositoryPath()));
                fileMetadata.setType(MetadataUtils.getFileType(fileMetadata.getRepositoryPath()));
                fileMetadata.setPath(fileMetadata.getRepositoryPath());
                fileMetadata.setSize(gitFileInfo.getSize());
                fileMetadata.setAttachInfo(JSONObject.toJSONString(gitFileInfo));
                result.add(this.save(fileMetadata));
            } else {
                MSException.throwException("File not found!");
            }
        } else if (!CollectionUtils.isEmpty(files)) {
            for (MultipartFile file : files) {
                QueryProjectFileRequest request = new QueryProjectFileRequest();
                request.setName(file.getOriginalFilename());
                result.add(this.saveFile(file, fileMetadata));
            }
        }
        return result;
    }

    private void validateGitFile(FileMetadataCreateRequest fileMetadata) {
        if (StringUtils.isEmpty(fileMetadata.getModuleId())) {
            MSException.throwException(Translator.get("test_case_module_not_null"));
        } else {
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andModuleIdEqualTo(fileMetadata.getModuleId())
                    .andStorageEqualTo(fileMetadata.getStorage())
                    .andPathEqualTo(fileMetadata.getRepositoryPath())
                    .andIdNotEqualTo(fileMetadata.getId());
            if (fileMetadataMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("project_file_already_exists"));
            }
        }
    }

    public FileMetadata save(FileMetadataWithBLOBs fileMetadata) {
        long createTime = System.currentTimeMillis();
        fileMetadata.setCreateTime(createTime);
        fileMetadata.setUpdateTime(createTime);
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);
        return fileMetadata;
    }

    public FileMetadata saveFile(MultipartFile file, FileMetadataWithBLOBs fileMetadata) {
        this.initBase(fileMetadata);
        if (StringUtils.isEmpty(fileMetadata.getName())) {
            fileMetadata.setName(file.getOriginalFilename());
        }
        checkName(fileMetadata);
        fileMetadata.setSize(file.getSize());
        String fileType = MetadataUtils.getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType);
        // 上传文件
        FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
        String path = fileManagerService.upload(file, request);
        fileMetadata.setPath(path);
        if (fileMetadataMapper.selectByPrimaryKey(fileMetadata.getId()) == null) {
            fileMetadata.setLatest(true);
            fileMetadata.setRefId(fileMetadata.getId());
            fileMetadataMapper.insert(fileMetadata);
        } else {
            fileMetadataMapper.updateByPrimaryKeyWithBLOBs(fileMetadata);
        }

        return fileMetadata;
    }


    public FileMetadata saveFile(MultipartFile file, String projectId) {
        FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
        fileMetadata.setProjectId(projectId);
        return saveFile(file, fileMetadata);
    }

    public List<FileMetadataWithBLOBs> getProjectFiles(String projectId, QueryProjectFileRequest request) {
        if (CollectionUtils.isEmpty(request.getOrders())) {
            OrderRequest req = new OrderRequest();
            req.setName("update_time");
            req.setType("desc");
            request.setOrders(new ArrayList<>() {{
                this.add(req);
            }});
        }
        List<FileMetadataWithBLOBs> fileMetadataList = extFileMetadataMapper.getProjectFiles(projectId, request);
        return fileMetadataList;
    }

    public void deleteFile(String fileId) {
        // 删除性能测试引用
        LoadTestFileExample example = new LoadTestFileExample();
        example.createCriteria().andFileIdEqualTo(fileId);
        List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(example);
        String errorMessage = "";
        if (loadTestFiles.size() > 0) {
            List<String> testIds = loadTestFiles.stream().map(LoadTestFile::getTestId).distinct().collect(Collectors.toList());
            LoadTestExample testExample = new LoadTestExample();
            testExample.createCriteria().andIdIn(testIds);
            List<LoadTest> loadTests = loadTestMapper.selectByExample(testExample);
            errorMessage += Translator.get("load_test") + ": " + StringUtils.join(loadTests.stream().map(LoadTest::getName).toArray(), ",");
            errorMessage += "\n";
        }
        if (StringUtils.isNotBlank(errorMessage)) {
            MSException.throwException(errorMessage + Translator.get("project_file_in_use"));
        }
        // 删除文件引用关系
        FileAssociationExample associationExample = new FileAssociationExample();
        associationExample.createCriteria().andFileMetadataIdEqualTo(fileId);
        fileAssociationMapper.deleteByExample(associationExample);

        // 删除文件,历史遗留数据保留附件只删除关系
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        fileMetadataMapper.deleteByPrimaryKey(fileId);

        if (StringUtils.isNotEmpty(fileMetadata.getRefId())) {
            //删除其余版本的文件
            FileMetadataExample fileMetadataExample = new FileMetadataExample();
            fileMetadataExample.createCriteria().andRefIdEqualTo(fileMetadata.getRefId());
            fileMetadataMapper.deleteByExample(fileMetadataExample);
        }

        if (StringUtils.isNotEmpty(fileMetadata.getStorage()) && StringUtils.isEmpty(fileMetadata.getResourceType())) {
            FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
            fileManagerService.delete(request);
        }
    }

    public void deleteBatch(List<String> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            ids.forEach(item -> {
                deleteFile(item);
            });
        }
    }

    public List<String> getTypes() {
        return extFileMetadataMapper.getTypes();
    }

    public void move(MoveFIleMetadataRequest request) {
        //不可移动到存储库模块节点
        FileModule fileModule = fileModuleService.get(request.getModuleId());
        if (fileModule != null && !CollectionUtils.isEmpty(request.getMetadataIds()) && StringUtils.isNotEmpty(request.getModuleId())) {
            if (StringUtils.equals(fileModule.getModuleType(), FileModuleTypeConstants.REPOSITORY.getValue())) {
                MSException.throwException(Translator.get("can_not_move_to_repository_node"));
            } else {
                extFileMetadataMapper.move(request);
            }
        }
    }

    public byte[] getContent(String id) {
        FileContent fileContent = fileContentMapper.selectByPrimaryKey(id);
        if (fileContent != null) {
            return fileContent.getFile();
        }
        return null;
    }

    public byte[] loadFileAsBytes(String id) {
        FileMetadataWithBLOBs fileMetadata = fileMetadataMapper.selectByPrimaryKey(id);
        if (fileMetadata == null) {
            return new byte[0];
        }
        return this.loadFileAsBytes(fileMetadata);
    }

    private byte[] loadFileAsBytes(FileMetadataWithBLOBs fileMetadata) {
        byte[] bytes = new byte[0];
        // 兼容历史数据
        if (StringUtils.isEmpty(fileMetadata.getStorage()) && StringUtils.isEmpty(fileMetadata.getResourceType())) {
            bytes = getContent(fileMetadata.getId());
        }
        if (ByteUtils.isEmpty(bytes)) {
            FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
            request.setResourceType(fileMetadata.getResourceType());
            request.setPath(fileMetadata.getPath());
            request.setStorage(fileMetadata.getStorage());
            request.setFileAttachInfoByString(fileMetadata.getAttachInfo());
            bytes = fileManagerService.downloadFile(request);
        }
        return bytes;
    }

    public ResponseEntity<byte[]> getFile(String fileId) {
        MediaType contentType = MediaType.parseMediaType("application/octet-stream");
        FileMetadataWithBLOBs fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        if (fileMetadata == null) {
            return null;
        }
        byte[] bytes = loadFileAsBytes(fileMetadata);
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + MetadataUtils.getFileName(fileMetadata.getName(), fileMetadata.getType()) + "\"")
                .body(bytes);
    }

    public byte[] exportZip(DownloadRequest request) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        request.getRequests().forEach(fileMetadata -> {
            byte[] bytes = loadFileAsBytes(fileMetadata);
            if (bytes != null) {
                files.put(MetadataUtils.getFileName(fileMetadata.getName(), fileMetadata.getType()), bytes);
            }
        });

        return FileUtils.listBytesToZip(files);
    }


    private void checkName(FileMetadata fileMetadata) {
        //文件库的名字不用判断是否唯一.是根据文件库路径自动解析生成的。
        if (!StringUtils.equals(StorageConstants.GIT.name(), fileMetadata.getStorage())) {
            FileMetadataExample example = new FileMetadataExample();
            FileMetadataExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(fileMetadata.getName())
                    .andProjectIdEqualTo(fileMetadata.getProjectId())
                    .andIdNotEqualTo(fileMetadata.getId());
            if (fileMetadataMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("project_file_already_exists"));
            }
        }
    }

    private String getBeforeName(FileMetadata fileMetadata) {
        return fileMetadataMapper.selectByPrimaryKey(fileMetadata.getId()).getName();
    }

    public void update(FileMetadataWithBLOBs fileMetadata) {
        this.checkName(fileMetadata);
        String beforeName = getBeforeName(fileMetadata);
        if (!StringUtils.equalsIgnoreCase(beforeName, fileMetadata.getName())
                && StringUtils.isNotEmpty(fileMetadata.getStorage()) && StringUtils.isEmpty(fileMetadata.getResourceType())) {
            boolean isReName = fileManagerService.reName(beforeName, fileMetadata.getName(), fileMetadata.getProjectId());
            if (!isReName) {
                MSException.throwException("重命名失败！");
            }
        }
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        fileMetadata.setUpdateUser(SessionUtils.getUserId());
        // 历史数据的路径不做更新
        if (StringUtils.isNotEmpty(fileMetadata.getStorage()) && StringUtils.isEmpty(fileMetadata.getResourceType())) {
            fileMetadata.setPath(FileUtils.getFilePath(fileMetadata));
        }
        fileMetadataMapper.updateByPrimaryKeySelective(fileMetadata);
    }

    public FileMetadata reLoad(FileMetadataWithBLOBs fileMetadata, List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            return fileMetadata;
        }
        fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileMetadata.getId());
        if (fileMetadata == null) {
            MSException.throwException("数据已经被删除！");
        }
        fileMetadata.setSize(files.get(0).getSize());
        String fileType = MetadataUtils.getFileType(files.get(0).getOriginalFilename());
        fileMetadata.setType(fileType);
        if (StringUtils.isEmpty(fileMetadata.getStorage())) {
            fileMetadata.setStorage(StorageConstants.LOCAL.name());
        }
        // 上传文件
        FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
        String path = fileManagerService.coverFile(files.get(0), request);
        // 更新关系数据
        fileMetadata.setPath(path);
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        fileMetadata.setUpdateUser(SessionUtils.getUserId());
        fileMetadataMapper.updateByPrimaryKeySelective(fileMetadata);
        return fileMetadata;
    }

    public FileMetadata getFileMetadataById(String fileId) {
        return fileMetadataMapper.selectByPrimaryKey(fileId);
    }

    public List<FileMetadata> uploadFiles(String projectId, List<MultipartFile> files) {
        List<FileMetadata> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(files)) {
            FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
            fileMetadata.setProjectId(projectId);
            fileMetadata.setStorage(StorageConstants.LOCAL.name());
            files.forEach(file -> {
                QueryProjectFileRequest request = new QueryProjectFileRequest();
                request.setName(file.getOriginalFilename());
                if (CollectionUtils.isEmpty(this.getProjectFiles(fileMetadata.getProjectId(), request))) {
                    result.add(this.saveFile(file, fileMetadata));
                } else {
                    MSException.throwException(Translator.get("project_file_already_exists"));
                }
            });
        }
        return result;
    }

    public FileMetadata updateFile(String fileId, MultipartFile file) {
        FileMetadataWithBLOBs fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        reLoad(fileMetadata, new ArrayList<>() {{
            this.add(file);
        }});
        return fileMetadata;
    }

    public void dumpFile(DumpFileRequest request, List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            // 文件已经存储过了
            String path = StringUtils.join(FileUtils.BODY_FILE_DIR, File.separator, request.getResourceId(), File.separator, request.getFileName());
            if (request.isCsv()) {
                path = StringUtils.join(FileUtils.BODY_FILE_DIR, File.separator, request.getResourceId(), "_", request.getFileName());
            }
            File file = new File(path);
            if (!file.exists()) {
                MSException.throwException("文件不存在！");
            }
            if (request.isCsv()) {
                this.saveFile(file, request.getFileName());
            } else {
                this.saveFile(file);
            }
        } else {
            FileMetadataCreateRequest fileMetadata = new FileMetadataCreateRequest();
            fileMetadata.setProjectId(request.getProjectId());
            fileMetadata.setModuleId(request.getModuleId());
            this.create(fileMetadata, files);
        }
    }

    public List<String> getJar(List<String> projectIds) {
        if (!CollectionUtils.isEmpty(projectIds)) {
            FileMetadataExample fileMetadata = new FileMetadataExample();
            fileMetadata.createCriteria().andProjectIdIn(projectIds).andLoadJarEqualTo(true);
            List<FileMetadata> files = fileMetadataMapper.selectByExample(fileMetadata);
            files = files.stream().filter(s -> StringUtils.isNotEmpty(s.getPath())).collect(Collectors.toList());
            return files.stream().map(FileMetadata::getPath).collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    public long myFiles(String createUser, String projectId) {
        FileMetadataExample fileMetadata = new FileMetadataExample();
        fileMetadata.createCriteria().andProjectIdEqualTo(projectId).andCreateUserEqualTo(createUser);
        return fileMetadataMapper.countByExample(fileMetadata);
    }

    public String getLogDetails(String id) {
        FileMetadata fileMetadata = this.getFileMetadataById(id);
        if (fileMetadata != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(fileMetadata, SystemReference.projectColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(fileMetadata.getId()), fileMetadata.getProjectId(), fileMetadata.getName(), null, columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public boolean isFileExits(String fileId) {
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdEqualTo(fileId);
        long fileCount = fileMetadataMapper.countByExample(example);
        if (fileCount > 0) {
            return true;
        } else {
            return false;
        }
    }

    public FileMetadata saveFile(File file) {
        if (file.exists()) {
            byte[] bytes = FileUtils.fileToByte(file);
            this.saveFile(bytes, file.getName(), file.length());
        }
        return null;
    }

    public void saveFile(File file, String name) {
        if (file.exists()) {
            byte[] bytes = FileUtils.fileToByte(file);
            this.saveFile(bytes, name, file.length());
        }
    }

    public FileMetadata saveFile(byte[] fileByte, String fileName, Long fileSize) {
        final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
        this.initBase(fileMetadata);
        fileMetadata.setName(fileName);
        fileMetadata.setSize(fileSize);
        String fileType = MetadataUtils.getFileType(fileName);
        fileMetadata.setType(fileType);
        checkName(fileMetadata);
        FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
        String path = fileManagerService.upload(fileByte, request);
        fileMetadata.setPath(path);
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);
        return fileMetadata;
    }

    private void initBase(FileMetadata fileMetadata) {
        if (fileMetadata == null) {
            fileMetadata = new FileMetadata();
        }
        if (StringUtils.isEmpty(fileMetadata.getId())) {
            fileMetadata.setId(UUID.randomUUID().toString());
        }
        if (StringUtils.isEmpty(fileMetadata.getStorage())) {
            fileMetadata.setStorage(StorageConstants.LOCAL.name());
        }
        if (StringUtils.isEmpty(fileMetadata.getProjectId())) {
            fileMetadata.setProjectId(SessionUtils.getCurrentProjectId());
        }
        if (StringUtils.isEmpty(fileMetadata.getModuleId()) || StringUtils.equals(ApiTestConstants.ROOT, fileMetadata.getModuleId())) {
            fileMetadata.setModuleId(fileModuleService.getDefaultNodeId(fileMetadata.getProjectId()));
        }
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());

        if (StringUtils.isEmpty(fileMetadata.getCreateUser())) {
            fileMetadata.setCreateUser(SessionUtils.getUserId());
        }
        if (StringUtils.isEmpty(fileMetadata.getUpdateUser())) {
            fileMetadata.setUpdateUser(SessionUtils.getUserId());
        }
    }

    public boolean exist(String fileId) {
        return fileMetadataMapper.selectByPrimaryKey(fileId) != null;
    }

    public List<String> exists(List<String> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return new LinkedList<>();
        }
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        List<FileMetadata> fileMetadataList = fileMetadataMapper.selectByExample(example);
        return fileMetadataList.stream().map(FileMetadata::getId).collect(Collectors.toList());
    }

    public List<FileInfoDTO> downloadFileByIds(Collection<String> fileIdList) {
        if (CollectionUtils.isEmpty(fileIdList)) {
            return new ArrayList<>(0);
        }
        LogUtil.info(JSONObject.toJSONString(fileIdList) + " 获取文件开始");

        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(new ArrayList<>(fileIdList));
        List<FileMetadataWithBLOBs> fileMetadataWithBLOBs = fileMetadataMapper.selectByExampleWithBLOBs(example);

        List<FileRequest> requestList = new ArrayList<>();
        fileMetadataWithBLOBs.forEach(fileMetadata -> {
            FileRequest request = new FileRequest(fileMetadata.getProjectId(), fileMetadata.getName(), fileMetadata.getType());
            request.setResourceId(fileMetadata.getId());
            request.setResourceType(fileMetadata.getResourceType());
            request.setPath(fileMetadata.getPath());
            request.setStorage(fileMetadata.getStorage());
            if (StringUtils.equals(fileMetadata.getStorage(), StorageConstants.GIT.name())) {
                try {
                    GitFileAttachInfo gitFileInfo = JSONObject.parseObject(fileMetadata.getAttachInfo(), GitFileAttachInfo.class);
                    request.setFileAttachInfo(gitFileInfo);
                } catch (Exception e) {
                    LogUtil.error("解析Git附加信息【" + fileMetadata.getAttachInfo() + "】失败!", e);
                }
            }
            requestList.add(request);
        });

        List<FileInfoDTO> repositoryFileDTOList = fileManagerService.downloadFileBatch(requestList);
        LogUtil.info(JSONObject.toJSONString(fileIdList) + " 获取文件结束。");
        return repositoryFileDTOList;
    }

    public FileMetadata pullFromRepository(FileMetadata request) {
        FileMetadata returnModel = null;
        FileMetadataWithBLOBs baseMetadata = fileMetadataMapper.selectByPrimaryKey(request.getId());
        if (StringUtils.equals(baseMetadata.getStorage(), StorageConstants.GIT.name()) && StringUtils.isNotEmpty(baseMetadata.getAttachInfo())) {
            GitFileAttachInfo baseAttachInfo = JSONObject.parseObject(baseMetadata.getAttachInfo(), GitFileAttachInfo.class);
            FileModule fileModule = fileModuleService.get(baseMetadata.getModuleId());
            if (fileModule != null) {
                GitRepositoryUtils repositoryUtils = new GitRepositoryUtils(fileModule.getRepositoryPath(), fileModule.getRepositoryUserName(), fileModule.getRepositoryToken());
                GitFileAttachInfo gitFileAttachInfo = repositoryUtils.selectLastCommitIdByBranch(baseAttachInfo.getBranch(), baseAttachInfo.getFilePath());
                if (gitFileAttachInfo != null &&
                        !StringUtils.equals(gitFileAttachInfo.getCommitId(), baseAttachInfo.getCommitId())) {
                    //有新的commitId，更新filemetadata的版本
                    long thistime = System.currentTimeMillis();
                    FileMetadataWithBLOBs newMetadata = this.genOtherVersionFileMetadata(baseMetadata, thistime, gitFileAttachInfo);
                    fileMetadataMapper.insert(newMetadata);

                    baseMetadata.setUpdateTime(thistime);
                    baseMetadata.setLatest(false);
                    baseMetadata.setUpdateUser(SessionUtils.getUserId());
                    fileMetadataMapper.updateByPrimaryKeySelective(baseMetadata);
                }
            }
        }
        return returnModel;
    }

    private FileMetadataWithBLOBs genOtherVersionFileMetadata(FileMetadataWithBLOBs baseMetadata, long operationTime, GitFileAttachInfo gitFileAttachInfo) {
        FileMetadataWithBLOBs newMetadata = new FileMetadataWithBLOBs();
        newMetadata.setDescription(baseMetadata.getDescription());
        newMetadata.setId(UUID.randomUUID().toString());
        newMetadata.setAttachInfo(JSONObject.toJSONString(gitFileAttachInfo));
        newMetadata.setName(baseMetadata.getName());
        newMetadata.setType(baseMetadata.getType());
        newMetadata.setSize(gitFileAttachInfo.getSize());
        newMetadata.setCreateTime(operationTime);
        newMetadata.setUpdateTime(operationTime);
        newMetadata.setStorage(baseMetadata.getStorage());
        newMetadata.setCreateUser(SessionUtils.getUserId());
        newMetadata.setProjectId(baseMetadata.getProjectId());
        newMetadata.setUpdateUser(SessionUtils.getUserId());
        newMetadata.setTags(baseMetadata.getTags());
        newMetadata.setLoadJar(baseMetadata.getLoadJar());
        newMetadata.setModuleId(baseMetadata.getModuleId());
        newMetadata.setPath(baseMetadata.getPath());
        newMetadata.setResourceType(baseMetadata.getResourceType());
        newMetadata.setRefId(baseMetadata.getRefId());
        newMetadata.setLatest(true);
        return newMetadata;
    }

    public List<FileVersionDTO> selectFileVersion(String refId) {
        List<FileVersionDTO> returnList = new ArrayList<>();
        if (StringUtils.isNotBlank(refId)) {
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andRefIdEqualTo(refId);
            List<FileMetadataWithBLOBs> fileMetadataList = this.fileMetadataMapper.selectByExampleWithBLOBs(example);
            fileMetadataList.sort(Comparator.comparing(FileMetadataWithBLOBs::getCreateTime).reversed());
            for (FileMetadataWithBLOBs fileMetadata : fileMetadataList) {
                try {
                    if (StringUtils.isNotBlank(fileMetadata.getAttachInfo())) {
                        GitFileAttachInfo gitFileAttachInfo = JSONObject.parseObject(fileMetadata.getAttachInfo(), GitFileAttachInfo.class);
                        User user = userService.selectById(fileMetadata.getCreateUser());
                        FileVersionDTO dto = new FileVersionDTO(fileMetadata.getId(), gitFileAttachInfo.getCommitId(), gitFileAttachInfo.getCommitMessage(),
                                user == null ? fileMetadata.getCreateUser() : user.getName(), fileMetadata.getCreateTime());
                        returnList.add(dto);
                    }
                } catch (Exception e) {
                    LogUtil.error("解析多版本下fileMetadata的attachInfo出错！", e);
                }
            }
        }
        return returnList;
    }

    public Pager<List<FileRelevanceCaseDTO>> getFileRelevanceCase(String refId, int goPage, int pageSize) {
        List<FileRelevanceCaseDTO> list = new ArrayList<>();
        Page<Object> page = null;
        if (StringUtils.isNotBlank(refId)) {
            FileMetadataExample fileMetadataExample = new FileMetadataExample();
            fileMetadataExample.createCriteria().andRefIdEqualTo(refId);
            List<FileMetadataWithBLOBs> fileMetadataWithBLOBsList = fileMetadataMapper.selectByExampleWithBLOBs(fileMetadataExample);
            if (CollectionUtils.isNotEmpty(fileMetadataWithBLOBsList)) {
                Map<String, String> fileCommitIdMap = new HashMap<>();
                fileMetadataWithBLOBsList.forEach(item -> {
                    if (StringUtils.isNotBlank(item.getAttachInfo()) && StringUtils.equals(item.getStorage(), StorageConstants.GIT.name())) {
                        try {
                            GitFileAttachInfo info = JSONObject.parseObject(item.getAttachInfo(), GitFileAttachInfo.class);
                            fileCommitIdMap.put(item.getId(), info.getCommitId());
                        } catch (Exception e) {
                            LogUtil.error("解析Git attachInfo失败！", e);
                        }
                    }
                });
                page = PageHelper.startPage(goPage, pageSize, true);
                FileAssociationExample associationExample = new FileAssociationExample();
                associationExample.createCriteria().andFileMetadataIdIn(new ArrayList<>(fileCommitIdMap.keySet())).andTypeIn(new ArrayList<>() {{
                    this.add(FileAssociationType.API.name());
                    this.add(FileAssociationType.CASE.name());
                    this.add(FileAssociationType.SCENARIO.name());
                    this.add("TEST_CASE");
                }});
                List<FileAssociation> fileAssociationList = fileAssociationMapper.selectByExample(associationExample);

                LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
                loadTestFileExample.createCriteria().andFileIdIn(new ArrayList<>(fileCommitIdMap.keySet()));
                List<LoadTestFile> loadTestFileList = loadTestFileMapper.selectByExample(loadTestFileExample);

                for (FileAssociation fileAssociation : fileAssociationList) {
                    String caseId = null;
                    String caseName = null;
                    if (StringUtils.equals(fileAssociation.getType(), FileAssociationType.API.name())) {
                        ApiDefinition apiDefinition = apiDefinitionMapper.selectByPrimaryKey(fileAssociation.getSourceId());
                        if (apiDefinition != null) {
                            caseId = apiDefinition.getNum() == null ? "" : apiDefinition.getNum().toString();
                            caseName = apiDefinition.getName();
                        }
                    } else if (StringUtils.equals(fileAssociation.getType(), FileAssociationType.CASE.name())) {
                        ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(fileAssociation.getSourceId());
                        if (testCase != null) {
                            caseId = testCase.getNum() == null ? "" : testCase.getNum().toString();
                            caseName = testCase.getName();
                        }
                    } else if (StringUtils.equals(fileAssociation.getType(), FileAssociationType.SCENARIO.name())) {
                        ApiScenario testCase = apiScenarioMapper.selectByPrimaryKey(fileAssociation.getSourceId());
                        if (testCase != null) {
                            caseId = testCase.getNum() == null ? "" : testCase.getNum().toString();
                            caseName = testCase.getName();
                        }
                    } else if (StringUtils.equals(fileAssociation.getType(), "TEST_CASE")) {
                        TestCase testCase = testCaseMapper.selectByPrimaryKey(fileAssociation.getSourceId());
                        if (testCase != null) {
                            caseId = testCase.getNum() == null ? "" : testCase.getNum().toString();
                            caseName = testCase.getName();
                        }
                    }

                    if (!StringUtils.isAllBlank(caseId, caseName)) {
                        FileRelevanceCaseDTO dto = new FileRelevanceCaseDTO(fileAssociation.getId(), caseId, caseName, fileAssociation.getType(), fileCommitIdMap.get(fileAssociation.getFileMetadataId()));
                        list.add(dto);
                    }
                }
                for (LoadTestFile loadTestFile : loadTestFileList) {
                    LoadTest loadTest = loadTestMapper.selectByPrimaryKey(loadTestFile.getTestId());
                    if (loadTest != null) {
                        FileRelevanceCaseDTO dto = new FileRelevanceCaseDTO(loadTestFile.getFileId(), loadTest.getNum() + "", loadTest.getName(), "LOAD_CASE"
                                , fileCommitIdMap.get(loadTestFile.getFileId()));
                        list.add(dto);
                    }
                }
            }
        }

        if (page == null) {
            page = new Page<>(goPage, pageSize);
        }

        //排序一下
        list.sort(Comparator.comparing(FileRelevanceCaseDTO::getCaseName));
        return PageUtils.setPageInfo(page, list);
    }

    public String updateCaseVersion(String refId, QueryProjectFileRequest request) {
        String returnString = "";
        int updateCount = 0;
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andRefIdEqualTo(refId).andLatestEqualTo(true);
        List<FileMetadata> fileMetadataList = fileMetadataMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(fileMetadataList)) {
            String latestId = fileMetadataList.get(0).getId();
            if (CollectionUtils.isNotEmpty(request.getIds())) {
                FileAssociationExample associationExample = new FileAssociationExample();
                associationExample.createCriteria().andIdIn(request.getIds());
                List<FileAssociation> fileAssociationList = fileAssociationMapper.selectByExample(associationExample);
                //同步更新具体的场景/用例/接口的hashTree
                this.updateResource(latestId, fileAssociationList);
                FileAssociation fileAssociation = new FileAssociation();
                fileAssociation.setFileMetadataId(latestId);
                updateCount = fileAssociationMapper.updateByExampleSelective(fileAssociation, associationExample);
            }
            if (MapUtils.isNotEmpty(request.getLoadCaseFileIdMap())) {
                for (Map.Entry<String, String> entry : request.getLoadCaseFileIdMap().entrySet()) {
                    String caseId = entry.getKey();
                    String fileId = entry.getValue();
                    extLoadTestFileMapper.updateFileIdByTestIdAndFileId(latestId, caseId, fileId);
                }
            }
        }

        returnString = String.valueOf(updateCount);
        return returnString;
    }

    private void updateResource(String newFileMetadataId, List<FileAssociation> fileAssociationList) {
        if (CollectionUtils.isNotEmpty(fileAssociationList)) {
            ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
            ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
            ApiAutomationService apiAutomationService = CommonBeanFactory.getBean(ApiAutomationService.class);
            fileAssociationList.forEach(item -> {
                if (StringUtils.equals(item.getType(), FileAssociationType.API.name()) && apiDefinitionService != null) {
                    ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionService.getBLOBs(item.getSourceId());
                    if (apiDefinitionWithBLOBs != null && StringUtils.isNotBlank(apiDefinitionWithBLOBs.getRequest())) {
                        String requestStr = apiDefinitionWithBLOBs.getRequest();
                        requestStr = StringUtils.replace(requestStr, item.getFileMetadataId(), newFileMetadataId);
                        apiDefinitionWithBLOBs.setRequest(requestStr);
                        apiDefinitionService.update(apiDefinitionWithBLOBs);
                    }
                } else if (StringUtils.equals(item.getType(), FileAssociationType.SCENARIO.name()) && apiAutomationService != null) {
                    ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiAutomationService.getById(item.getSourceId());
                    if (apiScenarioWithBLOBs != null && StringUtils.isNotBlank(apiScenarioWithBLOBs.getScenarioDefinition())) {
                        String requestStr = apiScenarioWithBLOBs.getScenarioDefinition();
                        requestStr = StringUtils.replace(requestStr, item.getFileMetadataId(), newFileMetadataId);
                        apiScenarioWithBLOBs.setScenarioDefinition(requestStr);
                        apiAutomationService.update(apiScenarioWithBLOBs);
                    }
                } else if (StringUtils.equals(item.getType(), FileAssociationType.CASE.name()) && apiTestCaseService != null) {
                    ApiTestCaseWithBLOBs testCaseWithBLOBs = apiTestCaseService.getById(item.getSourceId());
                    if (testCaseWithBLOBs != null && StringUtils.isNotBlank(testCaseWithBLOBs.getRequest())) {
                        String requestStr = testCaseWithBLOBs.getRequest();
                        requestStr = StringUtils.replace(requestStr, item.getFileMetadataId(), newFileMetadataId);
                        testCaseWithBLOBs.setRequest(requestStr);
                        apiTestCaseService.update(testCaseWithBLOBs);
                    }
                }
            });
        }
    }
}
