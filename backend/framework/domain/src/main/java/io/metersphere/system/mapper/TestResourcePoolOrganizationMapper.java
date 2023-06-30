package io.metersphere.system.mapper;

import io.metersphere.system.domain.TestResourcePoolOrganization;
import io.metersphere.system.domain.TestResourcePoolOrganizationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestResourcePoolOrganizationMapper {
    long countByExample(TestResourcePoolOrganizationExample example);

    int deleteByExample(TestResourcePoolOrganizationExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestResourcePoolOrganization record);

    int insertSelective(TestResourcePoolOrganization record);

    List<TestResourcePoolOrganization> selectByExample(TestResourcePoolOrganizationExample example);

    TestResourcePoolOrganization selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestResourcePoolOrganization record, @Param("example") TestResourcePoolOrganizationExample example);

    int updateByExample(@Param("record") TestResourcePoolOrganization record, @Param("example") TestResourcePoolOrganizationExample example);

    int updateByPrimaryKeySelective(TestResourcePoolOrganization record);

    int updateByPrimaryKey(TestResourcePoolOrganization record);
}