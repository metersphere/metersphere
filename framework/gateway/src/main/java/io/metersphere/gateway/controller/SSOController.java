package io.metersphere.gateway.controller;

import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.SessionConstants;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.gateway.service.SSOService;
import io.metersphere.log.annotation.MsAuditLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;

import javax.annotation.Resource;
import java.util.Locale;

@Controller
@RequestMapping("sso")
public class SSOController {
    @Resource
    private SSOService ssoService;

    @GetMapping("callback/{authId}")
    @MsAuditLog(module = OperLogModule.AUTH_TITLE, type = OperLogConstants.LOGIN, title = "登录")
    public Rendering callbackWithAuthId(@RequestParam("code") String code, @PathVariable("authId") String authId, WebSession session, Locale locale) throws Exception {
        ssoService.exchangeToken(code, authId, session, locale);
        return Rendering.redirectTo("/?_token=" + CodingUtil.base64Encoding(session.getId()))
                .build();
    }

    @GetMapping("callback")
    @MsAuditLog(module = OperLogModule.AUTH_TITLE, type = OperLogConstants.LOGIN, title = "登录")
    public Rendering callback(@RequestParam("code") String code, @RequestParam("state") String authId, WebSession session, Locale locale) throws Exception {
        ssoService.exchangeToken(code, authId, session, locale);
        return Rendering.redirectTo("/?_token=" + CodingUtil.base64Encoding(session.getId()))
                .build();
    }

    @GetMapping("/callback/cas/{authId}")
    @MsAuditLog(module = OperLogModule.AUTH_TITLE, type = OperLogConstants.LOGIN, title = "登录")
    public Rendering casCallback(@RequestParam("ticket") String ticket, @PathVariable("authId") String authId, WebSession session, Locale locale) throws Exception {
        ssoService.serviceValidate(ticket, authId, session, locale);
        return Rendering.redirectTo("/?_token=" + CodingUtil.base64Encoding(session.getId()))
                .build();
    }


    /**
     * oidc 登出 callback
     */
    @PostMapping("/callback/logout")
    public void logoutCallback(@RequestParam("logout_token") String logoutToken) {
        ssoService.kickOutUser(logoutToken);
    }

    /**
     * cas 登出 callback
     */
    @PostMapping("/callback/cas/logout")
    public void logoutCasCallback(@RequestParam("logoutRequest") String logoutRequest) {
        ssoService.kickOutCasUser(logoutRequest);
    }
}
