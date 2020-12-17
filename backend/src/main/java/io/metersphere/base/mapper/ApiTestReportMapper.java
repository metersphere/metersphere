package io.metersphere.base.mapper;

import io.metersphere.base.domain.ApiTestReport;
import io.metersphere.base.domain.ApiTestReportExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ApiTestReportMapper {
    long countByExample(ApiTestReportExample example);

    int deleteByExample(ApiTestReportExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiTestReport record);

    int insertSelective(ApiTestReport record);

    List<ApiTestReport> selectByExample(ApiTestReportExample example);

    ApiTestReport selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiTestReport record, @Param("example") ApiTestReportExample example);

    int updateByExample(@Param("record") ApiTestReport record, @Param("example") ApiTestReportExample example);

    int updateByPrimaryKeySelective(ApiTestReport record);

    int updateByPrimaryKey(ApiTestReport record);

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