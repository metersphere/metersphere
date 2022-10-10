package io.metersphere.metadata.vo;

import io.metersphere.base.domain.FileModule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DragFileModuleRequest extends FileModule {
    List<String> nodeIds;
    FileModuleVo nodeTree;
}
