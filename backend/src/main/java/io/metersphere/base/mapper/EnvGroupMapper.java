package io.metersphere.base.mapper;

import io.metersphere.api.dto.EnvironmentGroupDTO;
import io.metersphere.base.domain.EnvGroup;
import io.metersphere.base.domain.EnvGroupExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EnvGroupMapper {
    long countByExample(EnvGroupExample example);

    int deleteByExample(EnvGroupExample example);

    int insert(EnvironmentGroupDTO record);

    int insertSelective(EnvGroup record);

    List<EnvGroup> selectByExample(EnvGroupExample example);

    int updateByExampleSelective(@Param("record") EnvGroup record, @Param("example") EnvGroupExample example);

    int updateByExample(@Param("record") EnvGroup record, @Param("example") EnvGroupExample example);

    List<EnvironmentGroupDTO> selectByenvGroupId(String envGroupId);
}