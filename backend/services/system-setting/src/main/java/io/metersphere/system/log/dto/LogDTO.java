package io.metersphere.system.log.dto;

import io.metersphere.sdk.domain.OperationLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LogDTO extends OperationLog {
    @Schema(description =  "变更前内容")
    private byte[] originalValue;

    @Schema(description =  "变更后内容")
    private byte[] modifiedValue;

    @Schema(description =  "是否需要历史记录")
    private Boolean history = false;

    public LogDTO() {
    }
    public LogDTO(String projectId, String organizationId, String sourceId, String createUser, String type, String module, String content) {
        this.setProjectId(projectId);
        this.setOrganizationId(organizationId);
        this.setSourceId(sourceId);
        this.setCreateUser(createUser);
        this.setType(type);
        this.setModule(module);
        this.setContent(content);
        this.setCreateTime(System.currentTimeMillis());
    }
}