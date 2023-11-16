package io.metersphere.system.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.consul.CacheNode;
import io.metersphere.system.dto.pool.TestResourcePoolDTO;
import io.metersphere.system.dto.pool.TestResourcePoolRequest;
import io.metersphere.system.dto.pool.TestResourcePoolReturnDTO;
import io.metersphere.system.dto.sdk.QueryResourcePoolRequest;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.service.TestResourcePoolService;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统设置-系统-资源池")
@RequestMapping("/test/resource/pool")
@RestController
public class TestResourcePoolController {

    @Resource
    private TestResourcePoolService testResourcePoolService;

    @PostMapping("/update")
    @CacheNode // 把监控节点缓存起来
    @Operation(summary = "系统设置-系统-资源池-更新资源池")
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
    @Operation(summary = "系统设置-系统-资源池-获取资源池列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ)
    public Pager<List<TestResourcePoolDTO>> listResourcePools(@Validated @RequestBody QueryResourcePoolRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(), true);
        return PageUtils.setPageInfo(page, testResourcePoolService.listResourcePools(request));
    }

    @GetMapping("/detail/{poolId}")
    @Operation(summary = "系统-资源池-查看资源池详细")
    @RequiresPermissions(PermissionConstants.SYSTEM_TEST_RESOURCE_POOL_READ)
    public TestResourcePoolReturnDTO getTestResourcePoolDetail(@PathVariable(value = "poolId") String testResourcePoolId) {
        return testResourcePoolService.getTestResourcePoolDetail(testResourcePoolId);
    }




}
