package io.metersphere.api.dto.mockserver;

import io.metersphere.api.dto.ApiFile;
import lombok.Data;

import java.util.List;

@Data
public class FormKeyValueInfo extends KeyValueInfo {
    /**
     * 参数的文件列表
     * 当 paramType 为 FILE 时，参数值使用该字段
     * 其他类型使用 value字段
     */
    private List<ApiFile> files;
}
