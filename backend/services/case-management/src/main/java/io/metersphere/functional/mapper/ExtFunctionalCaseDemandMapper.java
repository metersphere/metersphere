package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseDemand;
import io.metersphere.functional.dto.FunctionalDemandDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author guoyuqi
 */
public interface ExtFunctionalCaseDemandMapper {

    List<FunctionalDemandDTO> selectParentDemandByKeyword(@Param("keyword") String keyword, @Param("caseId") String caseId);

    List<FunctionalCaseDemand> selectDemandByProjectId(@Param("projectId") String projectId, @Param("platform") String platform);

    List<String> selectDemandIdsByCaseId(@Param("caseId") String caseId, @Param("platform") String platform);


}
