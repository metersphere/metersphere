package io.metersphere.functional.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.metersphere.functional.domain.FunctionalCase;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FunctionalCaseMapper extends BaseMapper<FunctionalCase> {
}
