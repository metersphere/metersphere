package io.metersphere.project.dto.filemanagement.response;

import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.FileMetadataRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class FileInformationResponse {
    @Schema(description = "ID")
    private String id;

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "标签")
    private List<String> tags = new ArrayList<>();

    @Schema(description = "描述")
    private String description;

    @Schema(description = "模块名称")
    private String moduleName;

    @Schema(description = "模块ID")
    private String moduleId;

    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "创建时间")
    private long createTime;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "更新时间")
    private long updateTime;

    @Schema(description = "存储库类型")
    private String storage;

    @Schema(description = "文件大小")
    private long size;

    @Schema(description = "启用/禁用(jar文件)")
    private boolean enable;

    @Schema(description = "关联ID")
    private String refId;

    @Schema(description = "文件版本")
    private String fileVersion;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件分支")
    private String branch;

    @Schema(description = "文件分支")
    private String commitId;

    @Schema(description = "文件分支")
    private String commitMessage;

    public FileInformationResponse(FileMetadata fileMetadata, FileMetadataRepository repositoryFile) {
        if (fileMetadata != null) {
            this.id = fileMetadata.getId();
            this.projectId = fileMetadata.getProjectId();
            this.name = fileMetadata.getName();
            this.fileType = fileMetadata.getType();
            this.description = fileMetadata.getDescription();
            this.originalName = fileMetadata.getOriginalName();
            this.moduleId = fileMetadata.getModuleId();
            this.size = fileMetadata.getSize();
            if (CollectionUtils.isNotEmpty(fileMetadata.getTags())) {
                tags = fileMetadata.getTags();
            }
            this.enable = fileMetadata.getEnable();
            this.createTime = fileMetadata.getCreateTime();
            this.createUser = fileMetadata.getCreateUser();
            this.updateUser = fileMetadata.getUpdateUser();
            this.updateTime = fileMetadata.getUpdateTime();
            this.storage = fileMetadata.getStorage();
            this.refId = fileMetadata.getRefId();
            this.fileVersion = fileMetadata.getFileVersion();
            this.filePath = fileMetadata.getPath();
        }
        if (repositoryFile != null) {
            this.branch = repositoryFile.getBranch();
            this.commitId = repositoryFile.getCommitId();
            this.commitMessage = repositoryFile.getCommitMessage();
        }
    }
}
