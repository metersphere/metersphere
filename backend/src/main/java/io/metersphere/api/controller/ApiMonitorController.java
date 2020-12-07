package io.metersphere.api.controller;

import io.metersphere.api.dto.ApiMonitorSearch;
import io.metersphere.api.dto.ApiResponseCodeMonitor;
import io.metersphere.api.dto.ApiResponseTimeMonitor;
import io.metersphere.api.service.APIMonitorService;
import io.metersphere.commons.constants.RoleConstants;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/monitor")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class ApiMonitorController {

    @Resource
    private APIMonitorService apiMonitorService;

    /**
     * 查询所有接口
     */
    @GetMapping("/list")
    public List<ApiMonitorSearch> apiList() {
        return apiMonitorService.list();
    }


    /**
     * 查询响应时间
     */
    @GetMapping("/getResponseTime")
    public List<ApiResponseTimeMonitor> responseTimeData(@RequestHeader("apiUrl") String url, String startTime, String endTime) {
        return apiMonitorService.getApiResponseTimeData(url, startTime, endTime);
    }

    /**
     * 查询状态码
     */
    @GetMapping("/getResponseCode")
    public List<ApiResponseCodeMonitor> responseCodeData(@RequestHeader("apiUrl") String url, String startTime, String endTime) {
        return apiMonitorService.getApiResponseCodeData(url, startTime, endTime);
    }

    /**
     * 查询reportId
     */
    @GetMapping("/getReportId")
    public String searchReportId(@RequestHeader("apiUrl") String url, @RequestParam("startTime") String startTime) {
        return apiMonitorService.getReportId(url, startTime);
    }
}
