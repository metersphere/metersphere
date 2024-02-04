package io.metersphere.sdk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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

    public boolean fileIsNotExist() {
        return StringUtils.isAnyBlank(branch, commitId);
    }
}
