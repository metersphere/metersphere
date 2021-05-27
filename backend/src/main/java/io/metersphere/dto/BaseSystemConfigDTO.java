package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseSystemConfigDTO {
    private String url;
    private String concurrency;
    private String prometheusHost;
}
