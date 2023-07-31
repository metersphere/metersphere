package io.metersphere.system.mapper;

import io.metersphere.system.domain.PluginOrganization;
import io.metersphere.system.domain.PluginOrganizationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PluginOrganizationMapper {
    long countByExample(PluginOrganizationExample example);

    int deleteByExample(PluginOrganizationExample example);

    int deleteByPrimaryKey(@Param("pluginId") String pluginId, @Param("organizationId") String organizationId);

    int insert(PluginOrganization record);

    int insertSelective(PluginOrganization record);

    List<PluginOrganization> selectByExample(PluginOrganizationExample example);

    int updateByExampleSelective(@Param("record") PluginOrganization record, @Param("example") PluginOrganizationExample example);

    int updateByExample(@Param("record") PluginOrganization record, @Param("example") PluginOrganizationExample example);

    int batchInsert(@Param("list") List<PluginOrganization> list);

    int batchInsertSelective(@Param("list") List<PluginOrganization> list, @Param("selective") PluginOrganization.Column ... selective);
}