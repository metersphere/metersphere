package io.metersphere.controller.request;

import lombok.Data;

@Data
public class GroupRequest {
    private String resourceId;
    private String type;
}
