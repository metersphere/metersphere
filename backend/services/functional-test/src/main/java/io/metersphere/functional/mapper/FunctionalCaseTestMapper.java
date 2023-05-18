package io.metersphere.functional.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.metersphere.functional.domain.FunctionalCaseTest;


@Mapper
public interface FunctionalCaseTestMapper extends BaseMapper<FunctionalCaseTest> {
	
}
