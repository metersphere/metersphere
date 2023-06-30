package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.TestResourcePoolDTO;
import io.metersphere.sdk.dto.TestResourcePoolRequest;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.consul.CacheNode;
import io.metersphere.sdk.dto.QueryResourcePoolRequest;
import io.metersphere.sdk.service.TestResourcePoolService;
import io.metersphere.system.domain.TestResourcePool;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/test/resource/pool")
@RestController
public class TestResourcePoolController {

    @Resource
    private TestResourcePoolService testResourcePoolService;

    @PostMapping("/add")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ_ADD)
    @Log(type = OperationLogType.ADD, module = OperationLogModule.SYSTEM_TEST_RESOURCE_POOL, details = "#request.name")
    public TestResourcePool addTestResourcePool(@Validated @RequestBody TestResourcePoolRequest request) {
        String userId = SessionUtils.getUserId();
        TestResourcePoolDTO testResourcePool = new TestResourcePoolDTO();
        BeanUtils.copyBean(testResourcePool, request);
        testResourcePool.setCreateUser(userId);
        testResourcePool.setCreateTime(System.currentTimeMillis());
        return testResourcePoolService.addTestResourcePool(testResourcePool);
    }

    @GetMapping("/delete/{poolId}")
    @CacheNode // 把监控节点缓存起来
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ_DELETE)
    @Log(isBefore = true, type = OperationLogType.DELETE, module = OperationLogModule.SYSTEM_TEST_RESOURCE_POOL,
            details = "#msClass.getLogDetails(#testResourcePoolId)", msClass = TestResourcePoolService.class)
    public void deleteTestResourcePool(@PathVariable(value = "poolId") String testResourcePoolId) {
        testResourcePoolService.deleteTestResourcePool(testResourcePoolId);
    }

    @PostMapping("/update")
    @CacheNode // 把监控节点缓存起来
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, module = OperationLogModule.SYSTEM_TEST_RESOURCE_POOL,
            sourceId = "#request.id", details = "#request.name")
    public void updateTestResourcePool(@Validated @RequestBody TestResourcePoolRequest request) {
        TestResourcePoolDTO testResourcePool = new TestResourcePoolDTO();
        BeanUtils.copyBean(testResourcePool, request);
        testResourcePool.setCreateUser(null);
        testResourcePool.setCreateTime(null);
        testResourcePoolService.updateTestResourcePool(testResourcePool);
    }

    @PostMapping("/page")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ)
    public Pager<List<TestResourcePoolDTO>> listResourcePools(@Validated @RequestBody QueryResourcePoolRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        return PageUtils.setPageInfo(page, testResourcePoolService.listResourcePools(request));
    }

    @GetMapping("/detail/{poolId}")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ)
    public TestResourcePoolDTO getTestResourcePoolDetail(@PathVariable(value = "poolId") String testResourcePoolId) {
        return testResourcePoolService.getTestResourcePoolDetail(testResourcePoolId);
    }


}
