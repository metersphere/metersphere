package io.metersphere.base.mapper;

import io.metersphere.base.domain.SwaggerUrlProject;
import io.metersphere.base.domain.SwaggerUrlProjectExample;
import io.metersphere.base.domain.SwaggerUrlProjectWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SwaggerUrlProjectMapper {
    long countByExample(SwaggerUrlProjectExample example);

    int deleteByExample(SwaggerUrlProjectExample example);

    int deleteByPrimaryKey(String id);

    int insert(SwaggerUrlProjectWithBLOBs record);

    int insertSelective(SwaggerUrlProjectWithBLOBs record);

    List<SwaggerUrlProjectWithBLOBs> selectByExampleWithBLOBs(SwaggerUrlProjectExample example);

    List<SwaggerUrlProject> selectByExample(SwaggerUrlProjectExample example);

    SwaggerUrlProjectWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") SwaggerUrlProjectWithBLOBs record, @Param("example") SwaggerUrlProjectExample example);

    int updateByExampleWithBLOBs(@Param("record") SwaggerUrlProjectWithBLOBs record, @Param("example") SwaggerUrlProjectExample example);

    int updateByExample(@Param("record") SwaggerUrlProject record, @Param("example") SwaggerUrlProjectExample example);

    int updateByPrimaryKeySelective(SwaggerUrlProjectWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(SwaggerUrlProjectWithBLOBs record);

    int updateByPrimaryKey(SwaggerUrlProject record);
}