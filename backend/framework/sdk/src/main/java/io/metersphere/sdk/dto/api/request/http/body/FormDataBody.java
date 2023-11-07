package io.metersphere.sdk.dto.api.request.http.body;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  16:59
 */
@Data
@JsonTypeName("FORM_DATA")
public class FormDataBody extends Body {
    private List<FormDataKV> fromValues;
}
