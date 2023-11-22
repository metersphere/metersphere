package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseDemand;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author guoyuqi
 */
public interface ExtFunctionalCaseDemandMapper {

    List<FunctionalCaseDemand> selectByKeyword(@Param("keyword") String keyword, @Param("caseId") String caseId);

}
