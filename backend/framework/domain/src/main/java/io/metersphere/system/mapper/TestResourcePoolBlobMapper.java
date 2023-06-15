package io.metersphere.system.mapper;

import io.metersphere.system.domain.TestResourcePoolBlob;
import io.metersphere.system.domain.TestResourcePoolBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestResourcePoolBlobMapper {
    long countByExample(TestResourcePoolBlobExample example);

    int deleteByExample(TestResourcePoolBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestResourcePoolBlob record);

    int insertSelective(TestResourcePoolBlob record);

    List<TestResourcePoolBlob> selectByExampleWithBLOBs(TestResourcePoolBlobExample example);

    List<TestResourcePoolBlob> selectByExample(TestResourcePoolBlobExample example);

    TestResourcePoolBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestResourcePoolBlob record, @Param("example") TestResourcePoolBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") TestResourcePoolBlob record, @Param("example") TestResourcePoolBlobExample example);

    int updateByExample(@Param("record") TestResourcePoolBlob record, @Param("example") TestResourcePoolBlobExample example);

    int updateByPrimaryKeySelective(TestResourcePoolBlob record);

    int updateByPrimaryKeyWithBLOBs(TestResourcePoolBlob record);
}