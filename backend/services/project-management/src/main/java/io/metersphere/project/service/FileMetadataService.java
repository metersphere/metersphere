package io.metersphere.project.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.domain.*;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.filemanagement.FileManagementQuery;
import io.metersphere.project.dto.filemanagement.request.*;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.dto.filemanagement.response.FileVersionResponse;
import io.metersphere.project.mapper.ExtFileMetadataMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.FileMetadataRepositoryMapper;
import io.metersphere.project.mapper.FileModuleRepositoryMapper;
import io.metersphere.project.utils.FileDownloadUtils;
import io.metersphere.project.utils.FileMetadataUtils;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.dto.RemoteFileAttachInfo;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRepository;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.util.*;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.service.FileService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private BaseUserMapper baseUserMapper;

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
        FileMetadataUtils.transformRequestFileType(request);
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
                dto.setModuleName(Translator.get("file.module.default.name"));
            } else {
                dto.setModuleName(moduleNameMap.get(dto.getModuleId()));
            }
        }
    }

    private void initModuleName(FileInformationResponse dto) {
        if (StringUtils.equals(dto.getModuleId(), ModuleConstants.DEFAULT_NODE_ID)) {
            dto.setModuleName(Translator.get("file.module.default.name"));
        } else {
            dto.setModuleName(fileModuleService.getModuleName(dto.getModuleId()));
        }
    }

    private void checkEnableFile(String fileType) {
        if (!StringUtils.equalsIgnoreCase(fileType, JAR_FILE_PREFIX)) {
            throw new MSException(Translator.get("file.not.jar"));
        }
    }

    private void parseAndSetFileNameType(String filePath, @NotNull FileMetadata fileMetadata) {
        String fileName = TempFileUtils.getFileNameByPath(filePath);
        if (FileMetadataUtils.isUnknownFile(fileName)) {
            fileMetadata.setOriginalName(fileName);
            fileMetadata.setName(fileName);
            fileMetadata.setType(StringUtils.EMPTY);
        } else {
            //采用这种判断方式，可以避免将隐藏文件的后缀名作为文件类型
            fileMetadata.setOriginalName(fileName);
            fileMetadata.setName(StringUtils.substring(fileName, 0, fileName.lastIndexOf(".")));
            fileMetadata.setType(StringUtils.substring(fileName, fileName.lastIndexOf(".") + 1));
        }
    }

    public FileMetadata genFileMetadata(String fileSpecifyName, String filePath, String storage, long size, boolean enable, String projectId, String moduleId, String operator) {
        FileMetadata fileMetadata = new FileMetadata();
        this.parseAndSetFileNameType(filePath, fileMetadata);
        //如果开启了开关，检查是否是jar文件
        if (enable) {
            this.checkEnableFile(fileMetadata.getType());
        }
        // 指定了文件名称，则替换原文件名
        if (StringUtils.isNotBlank(fileSpecifyName)) {
            fileMetadata.setName(fileSpecifyName);
        }
        //检查处理后的用户名合法性
        if (StringUtils.equals(storage, StorageType.MINIO.name())) {
            this.checkMinIOFileName(null, fileMetadata.getName(), fileMetadata.getType(), projectId);
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
        fileMetadata.setEnable(enable);
        return fileMetadata;
    }

    public String upload(FileUploadRequest request, String operator, MultipartFile uploadFile) throws Exception {
        //检查模块的合法性
        fileManagementService.checkModule(request.getModuleId(), ModuleConstants.NODE_TYPE_DEFAULT);

        String fileName = StringUtils.trim(uploadFile.getOriginalFilename());

        FileMetadata fileMetadata = this.genFileMetadata(null, fileName, StorageType.MINIO.name(), uploadFile.getSize(), request.isEnable(), request.getProjectId(), request.getModuleId(), operator);

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
     * @param originFileName  原始文件名
     * @param projectId 项目ID
     * @param operator  操作人
     * @param fileBytes 文件字节
     * @return
     * @throws Exception
     */
    public String transferFile(String fileName, String originFileName, String projectId, String moduleId, String operator, byte[] fileBytes) {
        if (StringUtils.isBlank(originFileName)) {
            throw new MSException(Translator.get("file.name.cannot.be.empty"));
        }
        FileMetadata fileMetadata = this.genFileMetadata(fileName, originFileName, StorageType.MINIO.name(), fileBytes.length, false, projectId, moduleId, operator);
        if (StringUtils.isNotBlank(fileName)) {
            fileMetadata.setName(fileName);
        }
        FileRequest uploadFileRequest = new FileRequest();
        uploadFileRequest.setFileName(fileMetadata.getId());
        uploadFileRequest.setFolder(this.generateMinIOFilePath(projectId));
        uploadFileRequest.setStorage(StorageType.MINIO.name());

        FileRepository minio = CommonBeanFactory.getBean(MinioRepository.class);
        String filePath;
        try {
            filePath = minio.saveFile(fileBytes, uploadFileRequest);
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(Translator.get("file.transfer.failed"), e);
        }
        fileMetadata.setPath(filePath);
        fileMetadata.setFileVersion(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);
        return fileMetadata.getId();
    }

    private void checkMinIOFileName(String id, String fileName, String type, String projectId) {
        if (StringUtils.isBlank(fileName)) {
            throw new MSException(Translator.get("file.name.cannot.be.empty"));
        }
        FileMetadataExample example = new FileMetadataExample();
        if (StringUtils.isBlank(id)) {
            example.createCriteria().andNameEqualTo(fileName).andTypeEqualTo(type).andLatestEqualTo(true).andProjectIdEqualTo(projectId).andStorageEqualTo(StorageType.MINIO.name());
        } else {
            example.createCriteria().andNameEqualTo(fileName).andTypeEqualTo(type).andLatestEqualTo(true).andProjectIdEqualTo(projectId).andIdNotEqualTo(id).andStorageEqualTo(StorageType.MINIO.name());
        }
        if (fileMetadataMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("file.name.exist") + ":" + fileName);
        }
    }

    private String uploadFile(FileMetadata fileMetadata, MultipartFile file) throws Exception {
        String filePath;
        FileRequest uploadFileRequest = new FileRequest();
        try (InputStream inputStream = file.getInputStream()) {
            uploadFileRequest.setFileName(fileMetadata.getId());
            uploadFileRequest.setFolder(this.generateMinIOFilePath(fileMetadata.getProjectId()));
            uploadFileRequest.setStorage(StorageType.MINIO.name());
            filePath = fileService.upload(inputStream, uploadFileRequest);
        }
        if (TempFileUtils.isImage(fileMetadata.getType())) {
            try (InputStream inputStream = file.getInputStream()) {
                //图片文件自动生成预览图
                byte[] previewImg = TempFileUtils.compressPic(inputStream);
                if (previewImg.length > 0) {
                    uploadFileRequest.setFolder(DefaultRepositoryDir.getFileManagementPreviewDir(fileMetadata.getProjectId()));
                    fileService.upload(previewImg, uploadFileRequest);
                }
            }
        }
        return filePath;
    }

    public File getTmpFile(FileMetadata fileMetadata) {
        File file = null;
        if (TempFileUtils.isImgTmpFileExists(fileMetadata.getId())) {
            file = new File(TempFileUtils.getTmpFilePath(fileMetadata.getId()));
        } else {
            try {
                String filePath = TempFileUtils.createFile(TempFileUtils.getTmpFilePath(fileMetadata.getId()), fileManagementService.getFile(fileMetadata));
                file = new File(filePath);
            } catch (Exception ignore) {
            }
        }
        return file;
    }

    public byte[] getFileByte(FileMetadata fileMetadata) {
        String filePath = null;
            try {
                filePath = TempFileUtils.createFile(TempFileUtils.getTmpFilePath(fileMetadata.getId()), fileManagementService.getFile(fileMetadata));
            } catch (Exception ignore) {
            }
        return TempFileUtils.getFile(filePath);
    }

    public ResponseEntity<byte[]> downloadById(String id) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(id);
        byte[] bytes = this.getFileByte(fileMetadata);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + FileMetadataUtils.getFileName(fileMetadata) + "\"")
                .body(bytes);
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
                this.checkMinIOFileName(request.getId(), request.getName(), fileMetadata.getType(), fileMetadata.getProjectId());
                updateExample.setName(request.getName());
            }
            if (request.getTags() != null) {
                updateExample.setTags(request.getTags());
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

    public void batchDownload(FileBatchProcessRequest request, HttpServletResponse httpServletResponse) {
        List<FileMetadata> fileMetadataList = fileManagementService.getProcessList(request);
        this.checkDownloadSize(fileMetadataList);
        this.batchDownloadWithResponse(fileMetadataList, httpServletResponse);
    }

    public void batchDownloadWithResponse(List<FileMetadata> fileMetadataList, HttpServletResponse response) {
        Map<String, File> fileMap = new HashMap<>();
        fileMetadataList.forEach(fileMetadata -> fileMap.put(FileMetadataUtils.getFileNameWithId(fileMetadata), this.getTmpFile(fileMetadata)));
        FileDownloadUtils.zipFilesWithResponse(fileMap, response);
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

    /**
     * 重新上传
     */
    public String reUpload(FileReUploadRequest request, String operator, MultipartFile uploadFile) throws Exception {
        //检查模块的合法性
        FileMetadata oldFile = fileMetadataMapper.selectByPrimaryKey(request.getFileId());
        if (oldFile == null) {
            throw new MSException(Translator.get("old.file.not.exist"));
        }
        //非minio类型文件不允许重新上传
        if (!StringUtils.equals(oldFile.getStorage(), StorageType.MINIO.name())) {
            throw new MSException(Translator.get("file.not.exist"));
        }
        //旧文件latest修改
        this.setFileVersionIsOld(oldFile, operator);

        FileMetadata fileMetadata = new FileMetadata();
        // 解析新上传的文件名和文件类型
        String uploadFilePath = StringUtils.trim(uploadFile.getOriginalFilename());
        this.parseAndSetFileNameType(uploadFilePath, fileMetadata);
        //部分文件信息内容要和旧版本的信息内容保持一致
        this.genNewFileVersionByOldFile(oldFile, fileMetadata, operator);
        // 存储文件
        String filePath = this.uploadFile(fileMetadata, uploadFile);
        fileMetadata.setSize(uploadFile.getSize());
        fileMetadata.setPath(filePath);
        fileMetadata.setFileVersion(fileMetadata.getId());
        //数据入库
        fileMetadataMapper.insert(fileMetadata);
        //记录日志
        fileMetadataLogService.saveReUploadLog(fileMetadata, operator);

        return fileMetadata.getId();
    }

    //获取模块统计
    public Map<String, Long> moduleCount(FileMetadataTableRequest request, String operator) {
        //查出每个模块节点下的资源数量。 不需要按照模块进行筛选
        FileManagementQuery pageDTO = new FileManagementQuery(request);
        pageDTO.setModuleIds(null);
        List<ModuleCountDTO> moduleCountDTOList = extFileMetadataMapper.countModuleIdByKeywordAndFileType(pageDTO);
        Map<String, Long> moduleCountMap = fileModuleService.getModuleCountMap(request.getProjectId(), pageDTO.getStorage(), moduleCountDTOList);

        //查出全部文件和我的文件的数量
        FileManagementQuery myFileCountDTO = new FileManagementQuery();
        myFileCountDTO.setProjectId(request.getProjectId());
        myFileCountDTO.setStorage(null);
        myFileCountDTO.setHiddenIds(pageDTO.getHiddenIds());
        long allCount = extFileMetadataMapper.fileCount(myFileCountDTO);
        //查找git文件数量
        myFileCountDTO.setStorage(StorageType.GIT.name());
        long gitAllCount = extFileMetadataMapper.fileCount(myFileCountDTO);
        myFileCountDTO.setStorage(null);
        myFileCountDTO.setOperator(operator);
        long myFileCount = extFileMetadataMapper.fileCount(myFileCountDTO);
        moduleCountMap.put(ModuleConstants.MODULE_COUNT_MY, myFileCount);
        moduleCountMap.put(ModuleConstants.MODULE_COUNT_ALL, allCount);
        moduleCountMap.put(ModuleConstants.MODULE_COUNT_GIT, gitAllCount);
        moduleCountMap.put(ModuleConstants.MODULE_COUNT_MINIO, allCount - gitAllCount);
        return moduleCountMap;
    }

    public ResponseEntity<byte[]> downloadPreviewImgById(String id) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(id);
        byte[] bytes = new byte[]{};

        MediaType contentType = MediaType.parseMediaType("application/octet-stream");
        if (TempFileUtils.isImage(fileMetadata.getType())) {
            if (StringUtils.equalsIgnoreCase(fileMetadata.getType(), "svg")) {
                //svg图片不压缩
                contentType = MediaType.parseMediaType("image/svg+xml");
                bytes = this.getFileByte(fileMetadata);
            } else {
                /**
                 * 从minio中获取临时文件
                 * 如果minio不存在，压缩后上传到minio中，并缓存到文件目录中
                 */
                bytes = fileManagementService.getPreviewImg(fileMetadata);
            }
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + FileMetadataUtils.getFileName(fileMetadata) + "\"")
                .body(bytes);
    }

    public List<String> getFileType(String projectId, String storage) {
        List<String> fileTypes = extFileMetadataMapper.selectFileTypeByProjectId(projectId, storage);
        FileMetadataUtils.transformEmptyFileType(fileTypes);
        return fileTypes;
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
        //通过refId批量更新
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andRefIdEqualTo(oldFile.getRefId()).andLatestEqualTo(true);
        FileMetadata updateModel = new FileMetadata();
        updateModel.setLatest(false);
        fileMetadataMapper.updateByExampleSelective(updateModel, example);
    }

    private void genNewFileVersionByOldFile(FileMetadata oldFile, FileMetadata fileMetadata, String operator) {
        long operationTime = System.currentTimeMillis();
        fileMetadata.setId(IDGenerator.nextStr());
        fileMetadata.setStorage(oldFile.getStorage());
        fileMetadata.setProjectId(oldFile.getProjectId());
        fileMetadata.setModuleId(oldFile.getModuleId());
        fileMetadata.setName(oldFile.getName());
        fileMetadata.setOriginalName(oldFile.getOriginalName());
        fileMetadata.setCreateTime(oldFile.getCreateTime());
        fileMetadata.setCreateUser(oldFile.getCreateUser());
        fileMetadata.setUpdateTime(operationTime);
        fileMetadata.setPath(oldFile.getPath());
        fileMetadata.setUpdateUser(operator);
        fileMetadata.setRefId(oldFile.getRefId());
        fileMetadata.setEnable(oldFile.getEnable());
        fileMetadata.setLatest(true);
    }

    public String pullFile(String paramFileId, String operator) {
        FileMetadata oldFile = extFileMetadataMapper.selectLatestById(paramFileId);
        String returnFileId = oldFile.getId();
        if (StringUtils.equals(oldFile.getStorage(), StorageType.GIT.name())) {
            FileMetadataRepository metadataRepository = fileMetadataRepositoryMapper.selectByPrimaryKey(oldFile.getId());
            FileModuleRepository moduleRepository = fileModuleRepositoryMapper.selectByPrimaryKey(oldFile.getModuleId());
            if (metadataRepository != null && moduleRepository != null) {

                GitRepositoryUtil repositoryUtils = new GitRepositoryUtil(moduleRepository.getUrl(), moduleRepository.getUserName(), moduleRepository.getToken());
                RemoteFileAttachInfo gitFileAttachInfo = repositoryUtils.selectLastCommitIdByBranch(metadataRepository.getBranch(), oldFile.getPath());

                if (!StringUtils.equals(gitFileAttachInfo.getCommitId(), metadataRepository.getCommitId())) {
                    this.setFileVersionIsOld(oldFile, operator);
                    FileMetadata fileMetadata = new FileMetadata();
                    this.genNewFileVersionByOldFile(oldFile, fileMetadata, operator);
                    fileMetadata.setType(oldFile.getType());
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
            return fileMetadataList.getFirst();
        } else {
            throw new MSException(Translator.get("latest.file.not.exist"));
        }
    }

    public List<FileVersionResponse> getFileVersion(String fileId) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        if (fileMetadata == null) {
            throw new MSException(Translator.get("file.not.exist"));
        }

        //获取fileMetadata以及可能存在的fileMetadataRepository
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andRefIdEqualTo(fileMetadata.getRefId());
        example.setOrderByClause(" create_time DESC ");
        List<FileMetadata> fileMetadataList = fileMetadataMapper.selectByExample(example);
        List<String> fileIdList = fileMetadataList.stream().map(FileMetadata::getId).toList();
        FileMetadataRepositoryExample repositoryExample = new FileMetadataRepositoryExample();
        repositoryExample.createCriteria().andFileMetadataIdIn(fileIdList);
        List<FileMetadataRepository> fileMetadataRepositoryList = fileMetadataRepositoryMapper.selectByExampleWithBLOBs(repositoryExample);
        Map<String, FileMetadataRepository> fileIdMap = fileMetadataRepositoryList.stream().collect(Collectors.toMap(FileMetadataRepository::getFileMetadataId, Function.identity()));

        //用户ID-用户名的映射
        Map<String, String> userNameMap = new HashMap<>();

        List<FileVersionResponse> fileVersionResponseList = new ArrayList<>();
        fileMetadataList.forEach(item -> {

            FileVersionResponse fileVersionResponse = new FileVersionResponse();
            String userName = userNameMap.get(item.getCreateUser());
            if (userName == null) {
                userName = baseUserMapper.selectNameById(item.getCreateUser());
                userNameMap.put(item.getCreateUser(), userName);
            }

            fileVersionResponse.setId(item.getId());
            fileVersionResponse.setFileVersion(item.getFileVersion());
            fileVersionResponse.setOperator(userName);
            fileVersionResponse.setOperateTime(item.getUpdateTime());

            FileMetadataRepository fileRepository = fileIdMap.get(item.getId());
            if (fileRepository == null) {
                fileVersionResponse.setUpdateHistory(Translator.get("file.log.upload_file"));
            } else {
                fileVersionResponse.setUpdateHistory(fileRepository.getCommitMessage());
            }
            fileVersionResponseList.add(fileVersionResponse);
        });
        return fileVersionResponseList;
    }

    private String generateMinIOFilePath(String projectId) {
        return DefaultRepositoryDir.getFileManagementDir(projectId);
    }

    public List<FileMetadata> getByFileIds(List<String> tempFileIds) {
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(tempFileIds);
        return fileMetadataMapper.selectByExample(example);
    }
}