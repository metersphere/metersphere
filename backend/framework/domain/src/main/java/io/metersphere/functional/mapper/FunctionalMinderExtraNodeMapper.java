package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalMinderExtraNode;
import io.metersphere.functional.domain.FunctionalMinderExtraNodeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalMinderExtraNodeMapper {
    long countByExample(FunctionalMinderExtraNodeExample example);

    int deleteByExample(FunctionalMinderExtraNodeExample example);

    int deleteByPrimaryKey(String id);

    int insert(FunctionalMinderExtraNode record);

    int insertSelective(FunctionalMinderExtraNode record);

    List<FunctionalMinderExtraNode> selectByExampleWithBLOBs(FunctionalMinderExtraNodeExample example);

    List<FunctionalMinderExtraNode> selectByExample(FunctionalMinderExtraNodeExample example);

    FunctionalMinderExtraNode selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FunctionalMinderExtraNode record, @Param("example") FunctionalMinderExtraNodeExample example);

    int updateByExampleWithBLOBs(@Param("record") FunctionalMinderExtraNode record, @Param("example") FunctionalMinderExtraNodeExample example);

    int updateByExample(@Param("record") FunctionalMinderExtraNode record, @Param("example") FunctionalMinderExtraNodeExample example);

    int updateByPrimaryKeySelective(FunctionalMinderExtraNode record);

    int updateByPrimaryKeyWithBLOBs(FunctionalMinderExtraNode record);

    int updateByPrimaryKey(FunctionalMinderExtraNode record);

    int batchInsert(@Param("list") List<FunctionalMinderExtraNode> list);

    int batchInsertSelective(@Param("list") List<FunctionalMinderExtraNode> list, @Param("selective") FunctionalMinderExtraNode.Column ... selective);
}