package io.metersphere.reportstatistics.service;

import io.metersphere.base.domain.ReportStatistics;
import io.metersphere.base.domain.ReportStatisticsExample;
import io.metersphere.base.domain.ReportStatisticsWithBLOBs;
import io.metersphere.base.mapper.ReportStatisticsMapper;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.reportstatistics.dto.ReportStatisticsSaveRequest;
import io.metersphere.reportstatistics.dto.ReportStatisticsType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * @author song.tianyang
 * @Date 2021/9/14 4:50 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ReportStatisticsService {
    @Resource
    private ReportStatisticsMapper reportStatisticsMapper;

    public ReportStatisticsWithBLOBs saveByRequest(ReportStatisticsSaveRequest request) {
        ReportStatisticsWithBLOBs model = new ReportStatisticsWithBLOBs();
        model.setId(UUID.randomUUID().toString());

        String name = "用例分析报表";
        if(StringUtils.equalsIgnoreCase(ReportStatisticsType.TEST_CASE_COUNT.name(),request.getReportType())){
            name = "用例统计报表";
            model.setReportType(ReportStatisticsType.TEST_CASE_COUNT.name());
        }else {
            model.setReportType(ReportStatisticsType.TEST_CASE_ANALYSIS.name());
        }
        if(StringUtils.isEmpty(request.getName())){
            request.setName(name);
        }
        model.setName(request.getName());
        model.setDataOption(request.getDataOption());
        model.setSelectOption(request.getSelectOption());
        model.setCreateTime(System.currentTimeMillis());
        model.setUpdateTime(System.currentTimeMillis());
        model.setProjectId(request.getProjectId());
        String userId = SessionUtils.getUserId();
        model.setCreateUser(userId);
        model.setUpdateUser(userId);

        reportStatisticsMapper.insert(model);

        return model;
    }

    public int deleteById(String id) {
        return reportStatisticsMapper.deleteByPrimaryKey(id);
    }

    public List<ReportStatistics> selectByProjectIdAndReportType(String projectId, String reportType) {
        ReportStatisticsExample example = new ReportStatisticsExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andReportTypeEqualTo(reportType);
        example.setOrderByClause("create_time DESC");
        return  reportStatisticsMapper.selectByExample(example);
    }

    public ReportStatisticsWithBLOBs selectById(String id) {
        return reportStatisticsMapper.selectByPrimaryKey(id);
    }

    public ReportStatisticsWithBLOBs updateByRequest(ReportStatisticsSaveRequest request) {
        ReportStatisticsWithBLOBs updateModel = new ReportStatisticsWithBLOBs();
        updateModel.setId(request.getId());
        updateModel.setName(request.getName());
        updateModel.setUpdateTime(request.getUpdateTime());
        updateModel.setUpdateUser(SessionUtils.getUserId());
        reportStatisticsMapper.updateByPrimaryKeySelective(updateModel);
        return updateModel;
    }
}
