package io.metersphere.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.controller.request.resourcepool.QueryResourcePoolRequest;
import io.metersphere.dto.TestResourcePoolDTO;
import io.metersphere.dto.UpdatePoolDTO;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.TestResourcePoolService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("testresourcepool")
@RestController

public class TestResourcePoolController {

    @Resource
    private TestResourcePoolService testResourcePoolService;

    @PostMapping("/add")
    @MsAuditLog(module = "system_test_resource", type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#testResourcePoolDTO.id)", msClass = TestResourcePoolService.class)
    public TestResourcePoolDTO addTestResourcePool(@RequestBody TestResourcePoolDTO testResourcePoolDTO) {
        return testResourcePoolService.addTestResourcePool(testResourcePoolDTO);
    }

    @GetMapping("/delete/{testResourcePoolId}")
    @MsAuditLog(module = "system_test_resource", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#testResourcePoolId)",  msClass = TestResourcePoolService.class)
    public void deleteTestResourcePool(@PathVariable(value = "testResourcePoolId") String testResourcePoolId) {
        testResourcePoolService.deleteTestResourcePool(testResourcePoolId);
    }

    @PostMapping("/update")
    @MsAuditLog(module = "system_test_resource", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#testResourcePoolDTO.id)", content = "#msClass.getLogDetails(#testResourcePoolDTO.id)", msClass = TestResourcePoolService.class)
    public void updateTestResourcePool(@RequestBody TestResourcePoolDTO testResourcePoolDTO) {
        testResourcePoolService.updateTestResourcePool(testResourcePoolDTO);
    }

    @GetMapping("/update/{poolId}/{status}")
    @MsAuditLog(module = "system_test_resource", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#poolId)", content = "#msClass.getLogDetails(#poolId)", msClass = TestResourcePoolService.class)
    public void updateTestResourcePoolStatus(@PathVariable String poolId, @PathVariable String status) {
        testResourcePoolService.updateTestResourcePoolStatus(poolId, status);
    }

    @GetMapping("/check/use/{poolId}")
    public UpdatePoolDTO checkHaveTestUsePool(@PathVariable String poolId) {
        return testResourcePoolService.checkHaveTestUsePool(poolId);
    }

    @PostMapping("list/{goPage}/{pageSize}")
    public Pager<List<TestResourcePoolDTO>> listResourcePools(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody QueryResourcePoolRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, testResourcePoolService.listResourcePools(request));
    }

    @GetMapping("list/all/valid")
    public List<TestResourcePoolDTO> listValidResourcePools() {
        return testResourcePoolService.listValidResourcePools();
    }

    @GetMapping("list/quota/valid")
    public List<TestResourcePoolDTO> listValidQuotaResourcePools() {
        return testResourcePoolService.listValidQuotaResourcePools();
    }


}
