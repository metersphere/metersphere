package io.metersphere.metadata.vo;

import lombok.Data;

@Data
public class FileResponse {
    private String name;

    // 类型默认就是后缀
    private String type;

    private byte[] bytes;
}
