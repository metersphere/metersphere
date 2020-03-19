package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.service.TestResourcePoolService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("testresourcepool")
@RestController
public class TestResourcePoolController {

    @Resource
    private TestResourcePoolService testResourcePoolService;

    @PostMapping("/add")
    public TestResourcePool addTestResourcePool(@RequestBody TestResourcePool testResourcePool) {
        return testResourcePoolService.addTestResourcePool(testResourcePool);
    }

    @GetMapping("/delete/{testResourcePoolId}")
    public void deleteTestResourcePool(@PathVariable(value = "testResourcePoolId") String testResourcePoolId) {
        testResourcePoolService.deleteTestResourcePool(testResourcePoolId);
    }

    @PostMapping("/update")
    public void updateTestResourcePool(@RequestBody TestResourcePool testResourcePool) {
        testResourcePoolService.updateTestResourcePool(testResourcePool);
    }

    @PostMapping("list/{goPage}/{pageSize}")
    public Pager<List<TestResourcePool>> listResourcePools(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryResourcePoolRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testResourcePoolService.listResourcePools(request));
    }
}
