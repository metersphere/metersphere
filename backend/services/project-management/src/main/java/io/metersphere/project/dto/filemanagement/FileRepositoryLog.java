package io.metersphere.project.dto.filemanagement;

import io.metersphere.project.domain.FileModule;
import io.metersphere.project.domain.FileModuleRepository;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileRepositoryLog {
    private String id;

    private String projectId;

    private String name;

    private String parentId;

    private Long createTime;

    private Long updateTime;

    private Long pos;

    private String updateUser;

    private String createUser;

    private String moduleType;

    private String platform;

    private String url;

    private String token;

    private String userName;

    public FileRepositoryLog(FileModule module, FileModuleRepository repository) {
        this.id = module.getId();
        this.projectId = module.getProjectId();
        this.name = module.getName();
        this.parentId = module.getParentId();
        this.createTime = module.getCreateTime();
        this.updateTime = module.getUpdateTime();
        this.pos = module.getPos();
        this.updateUser = module.getUpdateUser();
        this.createUser = module.getCreateUser();
        this.moduleType = module.getModuleType();
        this.platform = repository.getPlatform();
        this.url = repository.getUrl();
        this.token = repository.getToken();
        this.userName = repository.getUserName();
    }
}
