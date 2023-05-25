package io.metersphere.project.mapper;

import io.metersphere.project.domain.FakeErrorBlob;
import io.metersphere.project.domain.FakeErrorBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FakeErrorBlobMapper {
    long countByExample(FakeErrorBlobExample example);

    int deleteByExample(FakeErrorBlobExample example);

    int deleteByPrimaryKey(String fakeErrorId);

    int insert(FakeErrorBlob record);

    int insertSelective(FakeErrorBlob record);

    List<FakeErrorBlob> selectByExampleWithBLOBs(FakeErrorBlobExample example);

    List<FakeErrorBlob> selectByExample(FakeErrorBlobExample example);

    FakeErrorBlob selectByPrimaryKey(String fakeErrorId);

    int updateByExampleSelective(@Param("record") FakeErrorBlob record, @Param("example") FakeErrorBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") FakeErrorBlob record, @Param("example") FakeErrorBlobExample example);

    int updateByExample(@Param("record") FakeErrorBlob record, @Param("example") FakeErrorBlobExample example);

    int updateByPrimaryKeySelective(FakeErrorBlob record);

    int updateByPrimaryKeyWithBLOBs(FakeErrorBlob record);
}