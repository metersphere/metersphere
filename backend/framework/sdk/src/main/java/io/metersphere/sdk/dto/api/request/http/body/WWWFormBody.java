package io.metersphere.sdk.dto.api.request.http.body;

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
