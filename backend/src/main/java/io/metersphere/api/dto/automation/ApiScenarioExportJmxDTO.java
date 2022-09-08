package io.metersphere.api.dto.automation;

import io.metersphere.base.domain.FileMetadata;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ApiScenarioExportJmxDTO {
    private String name;
    private String id;
    private String jmx;
    private Integer version;

    //性能测试引用场景时需要场景下的附件
    private List<FileMetadata> fileMetadataList;
    //项目-环境id
    private Map<String, List<String>> projectEnvMap;

    public ApiScenarioExportJmxDTO(String name, String jmx) {
        this.name = StringUtils.replace(name, "/", "");
        this.jmx = jmx;
    }

}
