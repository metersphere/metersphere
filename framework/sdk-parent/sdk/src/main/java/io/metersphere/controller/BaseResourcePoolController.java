package io.metersphere.controller;

import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.service.BaseTestResourcePoolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/testresourcepool")
public class BaseResourcePoolController {

    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;

    @GetMapping("list/all/valid")
    public List<TestResourcePoolDTO> listValidResourcePools() {
        return baseTestResourcePoolService.listValidResourcePools();
    }
}
