package io.metersphere.api.dto.request.http.body;

import lombok.Data;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  16:59
 */
@Data
public class WWWFormBody {
    private List<FormDataKV> fromValues;
}
