package io.metersphere.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShareReportController {

    @GetMapping(value = "/sharePlanReport")
    public String shareRedirect() {
        return "share-plan-report.html";
    }
}
