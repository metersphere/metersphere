package io.metersphere.functional.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.metersphere.functional.domain.FunctionalCaseComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FunctionalCaseCommentMapper extends BaseMapper<FunctionalCaseComment> {
}
