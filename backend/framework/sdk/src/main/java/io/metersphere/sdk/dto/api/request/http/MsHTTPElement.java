package io.metersphere.sdk.dto.api.request.http;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsHTTPElement extends AbstractMsTestElement {
    private String domain;
}