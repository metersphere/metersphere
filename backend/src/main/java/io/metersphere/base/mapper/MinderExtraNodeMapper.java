package io.metersphere.base.mapper;

import io.metersphere.base.domain.MinderExtraNode;
import io.metersphere.base.domain.MinderExtraNodeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MinderExtraNodeMapper {
    long countByExample(MinderExtraNodeExample example);

    int deleteByExample(MinderExtraNodeExample example);

    int deleteByPrimaryKey(String id);

    int insert(MinderExtraNode record);

    int insertSelective(MinderExtraNode record);

    List<MinderExtraNode> selectByExampleWithBLOBs(MinderExtraNodeExample example);

    List<MinderExtraNode> selectByExample(MinderExtraNodeExample example);

    MinderExtraNode selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") MinderExtraNode record, @Param("example") MinderExtraNodeExample example);

    int updateByExampleWithBLOBs(@Param("record") MinderExtraNode record, @Param("example") MinderExtraNodeExample example);

    int updateByExample(@Param("record") MinderExtraNode record, @Param("example") MinderExtraNodeExample example);

    int updateByPrimaryKeySelective(MinderExtraNode record);

    int updateByPrimaryKeyWithBLOBs(MinderExtraNode record);

    int updateByPrimaryKey(MinderExtraNode record);
}