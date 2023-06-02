package io.metersphere.bug.mapper;

import io.metersphere.bug.domain.BugFunctionalCase;
import io.metersphere.bug.domain.BugFunctionalCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BugFunctionalCaseMapper {
    long countByExample(BugFunctionalCaseExample example);

    int deleteByExample(BugFunctionalCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(BugFunctionalCase record);

    int insertSelective(BugFunctionalCase record);

    List<BugFunctionalCase> selectByExample(BugFunctionalCaseExample example);

    BugFunctionalCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") BugFunctionalCase record, @Param("example") BugFunctionalCaseExample example);

    int updateByExample(@Param("record") BugFunctionalCase record, @Param("example") BugFunctionalCaseExample example);

    int updateByPrimaryKeySelective(BugFunctionalCase record);

    int updateByPrimaryKey(BugFunctionalCase record);
}