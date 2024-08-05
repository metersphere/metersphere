package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiCaseBatchSyncRequest extends ApiTestCaseBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "同步项")
    private ApiCaseSyncItemRequest syncItems = new ApiCaseSyncItemRequest();
    @Schema(description = "是否删除多余参数", defaultValue = "false")
    private boolean deleteRedundantParam = false;
    @Schema(description = "通知配置")
    private ApiCaseSyncNotificationRequest notificationConfig = new ApiCaseSyncNotificationRequest();

    public class ApiCaseSyncItemRequest {
        @Schema(description = "请求头", defaultValue = "true")
        private Boolean header = true;
        @Schema(description = "请求体", defaultValue = "true")
        private Boolean body = true;
        @Schema(description = "Query参数", defaultValue = "true")
        private Boolean query = true;
        @Schema(description = "Rest参数", defaultValue = "true")
        private Boolean rest = true;
    }

    public class ApiCaseSyncNotificationRequest {
        @Schema(description = "是否通知接口创建人", defaultValue = "true")
        private Boolean apiCreator = true;
        @Schema(description = "是否通知引用该用例的场景创建人", defaultValue = "true")
        private Boolean scenarioCreator = true;
    }
}
