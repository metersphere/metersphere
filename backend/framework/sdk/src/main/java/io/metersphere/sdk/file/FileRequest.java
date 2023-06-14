package io.metersphere.sdk.file;

import lombok.Data;

@Data
public class FileRequest {
    // 项目id
    private String projectId;

    // 存储类型
    private String storage;

    // 资源id为空时存储在项目目录下
    private String resourceId;

    // 文件名称
    private String fileName;
}
