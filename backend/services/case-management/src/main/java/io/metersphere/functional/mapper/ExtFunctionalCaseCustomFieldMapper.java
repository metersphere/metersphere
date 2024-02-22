package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.dto.FunctionalCaseCustomFieldDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtFunctionalCaseCustomFieldMapper {


    List<FunctionalCaseCustomField> getCustomFieldByCaseIds(@Param("ids") List<String> ids);

    void batchUpdate(@Param("functionalCaseCustomField") FunctionalCaseCustomField functionalCaseCustomField, @Param("ids") List<String> ids);

    List<FunctionalCaseCustomFieldDTO> getCustomFieldsByCaseIds(@Param("ids") List<String> ids);

    void batchDelete(@Param("functionalCaseCustomField") FunctionalCaseCustomField functionalCaseCustomField, @Param("ids") List<String> ids);
}
