package io.metersphere.system.dto.sdk;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseSystemConfigDTO {
    private String url;
    private String concurrency;
    private String prometheusHost;
    private String runMode;
    private String docUrl;
}
