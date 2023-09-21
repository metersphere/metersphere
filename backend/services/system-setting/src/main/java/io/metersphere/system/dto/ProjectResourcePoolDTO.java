package io.metersphere.system.dto;

import io.metersphere.system.domain.TestResourcePool;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectResourcePoolDTO extends TestResourcePool implements Serializable {
    @Schema(description =  "项目ID")
    private String projectId;
    @Schema(description =  "资源池ID")
    private String id;

    @Schema(description =  "资源池名称")
    private String name;

    private static final long serialVersionUID = 1L;
}
