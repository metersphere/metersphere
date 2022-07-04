package io.metersphere.base.mapper;

import io.metersphere.base.domain.IssueFile;
import io.metersphere.base.domain.IssueFileExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IssueFileMapper {
    long countByExample(IssueFileExample example);

    int deleteByExample(IssueFileExample example);

    int insert(IssueFile record);

    int insertSelective(IssueFile record);

    List<IssueFile> selectByExample(IssueFileExample example);

    int updateByExampleSelective(@Param("record") IssueFile record, @Param("example") IssueFileExample example);

    int updateByExample(@Param("record") IssueFile record, @Param("example") IssueFileExample example);
}