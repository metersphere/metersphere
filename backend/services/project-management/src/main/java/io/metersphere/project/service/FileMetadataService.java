package io.metersphere.project.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.FileMetadataExample;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.filemanagement.FileInformationDTO;
import io.metersphere.project.dto.filemanagement.FileManagementPageDTO;
import io.metersphere.project.mapper.ExtFileMetadataMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.request.filemanagement.*;
import io.metersphere.project.utils.FileDownloadUtils;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.TempFileUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.uid.IDGenerator;
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
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileMetadataService {
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private ExtFileMetadataMapper extFileMetadataMapper;
    @Resource
    private FileMetadataLogService fileMetadataLogService;
    @Resource
    private FileManagementService fileManagementService;
    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private FileService fileService;

    @Value("${metersphere.file.batch-download-max:600MB}")
    private DataSize maxFileSize;

    public FileInformationDTO get(String id) {
        FileMetadata fileMetadata = extFileMetadataMapper.getById(id);
        FileInformationDTO dto = new FileInformationDTO(fileMetadata);
        initModuleName(dto);
        return dto;
    }

    public List<FileInformationDTO> list(FileMetadataTableRequest request) {
        List<FileInformationDTO> returnList = new ArrayList<>();
        FileManagementPageDTO pageDTO = new FileManagementPageDTO(request);
        List<FileMetadata> fileMetadataList = extFileMetadataMapper.selectByKeywordAndFileType(pageDTO);
        fileMetadataList.forEach(fileMetadata -> {
            FileInformationDTO fileInformationDTO = new FileInformationDTO(fileMetadata);
            returnList.add(fileInformationDTO);
        });
        this.initModuleName(returnList);
        return returnList;
    }

    private void initModuleName(List<FileInformationDTO> returnList) {
        List<String> moduleIds = returnList.stream().map(FileInformationDTO::getModuleId).distinct().collect(Collectors.toList());
        Map<String, String> moduleNameMap = fileModuleService.getModuleNameMapByIds(moduleIds);
        for (FileInformationDTO dto : returnList) {
            if (StringUtils.equals(dto.getModuleId(), ModuleConstants.DEFAULT_NODE_ID)) {
                dto.setModuleName(Translator.get("default.module"));
            } else {
                dto.setModuleName(moduleNameMap.get(dto.getModuleId()));
            }
        }
    }

    private void initModuleName(FileInformationDTO dto) {
        if (StringUtils.equals(dto.getModuleId(), ModuleConstants.DEFAULT_NODE_ID)) {
            dto.setModuleName(Translator.get("default.module"));
        } else {
            dto.setModuleName(fileModuleService.getModuleName(dto.getModuleId()));
        }
    }

    public String upload(FileUploadRequest request, String operator, MultipartFile uploadFile) throws Exception {
        //检查模块的合法性
        fileManagementService.checkModule(request.getModuleId(), ModuleConstants.NODE_TYPE_DEFAULT);

        String fileName = StringUtils.trim(uploadFile.getOriginalFilename());

        FileMetadata fileMetadata = new FileMetadata();
        if (StringUtils.contains(fileName, ".")) {
            fileMetadata.setName(StringUtils.substring(fileName, 0, fileName.lastIndexOf(".")));
            fileMetadata.setType(StringUtils.substring(fileName, fileName.lastIndexOf(".") + 1));
        } else {
            fileMetadata.setName(fileName);
            fileMetadata.setType(StringUtils.EMPTY);
        }
        //检查处理后的用户名合法性
        this.checkFileName(null, fileMetadata.getName(), request.getProjectId());

        fileMetadata.setId(IDGenerator.nextStr());
        fileMetadata.setStorage(StorageType.MINIO.name());
        fileMetadata.setProjectId(request.getProjectId());
        fileMetadata.setModuleId(request.getModuleId());
        long operationTime = System.currentTimeMillis();
        fileMetadata.setCreateTime(operationTime);
        fileMetadata.setCreateUser(operator);
        fileMetadata.setUpdateTime(operationTime);
        fileMetadata.setUpdateUser(operator);
        fileMetadata.setSize(uploadFile.getSize());
        fileMetadata.setLatest(true);
        fileMetadata.setRefId(fileMetadata.getId());
        fileMetadataMapper.insert(fileMetadata);

        //记录日志
        fileMetadataLogService.saveUploadLog(fileMetadata, operator);

        // 上传文件
        String filePath = this.uploadFile(fileMetadata, uploadFile);
        FileMetadata updateFileMetadata = new FileMetadata();
        updateFileMetadata.setId(fileMetadata.getId());
        updateFileMetadata.setPath(filePath);
        updateFileMetadata.setFileVersion(fileMetadata.getId());
        fileMetadataMapper.updateByPrimaryKeySelective(updateFileMetadata);

        return fileMetadata.getId();
    }

    private void checkFileName(String id, String fileName, String projectId) {
        if (StringUtils.isBlank(fileName)) {
            throw new MSException(Translator.get("file.name.cannot.be.empty"));
        }
        FileMetadataExample example = new FileMetadataExample();
        if (StringUtils.isBlank(id)) {
            example.createCriteria().andNameEqualTo(fileName).andProjectIdEqualTo(projectId);
        } else {
            example.createCriteria().andNameEqualTo(fileName).andProjectIdEqualTo(projectId).andIdNotEqualTo(id);
        }
        if (fileMetadataMapper.countByExample(example) > 0) {
            throw new MSException(Translator.get("file.name.exist"));
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

    public ResponseEntity<byte[]> downloadById(String id) {

        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(id);
        byte[] bytes = this.getFile(fileMetadata);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + this.getFileName(fileMetadata.getName(), fileMetadata.getType()) + "\"")
                .body(bytes);
    }

    private String getFileName(String fileName, String type) {
        if (StringUtils.isBlank(type)) {
            return fileName;
        }
        return fileName + "." + type;
    }

    private byte[] getFile(FileMetadata fileMetadata) {
        if (fileMetadata == null) {
            throw new MSException(Translator.get("file.not.exist"));
        }
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileName(fileMetadata.getId());
        fileRequest.setProjectId(fileMetadata.getProjectId());
        fileRequest.setStorage(fileMetadata.getStorage());
        try {
            return fileService.download(fileRequest);
        } catch (Exception e) {
            LogUtils.error("获取文件失败", e);
        }
        return new byte[0];
    }

    public void update(FileUpdateRequest request, String operator) {
        //检查模块的合法性
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(request.getId());
        if (fileMetadata == null) {
            throw new MSException(Translator.get("file.not.exist"));
        }

        //检查是否是空参数
        if (!StringUtils.isAllBlank(request.getName(), request.getDescription(), request.getModuleId()) && CollectionUtils.isNotEmpty(request.getTags())) {
            FileMetadata updateExample = new FileMetadata();
            updateExample.setId(request.getId());
            updateExample.setDescription(request.getDescription());
            updateExample.setModuleId(request.getModuleId());
            if (StringUtils.isNotBlank(request.getName())) {
                this.checkFileName(request.getId(), request.getName(), fileMetadata.getProjectId());
                updateExample.setName(request.getName());
            }
            if (CollectionUtils.isNotEmpty(request.getTags())) {
                updateExample.setTags(JSON.toJSONString(request.getTags()));
            }
            updateExample.setUpdateUser(operator);
            updateExample.setUpdateTime(System.currentTimeMillis());
            fileMetadataMapper.updateByPrimaryKeySelective(updateExample);
            //记录日志
            fileMetadataLogService.saveUpdateLog(fileMetadata, fileMetadata.getProjectId(), operator);
        }
    }

    public Pager<List<FileInformationDTO>> page(FileMetadataTableRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "update_time desc");
        return PageUtils.setPageInfo(page, this.list(request));
    }

    public ResponseEntity<byte[]> batchDownload(FileBatchProcessDTO request) {
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
            byte[] bytes = this.getFile(fileMetadata);
            if (bytes != null) {
                files.put(this.getFileName(fileMetadata.getName(), fileMetadata.getType()), bytes);
            }
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
            throw new MSException(Translator.get("file.size.is.zero"));
        }

        DataSize dataSize = DataSize.ofBytes(fileSize.get());
        if (maxFileSize.compareTo(dataSize) < 0) {
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
        oldFile.setLatest(false);
        fileMetadataMapper.updateByPrimaryKeySelective(oldFile);

        //删除旧的预览文件
        TempFileUtils.deleteTmpFile(oldFile.getId());

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
        fileMetadata.setUpdateUser(operator);
        fileMetadata.setSize(uploadFile.getSize());
        fileMetadata.setRefId(oldFile.getRefId());
        fileMetadata.setLatest(true);
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
        FileManagementPageDTO pageDTO = new FileManagementPageDTO(request);
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

    public ResponseEntity<byte[]> downloadPreviewImgById(String id) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(id);
        String previewImgPath = null;
        if (TempFileUtils.isImage(fileMetadata.getType())) {
            if (TempFileUtils.isImgFileExists(fileMetadata.getId())) {
                previewImgPath = TempFileUtils.getImgFileTmpPath(fileMetadata.getId());
            } else {
                previewImgPath = TempFileUtils.catchCompressImgIfNotExists(fileMetadata.getId(), this.getFile(fileMetadata));
            }
        }

        byte[] bytes;
        if (StringUtils.isNotBlank(previewImgPath)) {
            bytes = TempFileUtils.getPreviewFile(previewImgPath);
        } else {
            bytes = new byte[]{};
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + this.getFileName(fileMetadata.getName(), fileMetadata.getType()) + "\"")
                .body(bytes);
    }

    public List<String> getFileType(String projectId) {
        return extFileMetadataMapper.selectFileTypeByProjectId(projectId);
    }
}
