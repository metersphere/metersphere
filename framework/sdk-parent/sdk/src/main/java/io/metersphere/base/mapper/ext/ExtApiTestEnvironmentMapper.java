package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExtApiTestEnvironmentMapper {

    @Select("SELECT * FROM api_test_environment WHERE name = #{envName} LIMIT #{startNum},#{pageSize}")
    List<ApiTestEnvironmentWithBLOBs> selectByNameAndLimitNum(@Param("envName") String envName, @Param("startNum") int startNum, @Param("pageSize") int pageSize);
}
