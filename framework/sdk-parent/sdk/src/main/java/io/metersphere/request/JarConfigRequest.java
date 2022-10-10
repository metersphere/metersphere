package io.metersphere.request;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class JarConfigRequest extends BaseQueryRequest {
    private String id;
    private String resourceId;
    private String resourceType;
}
