package io.metersphere.api.dto.automation;

import io.metersphere.base.domain.FileMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ApiScenrioExportJmx {
    private String name;
    private String jmx;
    private Integer version;
    List<String> files;

    //性能测试引用场景时需要场景下的附件
    private List<FileMetadata> fileMetadataList;

    public ApiScenrioExportJmx(String name, String jmx) {
        this.name = name;
        this.jmx = jmx;
    }
}
