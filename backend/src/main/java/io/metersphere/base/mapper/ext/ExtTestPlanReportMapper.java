package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.track.dto.TestPlanReportDTO;
import io.metersphere.track.request.report.QueryTestPlanReportRequest;
import org.apache.ibatis.annotations.InsertProvider;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/1/8 4:58 下午
 * @Description
 */
public interface ExtTestPlanReportMapper {
    List<TestPlanReportDTO> list(@Param("request")QueryTestPlanReportRequest request);
}
