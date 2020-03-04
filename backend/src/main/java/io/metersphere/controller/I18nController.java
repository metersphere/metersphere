package io.metersphere.controller;


import io.metersphere.commons.constants.I18nConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Lang;
import io.metersphere.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by liqiang on 2019/4/1.
 */
@RestController
public class I18nController {

    private static final int FOR_EVER = 3600 * 24 * 30 * 12 * 10; //10 years in second

    @Value("${run.mode:release}")
    private String runMode;

    @Resource
    private UserService userService;

    @GetMapping("lang/change/{lang}")
    public void changeLang(@PathVariable String lang, HttpServletResponse response) {
        Lang targetLang = Lang.getLangWithoutDefault(lang);
        if (targetLang == null) {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            LogUtil.error("Invalid parameter: " + lang);
            MSException.throwException("ERROR_LANG_INVALID");
        }
        userService.setLanguage(targetLang.getDesc());
        Cookie cookie = new Cookie(I18nConstants.LANG_COOKIE_NAME, targetLang.getDesc());
        cookie.setPath("/");
        cookie.setMaxAge(FOR_EVER);
        response.addCookie(cookie);
        //重新登录
        if ("release".equals(runMode)) {
            Cookie f2cCookie = new Cookie("MS_COOKIE_ID", "deleteMe");
            f2cCookie.setPath("/");
            f2cCookie.setMaxAge(0);
            response.addCookie(f2cCookie);
        }
    }
}
