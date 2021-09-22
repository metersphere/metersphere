package io.metersphere.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private String name;
    private String type;
    // 表前缀
    private String prefix;
}
