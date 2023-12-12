package io.metersphere.bug.service;

import io.metersphere.bug.domain.BugLocalAttachment;
import io.metersphere.bug.domain.BugLocalAttachmentExample;
import io.metersphere.bug.dto.response.BugFileDTO;
import io.metersphere.bug.mapper.BugLocalAttachmentMapper;
import io.metersphere.project.domain.FileAssociation;
import io.metersphere.project.domain.FileAssociationExample;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.FileMetadataExample;
import io.metersphere.project.mapper.FileAssociationMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BugAttachmentService {

    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private BugLocalAttachmentMapper bugLocalAttachmentMapper;

    /**
     * 查询缺陷的附件集合
     * @param bugId 缺陷ID
     * @return 缺陷附件关系集合
     */
    public List<BugFileDTO> getAllBugFiles(String bugId) {
        List<BugFileDTO> bugFiles = new ArrayList<>();
        BugLocalAttachmentExample localAttachmentExample = new BugLocalAttachmentExample();
        localAttachmentExample.createCriteria().andBugIdEqualTo(bugId);
        List<BugLocalAttachment> bugLocalAttachments = bugLocalAttachmentMapper.selectByExample(localAttachmentExample);
        if (!CollectionUtils.isEmpty(bugLocalAttachments)) {
            bugLocalAttachments.forEach(localFile -> {
                BugFileDTO localFileDTO = BugFileDTO.builder().refId(localFile.getId()).fileId(localFile.getFileId()).fileName(localFile.getFileName()).fileType(getLocalFileType(localFile.getFileName()))
                        .fileSize(localFile.getSize()).createTime(localFile.getCreateTime()).createUser(localFile.getCreateUser()).associated(false).build();
                bugFiles.add(localFileDTO);
            });
        }
        FileAssociationExample associationExample = new FileAssociationExample();
        associationExample.createCriteria().andSourceIdEqualTo(bugId).andSourceTypeEqualTo(FileAssociationSourceUtil.SOURCE_TYPE_BUG);
        List<FileAssociation> fileAssociations = fileAssociationMapper.selectByExample(associationExample);
        if (!CollectionUtils.isEmpty(fileAssociations)) {
            List<String> associateFileIds = fileAssociations.stream().map(FileAssociation::getFileId).toList();
            FileMetadataExample metadataExample = new FileMetadataExample();
            metadataExample.createCriteria().andIdIn(associateFileIds);
            List<FileMetadata> fileMetadataList = fileMetadataMapper.selectByExample(metadataExample);
            Map<String, FileMetadata> fileMetadataMap = fileMetadataList.stream().collect(Collectors.toMap(FileMetadata::getId, v -> v));
            fileAssociations.forEach(associatedFile -> {
                FileMetadata associatedFileMetadata = fileMetadataMap.get(associatedFile.getFileId());
                BugFileDTO associatedFileDTO = BugFileDTO.builder().refId(associatedFile.getId()).fileId(associatedFile.getFileId()).fileName(associatedFileMetadata.getName() + "." + associatedFileMetadata.getType())
                        .fileType(associatedFileMetadata.getType()).fileSize(associatedFileMetadata.getSize()).createTime(associatedFileMetadata.getCreateTime())
                        .createUser(associatedFileMetadata.getCreateUser()).associated(true).build();
                bugFiles.add(associatedFileDTO);
            });
        }
        return bugFiles;
    }

    /**
     * 获取本地文件类型
     * @param fileName 文件名
     * @return 文件类型
     */
    private String getLocalFileType(String fileName) {
        int i = fileName.lastIndexOf(".");
        if (i > 0) {
            return fileName.substring(i);
        } else {
            return StringUtils.EMPTY;
        }
    }
}
