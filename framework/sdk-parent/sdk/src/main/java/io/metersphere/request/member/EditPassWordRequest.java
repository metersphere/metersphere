package io.metersphere.request.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditPassWordRequest {
    private String password;
    private String newpassword;
    private String id;
}
