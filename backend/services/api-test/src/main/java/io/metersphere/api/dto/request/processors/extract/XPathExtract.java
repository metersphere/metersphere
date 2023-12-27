package io.metersphere.api.dto.request.processors.extract;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * XPath提取
 */
@Data
@JsonTypeName("X_PATH")
public class XPathExtract extends ResultMatchingExtract {
    private String responseFormat;

    public enum ResponseFormat {
        /**
         * XML
         */
        XML,
        /**
         * HTML
         */
        HTML
    }
}