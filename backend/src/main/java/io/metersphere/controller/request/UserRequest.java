package io.metersphere.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRequest {
    private String id;
    private String name;
    private String email;

    boolean selectAll;
    List<String> unSelectIds;
}
