package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.LoadTestReportResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtLoadTestReportResultMapper {

    @Select("SELECT * FROM load_test_report_result WHERE report_id = #{id} AND report_key = #{key}")
    List<LoadTestReportResult> selectByIdAndKey(@Param("id") String id, @Param("key") String key);
}