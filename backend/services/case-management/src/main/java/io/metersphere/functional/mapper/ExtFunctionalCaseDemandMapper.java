package io.metersphere.functional.mapper;

import io.metersphere.functional.dto.FunctionalDemandDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author guoyuqi
 */
public interface ExtFunctionalCaseDemandMapper {

    List<FunctionalDemandDTO> selectParentDemandByKeyword(@Param("keyword") String keyword, @Param("caseId") String caseId);

}
