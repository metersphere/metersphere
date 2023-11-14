package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseCustomField;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtFunctionalCaseCustomFieldMapper {


    List<FunctionalCaseCustomField> getCustomFieldByCaseIds(@Param("ids") List<String> ids);

    void batchUpdate(@Param("functionalCaseCustomField") FunctionalCaseCustomField functionalCaseCustomField, @Param("ids") List<String> ids);
}
