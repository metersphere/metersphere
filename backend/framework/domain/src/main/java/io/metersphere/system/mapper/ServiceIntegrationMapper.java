package io.metersphere.system.mapper;

import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.domain.ServiceIntegrationExample;
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

    int batchInsert(@Param("list") List<ServiceIntegration> list);

    int batchInsertSelective(@Param("list") List<ServiceIntegration> list, @Param("selective") ServiceIntegration.Column ... selective);
}