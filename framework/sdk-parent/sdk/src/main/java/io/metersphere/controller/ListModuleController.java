package io.metersphere.controller;


import io.metersphere.dto.Module;
import io.metersphere.service.ListModuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("module")
public class ListModuleController {
    @Resource
    private ListModuleService listModuleService;

    @GetMapping("list")
    public List<Module> listModules() {
        return listModuleService.listModules();
    }

}
