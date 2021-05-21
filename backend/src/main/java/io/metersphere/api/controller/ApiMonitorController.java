package io.metersphere.api.controller;

import io.metersphere.api.dto.ApiMonitorRequest;
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
    @PostMapping("/getResponseTime")
    public List<ApiResponseTimeMonitor> responseTimeData(@RequestBody ApiMonitorRequest request) {
        return apiMonitorService.getApiResponseTimeData(request.getUrl(), request.getStartTime(), request.getEndTime());
    }

    /**
     * 查询状态码
     */
    @PostMapping("/getResponseCode")
    public List<ApiResponseCodeMonitor> responseCodeData(@RequestBody ApiMonitorRequest request) {
        return apiMonitorService.getApiResponseCodeData(request.getUrl(), request.getStartTime(), request.getEndTime());
    }

    /**
     * 查询reportId
     */
    @PostMapping("/getReportId")
    public String searchReportId(@RequestBody ApiMonitorRequest request) {
        return apiMonitorService.getReportId(request.getUrl(), request.getStartTime());
    }
}
