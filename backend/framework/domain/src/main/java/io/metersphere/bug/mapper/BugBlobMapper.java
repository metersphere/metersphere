package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugBlob;
import io.metersphere.bug.domain.BugBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BugBlobMapper {
    long countByExample(BugBlobExample example);

    int deleteByExample(BugBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(BugBlob record);

    int insertSelective(BugBlob record);

    List<BugBlob> selectByExampleWithBLOBs(BugBlobExample example);

    List<BugBlob> selectByExample(BugBlobExample example);

    BugBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") BugBlob record, @Param("example") BugBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") BugBlob record, @Param("example") BugBlobExample example);

    int updateByExample(@Param("record") BugBlob record, @Param("example") BugBlobExample example);

    int updateByPrimaryKeySelective(BugBlob record);

    int updateByPrimaryKeyWithBLOBs(BugBlob record);
}