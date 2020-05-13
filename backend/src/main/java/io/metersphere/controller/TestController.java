package io.metersphere.controller;

import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test")
public class TestController {

    @GetMapping(value = "/{str}")
    public Object getString(@PathVariable String str) throws InterruptedException {
        if (StringUtils.equals("error", str)) {
            throw new RuntimeException("test error");
        }
        if (StringUtils.equals("warning", str)) {
            return ResultHolder.error("test warning");
        }
        if (StringUtils.equals("user", str)) {
            return ResultHolder.success(SessionUtils.getUser());
        }
        if (StringUtils.equals("sleep", str)) {
            Thread.sleep(2000L);
            return ResultHolder.success(str);
        }
        return ResultHolder.success(str);
    }
}