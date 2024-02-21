package io.metersphere.api.dto.request.http.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  18:25
 */

// 避免空的bean，序列化报错
@JsonIgnoreProperties(value = {"handler","hibernateLazyInitializer","fieldHandler"})
@Data
public class NoneBody {
}
