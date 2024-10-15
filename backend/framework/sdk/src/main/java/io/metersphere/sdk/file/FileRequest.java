package io.metersphere.sdk.file;

import io.metersphere.sdk.dto.FileMetadataRepositoryDTO;
import io.metersphere.sdk.dto.FileModuleRepositoryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileRequest {

    private String folder;

    // 存储类型
    private String storage;

    // 文件名称
    private String fileName;

    //Git文件信息
    private GitFileRequest gitFileRequest;

    public void setGitFileRequest(FileModuleRepositoryDTO repository, FileMetadataRepositoryDTO file) {
        gitFileRequest = new GitFileRequest(repository.getUrl(), repository.getToken(), repository.getUserName(), file.getBranch(), file.getCommitId());
    }

    public FileRequest(String folder, String storage, String fileName) {
        this.folder = folder;
        this.storage = storage;
        this.fileName = fileName;
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
