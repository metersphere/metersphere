package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiCaseBatchSyncRequest extends ApiTestCaseBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "同步项")
    private ApiCaseSyncItemRequest syncItems = new ApiCaseSyncItemRequest();
    @Schema(description = "是否删除多余参数", defaultValue = "false")
    private Boolean deleteRedundantParam = false;
    @Schema(description = "通知配置")
    private ApiCaseSyncNotificationRequest notificationConfig = new ApiCaseSyncNotificationRequest();

    @Data
    public static class ApiCaseSyncNotificationRequest {
        @Schema(description = "是否通知接口用例创建人", defaultValue = "true")
        private Boolean apiCaseCreator = true;
        @Schema(description = "是否通知引用该用例的场景创建人", defaultValue = "true")
        private Boolean scenarioCreator = true;
    }
}
