package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.annotation.RequestLog;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.consul.CacheNode;
import io.metersphere.system.dto.TestResourcePoolDTO;
import io.metersphere.system.request.QueryResourcePoolRequest;
import io.metersphere.system.service.TestResourcePoolService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/test/resource/pool")
@RestController
public class TestResourcePoolController {

    @Resource
    private TestResourcePoolService testResourcePoolService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_POOL_READ_ADD)
    @RequestLog(type = OperationLogType.ADD, module = OperationLogModule.SYSTEM_TEST_RESOURCE_POOL, details = "#poolDTO.name")
    public TestResourcePoolDTO addTestResourcePool(@RequestBody TestResourcePoolDTO poolDTO) {
        return testResourcePoolService.addTestResourcePool(poolDTO);
    }

    @GetMapping("/delete/{poolId}")
    @CacheNode // 把监控节点缓存起来
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_POOL_READ_DELETE)
    @RequestLog(isBefore = true, type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_TEST_RESOURCE_POOL,
            details = "#msClass.getLogDetails(#testResourcePoolId)", msClass = TestResourcePoolService.class)
    public void deleteTestResourcePool(@PathVariable(value = "poolId") String testResourcePoolId) {
        testResourcePoolService.deleteTestResourcePool(testResourcePoolId);
    }

    @PostMapping("/update")
    @CacheNode // 把监控节点缓存起来
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_POOL_READ_UPDATE)
    @RequestLog(type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_TEST_RESOURCE_POOL,
            sourceId = "#testResourcePoolDTO.id", details = "#testResourcePoolDTO.name")
    public void updateTestResourcePool(@RequestBody TestResourcePoolDTO testResourcePoolDTO) {
        testResourcePoolService.updateTestResourcePool(testResourcePoolDTO);
    }

    @PostMapping("/page")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_POOL_READ)
    public Pager<List<TestResourcePoolDTO>> listResourcePools(@RequestBody QueryResourcePoolRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        return PageUtils.setPageInfo(page, testResourcePoolService.listResourcePools(request));
    }


}
