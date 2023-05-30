package io.metersphere.issue.mapper;

import io.metersphere.issue.domain.IssueBlob;
import io.metersphere.issue.domain.IssueBlobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IssueBlobMapper {
    long countByExample(IssueBlobExample example);

    int deleteByExample(IssueBlobExample example);

    int deleteByPrimaryKey(String id);

    int insert(IssueBlob record);

    int insertSelective(IssueBlob record);

    List<IssueBlob> selectByExampleWithBLOBs(IssueBlobExample example);

    List<IssueBlob> selectByExample(IssueBlobExample example);

    IssueBlob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") IssueBlob record, @Param("example") IssueBlobExample example);

    int updateByExampleWithBLOBs(@Param("record") IssueBlob record, @Param("example") IssueBlobExample example);

    int updateByExample(@Param("record") IssueBlob record, @Param("example") IssueBlobExample example);

    int updateByPrimaryKeySelective(IssueBlob record);

    int updateByPrimaryKeyWithBLOBs(IssueBlob record);
}