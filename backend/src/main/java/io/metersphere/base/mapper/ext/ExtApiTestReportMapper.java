package io.metersphere.base.mapper.ext;

import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.QueryAPIReportRequest;
import io.metersphere.dto.ApiReportDTO;
import io.metersphere.dto.DashboardTestDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtApiTestReportMapper {

    List<APIReportResult> list(@Param("request") QueryAPIReportRequest request);

    List<APIReportResult> listByTestId(@Param("testId") String testId);

    APIReportResult get(@Param("id") String id);

    List<DashboardTestDTO> selectDashboardTests(@Param("workspaceId") String workspaceId, @Param("startTimestamp") long startTimestamp);


    @Select({
            "SELECT COUNT(testReportDetail.report_id) AS countNumber FROM api_test_report_detail testReportDetail ",
            "INNER JOIN `schedule` sch ON sch.resource_id = testReportDetail.test_id ",
            "INNER JOIN api_test_report testReport ON testReportDetail.report_id = testReport.id ",
            "WHERE workspace_id = #{workspaceID}  AND `group` = #{group} ",
    })
    long countByWorkspaceIdAndGroup(@Param("workspaceID") String workspaceID, @Param("group")String group);

    @Select({
            "SELECT COUNT(testReportDetail.report_id) AS countNumber FROM api_test_report_detail testReportDetail ",
            "INNER JOIN `schedule` sch ON sch.resource_id = testReportDetail.test_id ",
            "INNER JOIN api_test_report testReport ON testReportDetail.report_id = testReport.id ",
            "WHERE workspace_id = #{workspaceID}  AND `group` = #{group} ",
            "AND testReport.create_time BETWEEN #{startTime} and #{endTime} ",
    })
    long countByProjectIDAndCreateInThisWeek(@Param("workspaceID") String workspaceID, @Param("group")String group, @Param("startTime") long startTime, @Param("endTime")long endTime);
}
