package io.metersphere.api.dto.request.http.body;

import io.metersphere.api.dto.ApiFile;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:11
 */
@Data
public class FormDataKV extends WWWFormKV {

    private List<ApiFile> files;

    public boolean isFile() {
        return StringUtils.equalsIgnoreCase(getParamType(), WWWFormParamType.FILE.name());
    }
}
