package io.metersphere.base.mapper;

import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.base.domain.ServiceIntegrationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ServiceIntegrationMapper {
    long countByExample(ServiceIntegrationExample example);

    int deleteByExample(ServiceIntegrationExample example);

    int deleteByPrimaryKey(String id);

    int insert(ServiceIntegration record);

    int insertSelective(ServiceIntegration record);

    List<ServiceIntegration> selectByExampleWithBLOBs(ServiceIntegrationExample example);

    List<ServiceIntegration> selectByExample(ServiceIntegrationExample example);

    ServiceIntegration selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ServiceIntegration record, @Param("example") ServiceIntegrationExample example);

    int updateByExampleWithBLOBs(@Param("record") ServiceIntegration record, @Param("example") ServiceIntegrationExample example);

    int updateByExample(@Param("record") ServiceIntegration record, @Param("example") ServiceIntegrationExample example);

    int updateByPrimaryKeySelective(ServiceIntegration record);

    int updateByPrimaryKeyWithBLOBs(ServiceIntegration record);

    int updateByPrimaryKey(ServiceIntegration record);
}