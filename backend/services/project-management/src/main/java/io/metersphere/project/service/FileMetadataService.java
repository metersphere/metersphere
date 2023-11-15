package io.metersphere.project.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.FileMetadataExample;
import io.metersphere.project.domain.FileMetadataRepository;
import io.metersphere.project.domain.FileModuleRepository;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.filemanagement.FileManagementQuery;
import io.metersphere.project.dto.filemanagement.request.*;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.mapper.ExtFileMetadataMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.FileMetadataRepositoryMapper;
import io.metersphere.project.mapper.FileModuleRepositoryMapper;
import io.metersphere.project.utils.FileDownloadUtils;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.TempFileUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.RemoteFileAttachInfo;
import io.metersphere.system.file.FileRepository;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.file.MinioRepository;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.GitRepositoryUtil;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileMetadataService {
    private static final String JAR_FILE_PREFIX = "jar";

    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private ExtFileMetadataMapper extFileMetadataMapper;
    @Resource
    private FileMetadataRepositoryMapper fileMetadataRepositoryMapper;
    @Resource
    private FileModuleRepositoryMapper fileModuleRepositoryMapper;
    @Resource
    private FileMetadataLogService fileMetadataLogService;
    @Resource
    private FileManagementService fileManagementService;
    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private FileService fileService;

    @Value("${metersphere.file.batch-download-max:600MB}")
    private DataSize batchDownloadMaxSize;
    @Value("50MB")
    private DataSize maxFileSize;

    public FileMetadata selectById(String id) {
        return fileMetadataMapper.selectByPrimaryKey(id);
    }

    public FileInformationResponse getFileInformation(String id) {
        FileMetadata fileMetadata = extFileMetadataMapper.getById(id);
        FileMetadataRepository repositoryMap = fileMetadataRepositoryMapper.selectByPrimaryKey(id);
        FileInformationResponse dto = new FileInformationResponse(fileMetadata, repositoryMap);
        initModuleName(dto);
        return dto;
    }

    public List<FileInformationResponse> list(FileMetadataTableRequest request) {
        List<FileInformationResponse> returnList = new ArrayList<>();
        FileManagementQuery pageDTO = new FileManagementQuery(request);
        List<FileMetadata> fileMetadataList = extFileMetadataMapper.selectByKeywordAndFileType(pageDTO);
        fileMetadataList.forEach(fileMetadata -> {
            FileInformationResponse fileInformationResponse = new FileInformationResponse(fileMetadata, null);
            returnList.add(fileInformationResponse);
        });
        this.initModuleName(returnList);
        return returnList;
    }

    private void initModuleName(List<FileInformationResponse> returnList) {
        List<String> moduleIds = returnList.stream().map(FileInformationResponse::getModuleId).distinct().toList();
        Map<String, String> moduleNameMap = fileModuleService.getModuleNameMapByIds(moduleIds);
        for (FileInformationResponse dto : returnList) {
            if (StringUtils.equals(dto.getModuleId(), ModuleConstants.DEFAULT_NODE_ID)) {
                dto.setModuleName(Translator.get("default.module"));
            } else {
                dto.setModuleName(moduleNameMap.get(dto.getModuleId()));
            }
        }
    }

    private void initModuleName(FileInformationResponse dto) {
        if (StringUtils.equals(dto.getModuleId(), ModuleConstants.DEFAULT_NODE_ID)) {
            dto.setModuleName(Translator.get("default.module"));
        } else {
            dto.setModuleName(fileModuleService.getModuleName(dto.getModuleId()));
        }
    }

    private void checkEnableFile(String fileType) {
        if (!StringUtils.equalsIgnoreCase(fileType, JAR_FILE_PREFIX)) {
            throw new MSException(Translator.get("file.not.jar"));
        }
    }

    public FileMetadata genFileMetadata(String filePath, String storage, long size, boolean enable, String projectId, String moduleId, String operator) {
        if (size > maxFileSize.toBytes()) {
            throw new MSException(Translator.get("file.size.is.too.large"));
        }
        String fileName = TempFileUtils.getFileNameByPath(filePath);
        FileMetadata fileMetadata = new FileMetadata();
        if (StringUtils.lastIndexOf(fileName, ".") > 0) {
            //采用这种判断方式，可以避免将隐藏文件的后缀名作为文件类型
            fileMetadata.setName(StringUtils.substring(fileName, 0, fileName.lastIndexOf(".")));
            fileMetadata.setType(StringUtils.substring(fileName, fileName.lastIndexOf(".") + 1));
        } else {
            fileMetadata.setName(fileName);
            fileMetadata.setType(StringUtils.EMPTY);
        }
        //如果开启了开关，检查是否是jar文件
        if (enable) {
            this.checkEnableFile(fileMetadata.getType());
        }
        //检查处理后的用户名合法性
        if (StringUtils.equals(storage, StorageType.MINIO.name())) {
            this.checkMinIOFileName(null, fileMetadata.getName(), projectId);
        } else {
            //Git： 存储库下的文件路径不能重复
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andPathEqualTo(filePath).andProjectIdEqualTo(projectId).andStorageEqualTo(StorageType.GIT.name()).andModuleIdEqualTo(moduleId);
            if (fileMetadataMapper.countByExample(example) > 0) {
                throw new MSException(Translator.get("file.name.exist") + ":" + fileName);
            }
        }

        fileMetadata.setId(IDGenerator.nextStr());
        fileMetadata.setStorage(storage);
        fileMetadata.setProjectId(projectId);
        fileMetadata.setModuleId(moduleId);
        long operationTime = System.currentTimeMillis();
        fileMetadata.setCreateTime(operationTime);
        fileMetadata.setCreateUser(operator);
        fileMetadata.setUpdateTime(operationTime);
        fileMetadata.setUpdateUser(operator);
        fileMetadata.setSize(size);
        fileMetadata.setPath(filePath);
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadata.setEnable(false);
        return fileMetadata;
    }

    public String upload(FileUploadRequest request, String operator, MultipartFile uploadFile) throws Exception {
        //检查模块的合法性
        fileManagementService.checkModule(request.getModuleId(), ModuleConstants.NODE_TYPE_DEFAULT);

        String fileName = StringUtils.trim(uploadFile.getOriginalFilename());

        FileMetadata fileMetadata = this.genFileMetadata(fileName, StorageType.MINIO.name(), uploadFile.getSize(), request.isEnable(), request.getProjectId(), request.getModuleId(), operator);

        // 上传文件
        String filePath = this.uploadFile(fileMetadata, uploadFile);

        fileMetadata.setPath(filePath);
        fileMetadata.setFileVersion(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);
        //记录日志
        fileMetadataLogService.saveUploadLog(fileMetadata, operator);

        return fileMetadata.getId();
    }


    /**
     * 文件转存
     *
     * @param fileName  文件名
     * @param projectId 项目ID
     * @param operator  操作人
     * @param fileBytes 文件字节
     * @return
     * @throws Exception
     */
    public String transferFile(String fileName, String projectId, String operator, byte[] fileBytes) throws Exception {
        if (StringUtils.isBlank(fileName)) {
            throw new MSException(Translator.get("file.name.cannot.be.empty"));
        }
        fileName = this.genTransferFileName(StringUtils.trim(fileName), projectId);
        String moduleId = ModuleConstants.NODE_TYPE_DEFAULT;

        FileMetadata fileMetadata = this.genFileMetadata(fileName, StorageType.MINIO.name(), fileBytes.length, false, projectId, moduleId, operator);

        FileRequest uploadFileRequest = new FileRequest();
        uploadFileRequest.setFileName(fileMetadata.getId());
        uploadFileRequest.setProjectId(fileMetadata.getProjectId());
        uploadFileRequest.setStorage(StorageType.MINIO.name());

        FileRepository minio = CommonBeanFactory.getBean(MinioRepository.class);
        String filePath = minio.saveFile(fileBytes, uploadFileRequest);
        fileMetadata.setPath(filePath);
        fileMetadata.setFileVersion(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);
        return fileMetadata.getId();
    }

    private String genTransferFileName(String fullFileName, String projectId) {
        if (StringUtils.containsAny(fullFileName, "/")) {
            throw new MSException(Translator.get("file.name.error"));
        }

        String fileName;
        String fileType = null;
        if (StringUtils.lastIndexOf(fullFileName, ".") > 0) {
            //采用这种判断方式，可以避免将隐藏文件的后缀名作为文件类型
            fileName = StringUtils.substring(fullFileName, 0, fullFileName.lastIndexOf("."));
            fileType = StringUtils.substring(fullFileName, fullFileName.lastIndexOf(".") + 1);
        } else {
            fileName = fullFileName;
        }

        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andNameEqualTo(fileName).andProjectIdEqualTo(projectId);

        int fileIndex = 0;
        String originFileName = fileName;
        while (fileMetadataMapper.countByExample(example) > 0) {
            fileIndex++;
            fileName = originFileName + "(" + fileIndex + ")";
            example = new FileMetadataExample();
            example.createCriteria().andNameEqualTo(fileName).andProjectIdEqualTo(projectId);
        }

        if (StringUtils.isNotEmpty(fileType)) {
            fileName = fileName + "." + fileType;
        }
        return fileName;
    }

    private void checkMinIOFileName(String id, String fileName, String projectId) {
        if (StringUtils.isBlank(fileName)) {
            throw new MSException(Translator.get("file.name.cannot.be.empty"));
        }
        FileMetadataExample example = new FileMetadataExample();
        if (StringUtils.isBlank(id)) {
            example.createCriteria().andNameEqualTo(fileName).andProjectIdEqualTo(projectId).andStorageEqualTo(StorageType.MINIO.name());
        } else {
            example.createCriteria().andNameEqualTo(fileName).andProjectIdEqualTo(projectId).andIdNotEqualTo(id).andStorageEqualTo(StorageType.MINIO.name());
        }
        if (fileMetadataMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("file.name.exist") + ":" + fileName);
        }
    }

    private static final String FILE_MODULE_COUNT_ALL = "all";

    private String uploadFile(FileMetadata fileMetadata, MultipartFile file) throws Exception {
        FileRequest uploadFileRequest = new FileRequest();
        uploadFileRequest.setFileName(fileMetadata.getId());
        uploadFileRequest.setProjectId(fileMetadata.getProjectId());
        uploadFileRequest.setStorage(StorageType.MINIO.name());
        return fileService.upload(file, uploadFileRequest);
    }

    public byte[] getFileByte(FileMetadata fileMetadata) {
        String filePath = null;
        if (TempFileUtils.isImgTmpFileExists(fileMetadata.getId())) {
            filePath = TempFileUtils.getTmpFilePath(fileMetadata.getId());
        } else {
            try {
                filePath = TempFileUtils.createFile(TempFileUtils.getTmpFilePath(fileMetadata.getId()), this.getFile(fileMetadata));
            } catch (Exception ignore) {
            }
        }
        return TempFileUtils.getFile(filePath);
    }

    public ResponseEntity<byte[]> downloadById(String id) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(id);
        byte[] bytes = this.getFileByte(fileMetadata);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + this.getFileName(fileMetadata.getId(), fileMetadata.getType()) + "\"")
                .body(bytes);
    }

    private String getFileName(String fileName, String type) {
        if (StringUtils.isBlank(type)) {
            return fileName;
        }
        return fileName + "." + type;
    }

    private byte[] getFile(FileMetadata fileMetadata) throws Exception {
        if (fileMetadata == null) {
            throw new MSException(Translator.get("file.not.exist"));
        }
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(fileMetadata.getId());
        fileRequest.setProjectId(fileMetadata.getProjectId());
        fileRequest.setStorage(fileMetadata.getStorage());

        //获取git文件下载
        if (StringUtils.equals(fileMetadata.getStorage(), StorageType.GIT.name())) {
            FileModuleRepository fileModuleRepository = fileModuleRepositoryMapper.selectByPrimaryKey(fileMetadata.getModuleId());
            FileMetadataRepository fileMetadataRepository = fileMetadataRepositoryMapper.selectByPrimaryKey(fileMetadata.getId());
            fileRequest.setGitFileRequest(fileModuleRepository, fileMetadataRepository);
        }

        return fileService.download(fileRequest);
    }

    public void update(FileUpdateRequest request, String operator) {
        //检查模块的合法性
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(request.getId());
        if (fileMetadata == null) {
            throw new MSException(Translator.get("file.not.exist"));
        }

        //检查是否是空参数
        if (!StringUtils.isAllBlank(request.getName(), request.getDescription(), request.getModuleId())
                || request.getEnable() != null
                || request.getTags() != null) {
            FileMetadata updateExample = new FileMetadata();
            updateExample.setId(request.getId());
            updateExample.setDescription(request.getDescription());
            updateExample.setModuleId(request.getModuleId());
            if (StringUtils.isNotBlank(request.getName())) {
                this.checkMinIOFileName(request.getId(), request.getName(), fileMetadata.getProjectId());
                updateExample.setName(request.getName());
            }
            if (request.getTags() != null) {
                updateExample.setTags(JSON.toJSONString(request.getTags()));
            } else {
                updateExample.setTags(null);
            }
            if (request.getEnable() != null) {
                this.checkEnableFile(fileMetadata.getType());
                updateExample.setEnable(request.getEnable());
            }
            updateExample.setUpdateUser(operator);
            updateExample.setUpdateTime(System.currentTimeMillis());
            fileMetadataMapper.updateByPrimaryKeySelective(updateExample);

            FileMetadata newFile = fileMetadataMapper.selectByPrimaryKey(request.getId());
            //记录日志
            fileMetadataLogService.saveUpdateLog(fileMetadata, newFile, fileMetadata.getProjectId(), operator);
        }
    }

    public Pager<List<FileInformationResponse>> page(FileMetadataTableRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "update_time desc");
        return PageUtils.setPageInfo(page, this.list(request));
    }

    public ResponseEntity<byte[]> batchDownload(FileBatchProcessRequest request) {
        List<FileMetadata> fileMetadataList = fileManagementService.getProcessList(request);
        this.checkDownloadSize(fileMetadataList);
        try {
            byte[] bytes = this.batchDownload(fileMetadataList);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "files.zip")
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.status(509).body(e.getMessage().getBytes());
        }
    }

    public byte[] batchDownload(List<FileMetadata> fileMetadataList) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        fileMetadataList.forEach(fileMetadata -> {
            byte[] bytes = this.getFileByte(fileMetadata);
            files.put(this.getFileName(fileMetadata.getName(), fileMetadata.getType()), bytes);
        });
        return FileDownloadUtils.listBytesToZip(files);
    }

    //检查下载的文件的大小
    private void checkDownloadSize(List<FileMetadata> fileMetadataList) {
        AtomicLong fileSize = new AtomicLong();
        if (CollectionUtils.isNotEmpty(fileMetadataList)) {
            fileMetadataList.forEach(item -> fileSize.addAndGet(item.getSize()));
        }

        if (fileSize.get() == 0) {
            throw new MSException(Translator.get("file.is.empty"));
        }

        DataSize dataSize = DataSize.ofBytes(fileSize.get());
        if (batchDownloadMaxSize.compareTo(dataSize) < 0) {
            throw new MSException(Translator.get("file.size.is.too.large"));
        }
    }
    private static final String FILE_MODULE_COUNT_MY = "my";

    /**
     * 重新上传
     */
    public String reUpload(FileReUploadRequest request, String operator, MultipartFile uploadFile) throws Exception {
        //检查模块的合法性
        FileMetadata oldFile = fileMetadataMapper.selectByPrimaryKey(request.getFileId());
        if (oldFile == null) {
            throw new MSException(Translator.get("old.file.not.exist"));
        }

        if (!StringUtils.equals(oldFile.getStorage(), StorageType.MINIO.name())) {
            //非minio类型文件不允许重新上传
            throw new MSException(Translator.get("file.not.exist"));
        }
        this.setFileVersionIsOld(oldFile, operator);
        FileMetadata fileMetadata = this.genNewVersion(oldFile, operator);
        fileMetadata.setSize(uploadFile.getSize());
        fileMetadataMapper.insert(fileMetadata);
        //记录日志
        fileMetadataLogService.saveReUploadLog(fileMetadata, operator);
        // 上传文件
        String filePath = this.uploadFile(fileMetadata, uploadFile);
        FileMetadata updateFileMetadata = new FileMetadata();
        updateFileMetadata.setId(fileMetadata.getId());
        updateFileMetadata.setPath(filePath);
        updateFileMetadata.setFileVersion(fileMetadata.getId());
        fileMetadataMapper.updateByPrimaryKeySelective(updateFileMetadata);

        return fileMetadata.getId();
    }

    //获取模块统计
    public Map<String, Long> moduleCount(FileMetadataTableRequest request, String operator) {
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        FileManagementQuery pageDTO = new FileManagementQuery(request);
        pageDTO.setModuleIds(null);
        List<ModuleCountDTO> moduleCountDTOList = extFileMetadataMapper.countModuleIdByKeywordAndFileType(pageDTO);
        long allCount = fileModuleService.getAllCount(moduleCountDTOList);
        //查出我的文件数量
        pageDTO.setOperator(operator);
        long myFileCount = extFileMetadataMapper.countMyFile(pageDTO);
        Map<String, Long> moduleCountMap = fileModuleService.getModuleCountMap(request.getProjectId(), moduleCountDTOList);
        moduleCountMap.put(FILE_MODULE_COUNT_MY, myFileCount);
        moduleCountMap.put(FILE_MODULE_COUNT_ALL, allCount);
        return moduleCountMap;
    }

    public ResponseEntity<byte[]> downloadPreviewImgById(String id) throws Exception {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(id);
        String previewImgPath = null;
        if (TempFileUtils.isImage(fileMetadata.getType())) {
            if (TempFileUtils.isImgPreviewFileExists(fileMetadata.getId())) {
                previewImgPath = TempFileUtils.getPreviewImgFilePath(fileMetadata.getId());
            } else {
                previewImgPath = TempFileUtils.catchCompressImgIfNotExists(fileMetadata.getId(), this.getFile(fileMetadata));
            }
        }

        byte[] bytes;
        if (StringUtils.isNotBlank(previewImgPath)) {
            bytes = TempFileUtils.getFile(previewImgPath);
        } else {
            bytes = new byte[]{};
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + this.getFileName(fileMetadata.getId(), fileMetadata.getType()) + "\"")
                .body(bytes);
    }

    public List<String> getFileType(String projectId, String storage) {
        return extFileMetadataMapper.selectFileTypeByProjectId(projectId, storage);
    }

    public void changeJarFileStatus(String fileId, boolean enable, String operator) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        if (fileMetadata == null) {
            throw new MSException(Translator.get("file.not.exist"));
        }
        this.checkEnableFile(fileMetadata.getType());
        FileMetadata updateModel = new FileMetadata();
        updateModel.setId(fileMetadata.getId());
        updateModel.setEnable(enable);
        updateModel.setUpdateTime(System.currentTimeMillis());
        updateModel.setUpdateUser(operator);
        fileMetadataMapper.updateByPrimaryKeySelective(updateModel);
        fileMetadataLogService.saveChangeJarFileStatusLog(fileMetadata, enable, operator);
    }

    public void batchMove(FileBatchMoveRequest request, String operator) {
        //检查模块的合法性
        fileManagementService.checkModule(request.getMoveModuleId(), ModuleConstants.NODE_TYPE_DEFAULT);
        List<FileMetadata> fileMetadataList = fileManagementService.getProcessList(request);
        List<String> updateFileId = new ArrayList<>();
        List<FileMetadata> logList = new ArrayList<>();
        for (FileMetadata fileMetadata : fileMetadataList) {
            if (!StringUtils.equals(fileMetadata.getModuleId(), request.getMoveModuleId())) {
                updateFileId.add(fileMetadata.getId());
                logList.add(fileMetadata);
            }
        }
        if (CollectionUtils.isNotEmpty(updateFileId)) {
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andIdIn(updateFileId);
            FileMetadata updateModel = new FileMetadata();
            updateModel.setModuleId(request.getMoveModuleId());
            updateModel.setUpdateTime(System.currentTimeMillis());
            updateModel.setUpdateUser(operator);
            fileMetadataMapper.updateByExampleSelective(updateModel, example);
            //记录日志
            fileMetadataLogService.saveFileMoveLog(logList, request.getProjectId(), operator);
        }
    }

    private void setFileVersionIsOld(FileMetadata oldFile, String operator) {
        //删除旧的预览文件
        TempFileUtils.deleteTmpFile(oldFile.getId());
        //更新文件版本分支
        FileMetadata updateModel = new FileMetadata();
        updateModel.setId(oldFile.getId());
        updateModel.setLatest(false);
        updateModel.setUpdateTime(System.currentTimeMillis());
        updateModel.setUpdateUser(operator);
        fileMetadataMapper.updateByPrimaryKeySelective(updateModel);
    }

    private FileMetadata genNewVersion(FileMetadata oldFile, String operator) {
        long operationTime = System.currentTimeMillis();
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(IDGenerator.nextStr());
        fileMetadata.setStorage(oldFile.getStorage());
        fileMetadata.setProjectId(oldFile.getProjectId());
        fileMetadata.setModuleId(oldFile.getModuleId());
        fileMetadata.setName(oldFile.getName());
        fileMetadata.setType(oldFile.getType());
        fileMetadata.setCreateTime(operationTime);
        fileMetadata.setCreateUser(oldFile.getCreateUser());
        fileMetadata.setUpdateTime(operationTime);
        fileMetadata.setPath(oldFile.getPath());
        fileMetadata.setUpdateUser(operator);
        fileMetadata.setRefId(oldFile.getRefId());
        fileMetadata.setEnable(oldFile.getEnable());
        fileMetadata.setLatest(true);
        return fileMetadata;
    }

    public String pullFile(String fileId, String operator) {
        FileMetadata oldFile = fileMetadataMapper.selectByPrimaryKey(fileId);
        String returnFileId = fileId;
        if (StringUtils.equals(oldFile.getStorage(), StorageType.GIT.name())) {
            FileMetadataRepository metadataRepository = fileMetadataRepositoryMapper.selectByPrimaryKey(fileId);
            FileModuleRepository moduleRepository = fileModuleRepositoryMapper.selectByPrimaryKey(oldFile.getModuleId());
            if (metadataRepository != null && moduleRepository != null) {

                GitRepositoryUtil repositoryUtils = new GitRepositoryUtil(moduleRepository.getUrl(), moduleRepository.getUserName(), moduleRepository.getToken());
                RemoteFileAttachInfo gitFileAttachInfo = repositoryUtils.selectLastCommitIdByBranch(metadataRepository.getBranch(), oldFile.getPath());

                if (!StringUtils.equals(gitFileAttachInfo.getCommitId(), metadataRepository.getCommitId())) {
                    this.setFileVersionIsOld(oldFile, operator);
                    FileMetadata fileMetadata = this.genNewVersion(oldFile, operator);
                    fileMetadata.setFileVersion(gitFileAttachInfo.getCommitId());
                    fileMetadata.setSize(gitFileAttachInfo.getSize());
                    fileMetadataMapper.insert(fileMetadata);
                    returnFileId = fileMetadata.getId();

                    FileMetadataRepository fileMetadataRepository = new FileMetadataRepository();
                    fileMetadataRepository.setFileMetadataId(returnFileId);
                    fileMetadataRepository.setBranch(gitFileAttachInfo.getBranch());
                    fileMetadataRepository.setCommitId(gitFileAttachInfo.getCommitId());
                    fileMetadataRepository.setCommitMessage(gitFileAttachInfo.getCommitMessage());
                    fileMetadataRepositoryMapper.insert(fileMetadataRepository);

                    //记录日志
                    fileMetadataLogService.saveFilePullLog(oldFile, fileMetadata, operator);
                }
            }
        }
        return returnFileId;
    }

    public List<FileMetadata> selectByList(List<String> fileIds) {
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        return fileMetadataMapper.selectByExample(example);
    }

    public FileMetadata selectLatestFileByRefId(String refId) {
        FileMetadataExample fileMetadataExample = new FileMetadataExample();
        fileMetadataExample.createCriteria().andRefIdEqualTo(refId).andLatestEqualTo(true);
        List<FileMetadata> fileMetadataList = fileMetadataMapper.selectByExample(fileMetadataExample);
        if (CollectionUtils.isNotEmpty(fileMetadataList)) {
            return fileMetadataList.get(0);
        } else {
            throw new MSException(Translator.get("latest.file.not.exist"));
        }
    }
}
