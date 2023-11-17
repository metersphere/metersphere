package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.dto.BaseFunctionalCaseBatchDTO;
import io.metersphere.functional.dto.FunctionalCasePageDTO;
import io.metersphere.functional.dto.FunctionalCaseVersionDTO;
import io.metersphere.functional.request.FunctionalCaseBatchMoveRequest;
import io.metersphere.functional.request.FunctionalCasePageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtFunctionalCaseMapper {

    Long getPos(@Param("projectId") String projectId);

    void updateFunctionalCaseModule(@Param("refId") String refId, @Param("moduleId") String moduleId);

    List<FunctionalCaseVersionDTO> getFunctionalCaseByRefId(@Param("refId") String refId);

    List<String> getFunctionalCaseIds(@Param("projectId") String projectId);

    void removeToTrashByModuleIds(@Param("moduleIds") List<String> deleteIds);

    List<FunctionalCase> checkCaseByModuleIds(@Param("moduleIds") List<String> deleteIds);

    List<FunctionalCasePageDTO> list(@Param("request") FunctionalCasePageRequest request, @Param("deleted") boolean deleted);

    void recoverCase(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("time") long time);


    List<String> getIds(@Param("request") BaseFunctionalCaseBatchDTO request, @Param("projectId") String projectId, @Param("deleted") boolean deleted);

    void batchDelete(@Param("ids") List<String> ids, @Param("userId") String userId);

    List<FunctionalCase> getLogInfo(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    List<String> getRefIds(@Param("ids") List<String> ids, @Param("deleted") boolean deleted);

    void batchMoveModule(@Param("request") FunctionalCaseBatchMoveRequest request, @Param("ids") List<String> ids, @Param("userId") String userId);

    List<FunctionalCase> getTagsByIds(@Param("ids") List<String> ids);


    void batchUpdate(@Param("functionalCase") FunctionalCase functionalCase, @Param("ids") List<String> ids);

    void recoverCaseByRefIds(@Param("refIds") List<String> refIds, @Param("userId") String userId, @Param("time") long time);
}
