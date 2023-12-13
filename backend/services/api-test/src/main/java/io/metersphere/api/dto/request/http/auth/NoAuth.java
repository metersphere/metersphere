package io.metersphere.api.dto.request.http.auth;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  11:00
 */
@Data
@JsonTypeName("NONE")
public class NoAuth extends HTTPAuth {
}
