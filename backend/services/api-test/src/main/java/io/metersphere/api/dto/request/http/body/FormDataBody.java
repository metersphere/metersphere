package io.metersphere.api.dto.request.http.body;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  16:59
 */
@Data
public class FormDataBody {
    /**
     * form-data 请求体的键值对列表
     */
    private List<FormDataKV> formValues = new ArrayList<>();
}
