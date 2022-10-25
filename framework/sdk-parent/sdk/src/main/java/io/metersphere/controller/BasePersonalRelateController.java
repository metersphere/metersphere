package io.metersphere.controller;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.service.MicroService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/personal/relate")
public class BasePersonalRelateController {

    @Resource
    private MicroService microService;

    @PostMapping("/issues/user/auth")
    public void userAuth(@RequestBody Object obj) {
        microService.postForData(MicroServiceName.TEST_TRACK, "/issues/user/auth", obj);
    }
}
