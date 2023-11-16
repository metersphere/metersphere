package io.metersphere.system.file;

import io.metersphere.project.domain.FileMetadataRepository;
import io.metersphere.project.domain.FileModuleRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class FileRequest {

    private String folder;

    //项目ID
    private String projectId;

    // 存储类型
    private String storage;

    // 资源id为空时存储在项目目录下
    private String resourceId;

    // 文件名称
    private String fileName;

    //Git文件信息
    private GitFileRequest gitFileRequest;

    public void setGitFileRequest(FileModuleRepository repository, FileMetadataRepository file) {
        gitFileRequest = new GitFileRequest(repository.getUrl(), repository.getToken(), repository.getUserName(), file.getBranch(), file.getCommitId());
    }
}

@Data
@AllArgsConstructor
class GitFileRequest {
    private String url;
    private String token;
    private String userName;
    private String branch;
    private String commitId;
}
