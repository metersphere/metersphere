package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.QueryResourcePoolRequest;
import io.metersphere.sdk.dto.TestResourcePoolDTO;
import io.metersphere.sdk.dto.TestResourcePoolRequest;
import io.metersphere.sdk.dto.TestResourcePoolReturnDTO;
import io.metersphere.sdk.log.annotation.Log;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.service.TestResourcePoolService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.PageUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.sdk.util.SessionUtils;
import io.metersphere.system.consul.CacheNode;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统资源池")
@RequestMapping("/test/resource/pool")
@RestController
public class TestResourcePoolController {

    @Resource
    private TestResourcePoolService testResourcePoolService;

    @PostMapping("/add")
    @Operation(summary = "添加资源池")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = TestResourcePoolService.class)
    public TestResourcePool addTestResourcePool(@Validated({Created.class}) @RequestBody TestResourcePoolRequest request) {
        String userId = SessionUtils.getUserId();
        TestResourcePoolDTO testResourcePool = new TestResourcePoolDTO();
        BeanUtils.copyBean(testResourcePool, request);
        testResourcePool.setCreateUser(userId);
        testResourcePool.setCreateTime(System.currentTimeMillis());
        return testResourcePoolService.addTestResourcePool(testResourcePool);
    }

    @GetMapping("/delete/{poolId}")
    @CacheNode // 把监控节点缓存起来
    @Operation(summary = "删除资源池")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#testResourcePoolId)", msClass = TestResourcePoolService.class)
    public void deleteTestResourcePool(@PathVariable(value = "poolId") String testResourcePoolId) {
        testResourcePoolService.deleteTestResourcePool(testResourcePoolId);
    }

    @PostMapping("/update")
    @CacheNode // 把监控节点缓存起来
    @Operation(summary = "更新资源池")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request.getId())", msClass = TestResourcePoolService.class)
    public void updateTestResourcePool(@Validated({Updated.class}) @RequestBody TestResourcePoolRequest request) {
        TestResourcePoolDTO testResourcePool = new TestResourcePoolDTO();
        BeanUtils.copyBean(testResourcePool, request);
        testResourcePool.setCreateUser(null);
        testResourcePool.setCreateTime(null);
        testResourcePoolService.updateTestResourcePool(testResourcePool);
    }

    @PostMapping("/page")
    @Operation(summary = "获取资源池列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ)
    public Pager<List<TestResourcePoolDTO>> listResourcePools(@Validated @RequestBody QueryResourcePoolRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        return PageUtils.setPageInfo(page, testResourcePoolService.listResourcePools(request));
    }

    @GetMapping("/detail/{poolId}")
    @Operation(summary = "查看资源池详细")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ)
    public TestResourcePoolReturnDTO getTestResourcePoolDetail(@PathVariable(value = "poolId") String testResourcePoolId) {
        return testResourcePoolService.getTestResourcePoolDetail(testResourcePoolId);
    }

    @PostMapping("/set/enable/{poolId}")
    @Operation(summary = "资源池禁用")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#testResourcePoolId)", msClass = TestResourcePoolService.class)
    public void unableTestResourcePool(@PathVariable(value = "poolId") String testResourcePoolId) {
        testResourcePoolService.unableTestResourcePool(testResourcePoolId);
    }


}
