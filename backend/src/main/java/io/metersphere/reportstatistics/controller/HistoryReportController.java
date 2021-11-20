package io.metersphere.reportstatistics.controller;

import com.alibaba.fastjson.JSONArray;
import io.metersphere.base.domain.ReportStatistics;
import io.metersphere.base.domain.ReportStatisticsWithBLOBs;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.reportstatistics.dto.ReportStatisticsSaveRequest;
import io.metersphere.reportstatistics.service.ReportStatisticsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/9/14 2:58 下午
 */
@RestController
@RequestMapping(value = "/history/report")
public class HistoryReportController {

    @Resource
    private ReportStatisticsService reportStatisticsService;

    @PostMapping("/selectByParams")
    public List<ReportStatistics> selectByParams(@RequestBody ReportStatisticsSaveRequest request) {
        List<ReportStatistics> returnList = reportStatisticsService.selectByRequest(request);
        return returnList;
    }

    @PostMapping("/saveReport")
    public ReportStatisticsWithBLOBs saveReport(@RequestBody ReportStatisticsSaveRequest request){
        ReportStatisticsWithBLOBs returnData = reportStatisticsService.saveByRequest(request);
        return returnData;
    }

    @PostMapping("/updateReport")
    public ReportStatisticsWithBLOBs updateReport(@RequestBody ReportStatisticsSaveRequest request){
        ReportStatisticsWithBLOBs returnData = reportStatisticsService.updateByRequest(request);
        return returnData;
    }

    @PostMapping("/deleteByParam")
    public int deleteById(@RequestBody ReportStatisticsSaveRequest request) {
        return reportStatisticsService.deleteById(request.getId());
    }

    @PostMapping("/selectById")
    public ReportStatisticsWithBLOBs selectById(@RequestBody ReportStatisticsSaveRequest request) {
        return reportStatisticsService.selectById(request.getId());
    }
}
