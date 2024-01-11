package io.metersphere.system.utils;

import io.metersphere.system.log.constants.OperationLogType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
public class CheckLogModel {
    private String resourceId;
    private OperationLogType operationType;
    private String url;


    public CheckLogModel(String resourceId, OperationLogType operationType, String url) {
        this.resourceId = resourceId;
        this.operationType = operationType;
        this.url = getLogUrl(url);
    }

    private String getLogUrl(String url) {
        if (StringUtils.endsWith(url, "/%s")) {
            return StringUtils.substring(url, 0, url.length() - 3);
        } else {
            return url;
        }
    }
}