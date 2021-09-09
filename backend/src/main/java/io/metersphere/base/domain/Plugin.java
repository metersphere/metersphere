package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class Plugin implements Serializable {
    private String id;

    private String name;

    private String pluginId;

    private String scriptId;

    private String jmeterClazz;

    private String clazzName;

    private String sourcePath;

    private String sourceName;

    private String execEntry;

    private Long createTime;

    private Long updateTime;

    private String createUserId;

    private static final long serialVersionUID = 1L;
}