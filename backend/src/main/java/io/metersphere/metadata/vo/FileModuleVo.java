package io.metersphere.metadata.vo;

import io.metersphere.track.dto.TreeNodeDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileModuleVo extends TreeNodeDTO<FileModuleVo> {
    private String path;
}
