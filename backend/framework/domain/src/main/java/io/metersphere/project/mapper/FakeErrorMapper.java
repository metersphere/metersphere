package io.metersphere.project.mapper;

import io.metersphere.project.domain.FakeError;
import io.metersphere.project.domain.FakeErrorExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FakeErrorMapper {
    long countByExample(FakeErrorExample example);

    int deleteByExample(FakeErrorExample example);

    int deleteByPrimaryKey(String id);

    int insert(FakeError record);

    int insertSelective(FakeError record);

    List<FakeError> selectByExample(FakeErrorExample example);

    FakeError selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") FakeError record, @Param("example") FakeErrorExample example);

    int updateByExample(@Param("record") FakeError record, @Param("example") FakeErrorExample example);

    int updateByPrimaryKeySelective(FakeError record);

    int updateByPrimaryKey(FakeError record);

    int batchInsert(@Param("list") List<FakeError> list);

    int batchInsertSelective(@Param("list") List<FakeError> list, @Param("selective") FakeError.Column ... selective);
}