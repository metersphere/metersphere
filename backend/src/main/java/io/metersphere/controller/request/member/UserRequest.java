package io.metersphere.controller.request.member;

import io.metersphere.base.domain.User;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UserRequest extends User {
    /**
     * 弃用
     */
    private List<Map<String, Object>> roles = new ArrayList<>();
    private List<Map<String, Object>> groups = new ArrayList<>();

}
