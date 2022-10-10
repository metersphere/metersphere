package io.metersphere.xpack.resourcepool.controller;

import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.xpack.resourcepool.service.ValidQuotaResourcePoolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/testresourcepool")
public class ValidQuotaResourcePoolController {
    @Resource
    private ValidQuotaResourcePoolService validQuotaResourcePoolService;

    @GetMapping("list/quota/valid")
    public List<TestResourcePoolDTO> listValidQuotaResourcePools() {
        return validQuotaResourcePoolService.listValidQuotaResourcePools();
    }
}
