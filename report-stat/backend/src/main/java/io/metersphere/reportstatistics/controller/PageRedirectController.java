package io.metersphere.reportstatistics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageRedirectController {
    @GetMapping(value = "/chart-pic")
    public String getChart() {
        return "share-enterprise-report.html";
    }
}
