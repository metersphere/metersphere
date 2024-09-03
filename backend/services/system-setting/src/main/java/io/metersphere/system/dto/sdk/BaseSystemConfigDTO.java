package io.metersphere.system.dto.sdk;

import lombok.Data;

@Data
public class BaseSystemConfigDTO {
    private String url;
    private String concurrency;
    private String prometheusHost;
    private String runMode;
    private String docUrl;
    private String fileMaxSize;
}
