package io.metersphere.project.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.FileMetadataExample;
import io.metersphere.project.dto.FileInformationDTO;
import io.metersphere.project.dto.FileTableResult;
import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.mapper.ExtFileMetadataMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.request.filemanagement.*;
import io.metersphere.project.utils.FileDownloadUtils;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.service.FileService;
import io.metersphere.sdk.util.*;
import io.metersphere.system.uid.UUID;
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
    private FileService fileService;

    @Value("${metersphere.file.batch-download-max:600MB}")
    private DataSize maxFileSize;

    public List<FileInformationDTO> list(FileMetadataTableRequest request) {
        List<FileInformationDTO> returnList = new ArrayList<>();
        List<FileMetadata> fileMetadataList = extFileMetadataMapper.selectByKeywordAndFileType(request.getProjectId(), request.getKeyword(), request.getModuleIds(), request.getFileTypes(), false);
        fileMetadataList.forEach(fileMetadata -> {
            FileInformationDTO fileInformationDTO = new FileInformationDTO(fileMetadata);
            if (FilePreviewUtils.isImage(fileMetadata.getType())) {
                fileInformationDTO.setPreviewSrc(FilePreviewUtils.catchFileIfNotExists(fileMetadata.getPath() + "." + fileMetadata.getType(), this.getFile(fileMetadata)));
            }
            returnList.add(fileInformationDTO);
        });
        return returnList;
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

        fileMetadata.setId(UUID.randomUUID().toString());
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

        long operationTime = System.currentTimeMillis();
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setStorage(oldFile.getStorage());
        fileMetadata.setProjectId(oldFile.getProjectId());
        fileMetadata.setModuleId(oldFile.getModuleId());
        fileMetadata.setName(oldFile.getName());
        fileMetadata.setType(oldFile.getType());
        fileMetadata.setCreateTime(operationTime);
        fileMetadata.setCreateUser(operator);
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
        return null;
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

    public FileTableResult page(FileMetadataTableRequest request) {
        FileTableResult dto = new FileTableResult();
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "update_time desc");
        dto.setTableData(PageUtils.setPageInfo(page, this.list(request)));

        //获取模块统计
        List<ModuleCountDTO> moduleCountDTOList = extFileMetadataMapper.countModuleIdByKeywordAndFileType(request.getProjectId(), request.getKeyword(), request.getModuleIds(), request.getFileTypes());
        Map<String, Long> moduleCountMap = moduleCountDTOList.stream().collect(Collectors.toMap(ModuleCountDTO::getModuleId, ModuleCountDTO::getDataCount));
        dto.setModuleCount(moduleCountMap);
        return dto;
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
}
