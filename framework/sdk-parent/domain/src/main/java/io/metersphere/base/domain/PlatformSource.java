package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class PlatformSource implements Serializable {
    private String platform;

    private Boolean enable;

    private Boolean valid;

    private String config;

    private static final long serialVersionUID = 1L;
}