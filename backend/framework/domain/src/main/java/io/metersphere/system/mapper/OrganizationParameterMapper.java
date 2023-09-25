package io.metersphere.system.mapper;

import io.metersphere.system.domain.OrganizationParameter;
import io.metersphere.system.domain.OrganizationParameterExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrganizationParameterMapper {
    long countByExample(OrganizationParameterExample example);

    int deleteByExample(OrganizationParameterExample example);

    int deleteByPrimaryKey(@Param("organizationId") String organizationId, @Param("paramKey") String paramKey);

    int insert(OrganizationParameter record);

    int insertSelective(OrganizationParameter record);

    List<OrganizationParameter> selectByExample(OrganizationParameterExample example);

    OrganizationParameter selectByPrimaryKey(@Param("organizationId") String organizationId, @Param("paramKey") String paramKey);

    int updateByExampleSelective(@Param("record") OrganizationParameter record, @Param("example") OrganizationParameterExample example);

    int updateByExample(@Param("record") OrganizationParameter record, @Param("example") OrganizationParameterExample example);

    int updateByPrimaryKeySelective(OrganizationParameter record);

    int updateByPrimaryKey(OrganizationParameter record);

    int batchInsert(@Param("list") List<OrganizationParameter> list);

    int batchInsertSelective(@Param("list") List<OrganizationParameter> list, @Param("selective") OrganizationParameter.Column ... selective);
}