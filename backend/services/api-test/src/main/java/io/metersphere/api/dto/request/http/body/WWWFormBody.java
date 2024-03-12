package io.metersphere.api.dto.request.http.body;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * x-www-form-urlencoded 请求体的键值对列表
 * @Author: jianxing
 * @CreateTime: 2023-11-06  16:59
 */
@Data
public class WWWFormBody {
    @Valid
    private List<WWWFormKV> formValues = new ArrayList<>();
}
