package io.metersphere.base.mapper;

import io.metersphere.api.dto.delimit.ApiComputeResult;
import io.metersphere.api.dto.delimit.ApiDelimitRequest;
import io.metersphere.api.dto.delimit.ApiDelimitResult;
import io.metersphere.base.domain.ApiDelimit;
import io.metersphere.base.domain.ApiDelimitExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApiDelimitMapper {
    
    List<ApiDelimitResult> list(@Param("request") ApiDelimitRequest request);

    List<ApiComputeResult> selectByIds(@Param("ids") List<String> ids);

    long countByExample(ApiDelimitExample example);

    int deleteByExample(ApiDelimitExample example);

    int deleteByPrimaryKey(String id);

    int insert(ApiDelimit record);

    int insertSelective(ApiDelimit record);

    List<ApiDelimit> selectByExampleWithBLOBs(ApiDelimitExample example);

    List<ApiDelimit> selectByExample(ApiDelimitExample example);

    ApiDelimit selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ApiDelimit record, @Param("example") ApiDelimitExample example);

    int updateByExampleWithBLOBs(@Param("record") ApiDelimit record, @Param("example") ApiDelimitExample example);

    int updateByExample(@Param("record") ApiDelimit record, @Param("example") ApiDelimitExample example);

    int updateByPrimaryKeySelective(ApiDelimit record);

    int updateByPrimaryKeyWithBLOBs(ApiDelimit record);

    int updateByPrimaryKey(ApiDelimit record);
}