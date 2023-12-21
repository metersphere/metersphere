package io.metersphere.api.dto.request.http.body;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:11
 */
@Data
public class FormDataKV extends WWWFormKV {

    private List<BodyFile> files;

    public boolean isFile() {
        return StringUtils.equalsIgnoreCase(getParamType(), WWWFormParamType.FILE.name());
    }
}
