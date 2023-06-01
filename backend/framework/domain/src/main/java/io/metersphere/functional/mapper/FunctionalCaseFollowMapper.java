package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseFollow;
import io.metersphere.functional.domain.FunctionalCaseFollowExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseFollowMapper {
    long countByExample(FunctionalCaseFollowExample example);

    int deleteByExample(FunctionalCaseFollowExample example);

    int deleteByPrimaryKey(@Param("caseId") String caseId, @Param("followId") String followId);

    int insert(FunctionalCaseFollow record);

    int insertSelective(FunctionalCaseFollow record);

    List<FunctionalCaseFollow> selectByExample(FunctionalCaseFollowExample example);

    int updateByExampleSelective(@Param("record") FunctionalCaseFollow record, @Param("example") FunctionalCaseFollowExample example);

    int updateByExample(@Param("record") FunctionalCaseFollow record, @Param("example") FunctionalCaseFollowExample example);
}