package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.TestResourcePool;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.service.TestResourcePoolService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import java.util.List;

@RequestMapping("testresourcepool")
@RestController
@RequiresRoles(RoleConstants.ADMIN)
public class TestResourcePoolController {

    @Resource
    private TestResourcePoolService testResourcePoolService;

    @PostMapping("/add")
    public TestResourcePoolDTO addTestResourcePool(@RequestBody TestResourcePoolDTO testResourcePoolDTO) {
        return testResourcePoolService.addTestResourcePool(testResourcePoolDTO);
    }

    @GetMapping("/delete/{testResourcePoolId}")
    public void deleteTestResourcePool(@PathVariable(value = "testResourcePoolId") String testResourcePoolId) {
        testResourcePoolService.deleteTestResourcePool(testResourcePoolId);
    }

    @PostMapping("/update")
    public void updateTestResourcePool(@RequestBody TestResourcePoolDTO testResourcePoolDTO) {
        testResourcePoolService.updateTestResourcePool(testResourcePoolDTO);
    }

    @GetMapping("/update/{poolId}/{status}")
    public void updateTestResourcePoolStatus(@PathVariable String poolId, @PathVariable String status) {
        testResourcePoolService.updateTestResourcePoolStatus(poolId, status);
    }

    @PostMapping("list/{goPage}/{pageSize}")
    public Pager<List<TestResourcePoolDTO>> listResourcePools(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryResourcePoolRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testResourcePoolService.listResourcePools(request));
    }

    @GetMapping("list/all/valid")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<TestResourcePool> listValidResourcePools() {
        return testResourcePoolService.listValidResourcePools();
    }

    @GetMapping("list/quota/valid")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
    public List<TestResourcePool> listValidQuotaResourcePools() {
        return testResourcePoolService.listValidQuotaResourcePools();
    }


}
