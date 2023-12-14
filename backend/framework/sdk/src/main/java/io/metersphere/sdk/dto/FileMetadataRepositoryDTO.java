package io.metersphere.sdk.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FileMetadataRepositoryDTO implements Serializable {
    private String fileMetadataId;
    private String branch;
    private String commitId;

    private String commitMessage;

    @Serial
    private static final long serialVersionUID = 1L;
}