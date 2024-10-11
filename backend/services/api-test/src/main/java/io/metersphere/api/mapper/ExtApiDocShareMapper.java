package io.metersphere.api.mapper;

import io.metersphere.api.dto.definition.ApiDocShareDTO;
import io.metersphere.api.dto.definition.request.ApiDocSharePageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExtApiDocShareMapper {

	/**
	 * 分页获取分享列表
	 * @param request 请求参数
	 * @return 分享列表
	 */
	List<ApiDocShareDTO> list(@Param("request")ApiDocSharePageRequest request);
}
