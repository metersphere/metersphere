package io.metersphere.project.dto.filemanagement.response;

import io.metersphere.project.domain.FileModule;
import io.metersphere.project.domain.FileModuleRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileRepositoryResponse {
    @Schema(description = "ID")
    private String id;
    @Schema(description = "存储库名称")
    private String name;

    @Schema(description = "存储库Token")
    private String token;

    @Schema(description = "存储库地址")
    private String url;

    @Schema(description = "所属平台")
    private String platform;

    @Schema(description = "用户名;platform为Gitee时必填")
    private String userName;

    public FileRepositoryResponse(FileModule fileModule, FileModuleRepository repository) {
        this.id = fileModule.getId();
        this.name = fileModule.getName();
        this.token = repository.getToken();
        this.url = repository.getUrl();
        this.platform = repository.getPlatform();
        this.userName = repository.getUserName();
    }

}
