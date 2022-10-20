package io.metersphere.controller;

import io.metersphere.service.SSOLogoutService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("sso")
public class SSOLogoutController {
    @Resource
    private SSOLogoutService ssoLogoutService;

    /**
     * oidc 登出 callback
     */
    @PostMapping("/callback/logout")
    public void logoutCallback(@RequestParam("logout_token") String logoutToken) {
        ssoLogoutService.kickOutUser(logoutToken);
    }


    /**
     * cas 登出 callback
     */
    @PostMapping("/callback/cas/logout")
    public void logoutCasCallback(@RequestParam("logoutRequest") String logoutRequest) {
        ssoLogoutService.kickOutCasUser(logoutRequest);
    }
}
