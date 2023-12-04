package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.metersphere.functional.dto.FunctionalDemandDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author guoyuqi
 */
public interface ExtFunctionalCaseDemandMapper {

    List<FunctionalDemandDTO> selectGroupByKeyword(@Param("keyword") String keyword, @Param("caseId") String caseId);
    List<FunctionalCaseDemand> selectByKeyword(@Param("keyword") String keyword, @Param("caseId") String caseId, @Param("platforms") List<String> platforms, @Param("ids") List<String> ids);

}
