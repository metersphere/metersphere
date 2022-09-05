package io.metersphere.metadata.vo;

import io.metersphere.track.dto.TreeNodeDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileModuleVo extends TreeNodeDTO<FileModuleVo> {
    private String path;
    private String moduleType;
    private String repositoryName;
    private String repositoryPath;
    private String repositoryUserName;
    private String repositoryToken;
    private String repositoryDesc;
}
