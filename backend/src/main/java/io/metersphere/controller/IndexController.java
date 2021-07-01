package io.metersphere.controller;

import io.metersphere.commons.constants.SessionConstants;
import io.metersphere.commons.utils.SessionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping
public class IndexController {

    @GetMapping(value = "/")
    public String index() {
        return "index.html";
    }

    @GetMapping(value = "/login")
    public String login(HttpServletResponse response) {
        if (SessionUtils.getUser() == null) {
            response.setHeader(SessionConstants.AUTHENTICATION_STATUS, SessionConstants.AUTHENTICATION_INVALID);
            return "login.html";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping(value = "/document")
    public String document() {
        return "document.html";
    }
}
