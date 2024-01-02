package io.metersphere.sdk.dto.api.notice;

import io.metersphere.sdk.constants.ApiExecuteResourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiNoticeDTO implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private ApiExecuteResourceType resourceType;
    private String resourceId;
    private String reportStatus;
    private String userId;
    private String projectId;
    private List<String> environmentIds;
    private String reportId;
}
