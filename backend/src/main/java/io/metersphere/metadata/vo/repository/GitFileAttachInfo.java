package io.metersphere.metadata.vo.repository;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GitFileAttachInfo extends FileAttachInfo {
    private String repositoryPath;
    private String userName;
    private String token;
    private String branch;
    private String commitId;
    private String filePath;
    private String commitMessage;
    private long size;

    public String getRepositoryInfo() {
        return repositoryPath + "-" + userName + "-" + token;
    }
}
