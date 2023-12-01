package io.metersphere.functional.mapper;

import io.metersphere.functional.dto.CaseReviewUserDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author guoyuqi
 */
public interface ExtCaseReviewUserMapper {

    List<CaseReviewUserDTO> getReviewUser(@Param("reviewIds") List<String> reviewIds);



}
