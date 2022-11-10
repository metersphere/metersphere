package io.metersphere.gateway.controller;

import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.gateway.log.annotation.MsAuditLog;
import io.metersphere.gateway.service.SSOService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;

import javax.annotation.Resource;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("sso")
public class SSOController {
    @Resource
    private SSOService ssoService;

    @GetMapping("callback/{authId}")
    @MsAuditLog(module = OperLogModule.AUTH_TITLE, type = OperLogConstants.LOGIN, title = "登录")
    public Rendering callbackWithAuthId(@RequestParam("code") String code, @PathVariable("authId") String authId, WebSession session, Locale locale) throws Exception {
        Optional<SessionUser> sessionUser = ssoService.exchangeToken(code, authId, session, locale);
        return Rendering.redirectTo("/#/?_token=" + CodingUtil.base64Encoding(session.getId()) + "&_csrf=" + sessionUser.get().getCsrfToken())
                .build();
    }

    @GetMapping("callback")
    @MsAuditLog(module = OperLogModule.AUTH_TITLE, type = OperLogConstants.LOGIN, title = "登录")
    public Rendering callback(@RequestParam("code") String code, @RequestParam("state") String authId, WebSession session, Locale locale) throws Exception {
        Optional<SessionUser> sessionUser = ssoService.exchangeToken(code, authId, session, locale);
        return Rendering.redirectTo("/#/?_token=" + CodingUtil.base64Encoding(session.getId()) + "&_csrf=" + sessionUser.get().getCsrfToken())
                .build();
    }

    @GetMapping("callback/oauth")
    @MsAuditLog(module = OperLogModule.AUTH_TITLE, type = OperLogConstants.LOGIN, title = "登录")
    public Rendering callbackOauth(@RequestParam("code") String code, @RequestParam("state") String authId, WebSession session, Locale locale) throws Exception {
        Optional<SessionUser> sessionUser = ssoService.exchangeOauth2Token(code, authId, session, locale);
        return Rendering.redirectTo("/#/?_token=" + CodingUtil.base64Encoding(session.getId()) + "&_csrf=" + sessionUser.get().getCsrfToken())
                .build();
    }

    @GetMapping("/callback/cas/{authId}")
    @MsAuditLog(module = OperLogModule.AUTH_TITLE, type = OperLogConstants.LOGIN, title = "登录")
    public Rendering casCallback(@RequestParam("ticket") String ticket, @PathVariable("authId") String authId, WebSession session, Locale locale) throws Exception {
        Optional<SessionUser> sessionUser = ssoService.serviceValidate(ticket, authId, session, locale);
        return Rendering.redirectTo("/#/?_token=" + CodingUtil.base64Encoding(session.getId()) + "&_csrf=" + sessionUser.get().getCsrfToken())
                .build();
    }


//    /**
//     * oidc 登出 callback
//     */
//    @PostMapping("/callback/logout")
//    public Mono<Void> logoutCallback(@RequestParam("logout_token") String logoutToken) {
//        ssoService.kickOutUser(logoutToken);
//        return Mono.empty();
//    }
//
//    /**
//     * cas 登出 callback
//     */
//    @PostMapping("/callback/cas/logout")
//    public Mono<Void> logoutCasCallback(@RequestParam("logoutRequest") String logoutRequest) {
//        ssoService.kickOutCasUser(logoutRequest);
//        return Mono.empty();
//    }
}
