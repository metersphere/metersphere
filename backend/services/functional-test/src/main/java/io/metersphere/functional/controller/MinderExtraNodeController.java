package io.metersphere.functional.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.metersphere.functional.service.MinderExtraNodeService;


@RestController
@RequestMapping("/minder/extra/node")
public class MinderExtraNodeController {

    @Resource
    private MinderExtraNodeService minderExtraNodeService;

}