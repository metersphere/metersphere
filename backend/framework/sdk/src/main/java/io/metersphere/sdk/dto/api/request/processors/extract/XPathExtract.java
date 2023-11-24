package io.metersphere.sdk.dto.api.request.processors.extract;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * XPath提取
 */
@Data
@JsonTypeName("X_PATH")
public class XPathExtract extends MsExtract {
    private String responseFormat;
}