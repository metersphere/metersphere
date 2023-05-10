package io.metersphere.reportstatistics.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.EnterpriseTestReport;
import io.metersphere.base.domain.EnterpriseTestReportWithBLOBs;
import io.metersphere.base.domain.Schedule;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.reportstatistics.dto.request.EnterpriseTestReportRequest;
import io.metersphere.reportstatistics.dto.response.EnterpriseTestReportDTO;
import io.metersphere.reportstatistics.dto.response.UserGroupResponse;
import io.metersphere.reportstatistics.service.EnterpriseTestReportService;
import io.metersphere.request.ScheduleRequest;
import io.metersphere.service.BaseScheduleService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/enterprise/test/report")
public class EnterpriseTestReportController {

    @Resource
    private EnterpriseTestReportService enterpriseTestReportService;
    @Resource
    private BaseScheduleService scheduleService;

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<EnterpriseTestReportDTO>> list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody EnterpriseTestReportRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<EnterpriseTestReport> returnList = enterpriseTestReportService.list(request);
        List<EnterpriseTestReportDTO> returnDTO = enterpriseTestReportService.parseDTO(returnList);
        return PageUtils.setPageInfo(page, returnDTO);
    }

    @GetMapping("/get/{id}")
    public EnterpriseTestReportWithBLOBs get(@PathVariable String id) {
        return enterpriseTestReportService.get(id);
    }


    @PostMapping(value = "/create")
    @MsAuditLog(module = OperLogModule.ENTERPRISE_TEST_REPORT, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request.id)", msClass = EnterpriseTestReportService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_ENTERPRISE_REPORT_CREATE, PermissionConstants.PROJECT_API_SCENARIO_READ_COPY}, logical = Logical.OR)
    public EnterpriseTestReport create(@RequestBody EnterpriseTestReportWithBLOBs request) {
        return enterpriseTestReportService.save(request);
    }

    @PostMapping(value = "/update")
    @MsAuditLog(module = OperLogModule.ENTERPRISE_TEST_REPORT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = EnterpriseTestReportService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_ENTERPRISE_REPORT_EDIT}, logical = Logical.OR)
    public EnterpriseTestReport update(@RequestBody EnterpriseTestReportWithBLOBs request) {
        return enterpriseTestReportService.update(request);
    }

    @PostMapping(value = "/send")
    @MsAuditLog(module = OperLogModule.ENTERPRISE_TEST_REPORT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = EnterpriseTestReportService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_ENTERPRISE_REPORT_EDIT}, logical = Logical.OR)
    public EnterpriseTestReport send(@RequestBody EnterpriseTestReportWithBLOBs request) {
        return enterpriseTestReportService.send(request);
    }

    @PostMapping(value = "/delete")
    @MsAuditLog(module = OperLogModule.ENTERPRISE_TEST_REPORT, project = "#request.projectId", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#request)", msClass = EnterpriseTestReportService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_ENTERPRISE_REPORT_DELETE}, logical = Logical.OR)
    public void delete(@RequestBody EnterpriseTestReportRequest request) {
        enterpriseTestReportService.deleteByRequest(request);
    }

    @PostMapping(value = "/copy")
    @MsAuditLog(module = OperLogModule.ENTERPRISE_TEST_REPORT, type = OperLogConstants.COPY, title = "#request.name", content = "#msClass.getLogDetails(#request.id)", msClass = EnterpriseTestReportService.class)
    @RequiresPermissions(value = {PermissionConstants.PROJECT_ENTERPRISE_REPORT_COPY}, logical = Logical.OR)
    public void copy(@RequestBody EnterpriseTestReportRequest request) {
        enterpriseTestReportService.copy(request);
    }

    @GetMapping("/initUserGroupInfo/{projectId}")
    public List<UserGroupResponse> initUserGroupInfo(@PathVariable String projectId) {
        return enterpriseTestReportService.initUserGroupInfo(projectId);
    }

    @PostMapping(value = "/schedule/update")
    @MsRequestLog(module = OperLogModule.ENTERPRISE_TEST_REPORT)
    public void updateSchedule(@RequestBody Schedule request) {
        enterpriseTestReportService.updateSchedule(request);
    }

    @PostMapping(value = "/schedule/create")
    @MsRequestLog(module = OperLogModule.ENTERPRISE_TEST_REPORT)
    public void createSchedule(@RequestBody ScheduleRequest request) {
        enterpriseTestReportService.createSchedule(request);
    }

    @GetMapping("/schedule/findOne/{testId}/{group}")
    public Schedule schedule(@PathVariable String testId, @PathVariable String group) {
        Schedule schedule = scheduleService.getScheduleByResource(testId, group);
        return schedule;
    }
}
