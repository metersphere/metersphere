package io.metersphere.dto;

import lombok.Data;

@Data
public class ProjectJarConfig {

    private String id;
    private String name;
    private long updateTime;
    // 是否已有文件
    private boolean hasFile;
    // 文件存储类型
    private String storage;
    private String attachInfo;
}
