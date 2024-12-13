package io.metersphere.api.dto.request.http.body;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.metersphere.api.dto.ApiFile;
import jakarta.validation.Valid;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * form-data 请求体的键值对
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:11
 */
@Data
public class FormDataKV extends WWWFormKV {

    /**
     * 参数的文件列表
     * 当 paramType 为 FILE 时，参数值使用该字段
     * 其他类型使用 value字段
     */
    @Valid
    private List<ApiFile> files;
    /**
     * 参数的 contentType
     */
    private String contentType;

    @JsonIgnore
    public boolean isFile() {
        return StringUtils.equalsIgnoreCase(getParamType(), BodyParamType.FILE.getValue());
    }
}
