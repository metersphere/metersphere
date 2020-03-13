package io.metersphere.base.mapper;

import io.metersphere.base.domain.FucTestFile;
import io.metersphere.base.domain.FucTestFileExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FucTestFileMapper {
    long countByExample(FucTestFileExample example);

    int deleteByExample(FucTestFileExample example);

    int insert(FucTestFile record);

    int insertSelective(FucTestFile record);

    List<FucTestFile> selectByExample(FucTestFileExample example);

    int updateByExampleSelective(@Param("record") FucTestFile record, @Param("example") FucTestFileExample example);

    int updateByExample(@Param("record") FucTestFile record, @Param("example") FucTestFileExample example);
}