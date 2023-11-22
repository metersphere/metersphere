package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.CaseReview;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wx
 */
public interface ExtCaseReviewMapper {

    List<CaseReview> checkCaseByModuleIds(@Param("moduleIds") List<String> deleteIds);


}
