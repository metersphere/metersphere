package io.metersphere.track.issue.domain.zentao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserResponse {
    private String status;
    private User user;
    private String reason;

    @Getter
    @Setter
    public static class User {
        private String id;
//        private String dept;
        private String account;
//        private String type;
//        private String role;
//        private String realname;
//        private String nickname;
//        private String commiter;
//        private String avatar;
//        private String birthday;
//        private String gender;
//        private String email;
//        private String skype;
//        private String qq;
//        private String mobile;
//        private String phone;
//        private String weixin;
//        private String dingding;
//        private String slack;
//        private String whatsapp;
//        private String address;
//        private String zipcode;
//        private String join;
//        private String visits;
//        private String ip;
//        private String last;
//        private String fails;
//        private String ranzhi;
//        private String score;
//        private String scoreLevel;
//        private String clientStatus;
//        private String clientLang;
//        private String lastTime;
//        private boolean admin;
//        private boolean modifyPassword;
//        private String company;
//        private String token;
    }
}
