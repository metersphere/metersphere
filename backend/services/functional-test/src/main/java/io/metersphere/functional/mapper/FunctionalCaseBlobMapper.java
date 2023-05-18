/**
 * @filename:FunctionalCaseBlobDao 2023年5月17日
 * @project ms  V3.x
 * Copyright(c) 2020 wx Co. Ltd. 
 * All right reserved. 
 */
package io.metersphere.functional.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.metersphere.functional.domain.FunctionalCaseBlob;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface FunctionalCaseBlobMapper extends BaseMapper<FunctionalCaseBlob> {
	
}
