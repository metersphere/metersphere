package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseFollower;
import io.metersphere.functional.domain.FunctionalCaseFollowerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FunctionalCaseFollowerMapper {
    long countByExample(FunctionalCaseFollowerExample example);

    int deleteByExample(FunctionalCaseFollowerExample example);

    int deleteByPrimaryKey(@Param("caseId") String caseId, @Param("userId") String userId);

    int insert(FunctionalCaseFollower record);

    int insertSelective(FunctionalCaseFollower record);

    List<FunctionalCaseFollower> selectByExample(FunctionalCaseFollowerExample example);

    int updateByExampleSelective(@Param("record") FunctionalCaseFollower record, @Param("example") FunctionalCaseFollowerExample example);

    int updateByExample(@Param("record") FunctionalCaseFollower record, @Param("example") FunctionalCaseFollowerExample example);

    int batchInsert(@Param("list") List<FunctionalCaseFollower> list);

    int batchInsertSelective(@Param("list") List<FunctionalCaseFollower> list, @Param("selective") FunctionalCaseFollower.Column ... selective);
}