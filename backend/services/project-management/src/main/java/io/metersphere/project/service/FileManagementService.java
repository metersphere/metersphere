package io.metersphere.project.service;

import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.FileMetadataExample;
import io.metersphere.project.domain.FileModuleExample;
import io.metersphere.project.mapper.ExtFileMetadataMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.FileModuleMapper;
import io.metersphere.project.request.filemanagement.FileBatchProcessDTO;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.service.FileService;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileManagementService {
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileModuleMapper fileModuleMapper;
    @Resource
    private FileService fileService;
    @Resource
    private FileMetadataLogService fileMetadataLogService;

    @Resource
    private ExtFileMetadataMapper extFileMetadataMapper;


    public void checkModule(String moduleId, String nodeTypeDefault) {
        if (!StringUtils.equals(moduleId, ModuleConstants.DEFAULT_NODE_ID)) {
            FileModuleExample example = new FileModuleExample();
            example.createCriteria().andIdEqualTo(moduleId).andModuleTypeEqualTo(nodeTypeDefault);
            if (fileModuleMapper.countByExample(example) == 0) {
                throw new MSException("file_module.not.exist");
            }
        }
    }

    public void delete(FileBatchProcessDTO request, String operator) {
        List<FileMetadata> deleteList = this.getDeleteList(request);
        List<String> deleteIds = deleteList.stream().map(FileMetadata::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andIdIn(deleteIds);
            fileMetadataMapper.deleteByExample(example);

            //记录日志
            fileMetadataLogService.saveDeleteLog(deleteList, request.getProjectId(), operator);

            deleteList.forEach(fileMetadata -> {
                FileRequest fileRequest = new FileRequest();
                fileRequest.setFileName(fileMetadata.getId());
                fileRequest.setProjectId(fileMetadata.getProjectId());
                fileRequest.setStorage(fileMetadata.getStorage());
                try {
                    fileService.deleteFile(fileRequest);
                } catch (Exception e) {
                    LogUtils.error("删除文件失败", e);
                }
            });
        }
    }

    public List<FileMetadata> getDeleteList(FileBatchProcessDTO request) {
        List<String> processIds = request.getSelectIds();
        List<FileMetadata> refFileList = new ArrayList<>();
        if (request.isSelectAll()) {
            refFileList = extFileMetadataMapper.selectByKeywordAndFileType(request.getProjectId(), request.getKeyword(), request.getModuleIds(), request.getFileTypes(), true);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                refFileList = refFileList.stream().filter(fileMetadata -> !request.getExcludeIds().contains(fileMetadata.getId())).collect(Collectors.toList());
            }
        } else if (CollectionUtils.isNotEmpty(processIds)) {
            refFileList = extFileMetadataMapper.selectRefIdByIds(processIds);
        }

        List<String> refIdList = refFileList.stream().map(FileMetadata::getRefId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(refIdList)) {
            processIds = extFileMetadataMapper.selectIdByRefIdList(refIdList);
            return extFileMetadataMapper.selectDeleteFileInfoByIds(processIds);
        } else {
            return new ArrayList<>();
        }
    }

    public List<FileMetadata> getProcessList(FileBatchProcessDTO request) {
        List<String> processIds = request.getSelectIds();
        List<FileMetadata> refFileList = new ArrayList<>();
        if (request.isSelectAll()) {
            refFileList = extFileMetadataMapper.selectByKeywordAndFileType(request.getProjectId(), request.getKeyword(), request.getModuleIds(), request.getFileTypes(), false);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                refFileList = refFileList.stream().filter(fileMetadata -> !request.getExcludeIds().contains(fileMetadata.getId())).collect(Collectors.toList());
            }
        } else if (CollectionUtils.isNotEmpty(processIds)) {
            refFileList = extFileMetadataMapper.selectRefIdByIds(processIds);
        }

        return refFileList;
    }

    public void deleteByModuleIds(List<String> deleteModuleIds) {
        //获取要删除的文件引用ID
        List<FileMetadata> refFileList = extFileMetadataMapper.selectRefIdByModuleIds(deleteModuleIds);
        List<String> refIdList = refFileList.stream().map(FileMetadata::getRefId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(refIdList)) {
            //获取要删除的所有文件ID
            List<FileMetadata> deleteList = extFileMetadataMapper.selectDeleteFileInfoByRefIdList(refIdList);
            if (CollectionUtils.isNotEmpty(deleteList)) {
                FileMetadataExample example = new FileMetadataExample();
                example.createCriteria().andIdIn(
                        deleteList.stream().map(FileMetadata::getId).collect(Collectors.toList()));
                fileMetadataMapper.deleteByExample(example);

                deleteList.forEach(fileMetadata -> {
                    FileRequest fileRequest = new FileRequest();
                    fileRequest.setFileName(fileMetadata.getId());
                    fileRequest.setProjectId(fileMetadata.getProjectId());
                    fileRequest.setStorage(fileMetadata.getStorage());
                    try {
                        fileService.deleteFile(fileRequest);
                    } catch (Exception e) {
                        LogUtils.error("删除文件失败", e);
                    }
                });
            }
        }
    }
}
