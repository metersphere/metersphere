package io.metersphere.api.dto.automation;

import io.metersphere.base.domain.FileMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiScenarioExportJmxDTO {
    private String name;
    private String id;
    private String jmx;
    private Integer version;

    //性能测试引用场景时需要场景下的附件
    private List<FileMetadata> fileMetadataList;

    public ApiScenarioExportJmxDTO(String name, String jmx) {
        this.name = name;
        this.jmx = jmx;
    }
}
