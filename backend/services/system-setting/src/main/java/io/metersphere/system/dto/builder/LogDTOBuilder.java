package io.metersphere.system.dto.builder;

import io.metersphere.system.log.dto.LogDTO;
import lombok.Builder;

@Builder
public class LogDTOBuilder {
    private String projectId;
    private String organizationId;
    private String sourceId;
    private String createUser;
    private String type;
    private String method;
    private String module;
    private String content;
    private String path;
    private byte[] originalValue;
    private byte[] modifiedValue;

    public LogDTO getLogDTO() {
        LogDTO logDTO = new LogDTO(projectId, organizationId, sourceId, createUser, type, module, content);
        logDTO.setPath(path);
        logDTO.setMethod(method);
        logDTO.setOriginalValue(originalValue);
        logDTO.setModifiedValue(modifiedValue);
        return logDTO;
    }
}
