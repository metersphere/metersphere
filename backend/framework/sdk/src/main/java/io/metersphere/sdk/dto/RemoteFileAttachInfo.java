package io.metersphere.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteFileAttachInfo implements Serializable {
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
