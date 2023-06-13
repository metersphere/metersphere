package io.metersphere.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("/")
    public String index() {
        return "index.html";
    }


    @GetMapping(value = "/login")
    public String login() {
        return "index.html";
    }
}
