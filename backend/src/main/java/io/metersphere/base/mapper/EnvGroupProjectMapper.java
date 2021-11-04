package io.metersphere.base.mapper;

import io.metersphere.api.dto.EnvironmentGroupDTO;
import io.metersphere.base.domain.EnvGroupProject;
import io.metersphere.base.domain.EnvGroupProjectExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EnvGroupProjectMapper {
    long countByExample(EnvGroupProjectExample example);

    int deleteByExample(EnvGroupProjectExample example);

    int insert(EnvGroupProject record);

    int insertSelective(EnvironmentGroupDTO record);

    List<EnvGroupProject> selectByExample(EnvGroupProjectExample example);

    int updateByExampleSelective(@Param("record") EnvGroupProject record, @Param("example") EnvGroupProjectExample example);

    int updateByExample(@Param("record") EnvGroupProject record, @Param("example") EnvGroupProjectExample example);
}