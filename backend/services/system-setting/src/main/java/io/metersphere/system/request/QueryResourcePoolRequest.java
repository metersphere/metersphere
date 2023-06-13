package io.metersphere.system.request;

import io.metersphere.sdk.dto.BasePageRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryResourcePoolRequest extends BasePageRequest {
    private String name;
    private Boolean enable;
}
