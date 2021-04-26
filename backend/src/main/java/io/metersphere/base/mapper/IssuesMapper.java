package io.metersphere.base.mapper;

import io.metersphere.base.domain.Issues;
import io.metersphere.base.domain.IssuesExample;
import io.metersphere.base.domain.IssuesWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IssuesMapper {
    long countByExample(IssuesExample example);

    int deleteByExample(IssuesExample example);

    int deleteByPrimaryKey(String id);

    int insert(IssuesWithBLOBs record);

    int insertSelective(IssuesWithBLOBs record);

    List<IssuesWithBLOBs> selectByExampleWithBLOBs(IssuesExample example);

    List<Issues> selectByExample(IssuesExample example);

    IssuesWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") IssuesWithBLOBs record, @Param("example") IssuesExample example);

    int updateByExampleWithBLOBs(@Param("record") IssuesWithBLOBs record, @Param("example") IssuesExample example);

    int updateByExample(@Param("record") Issues record, @Param("example") IssuesExample example);

    int updateByPrimaryKeySelective(IssuesWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(IssuesWithBLOBs record);

    int updateByPrimaryKey(Issues record);
}