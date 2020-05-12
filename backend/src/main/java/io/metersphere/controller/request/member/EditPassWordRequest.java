package io.metersphere.controller.request.member;

import lombok.Data;
@Data
public class EditPassWordRequest {
        private String password;
        private String newpassword;
        private String id;
}
