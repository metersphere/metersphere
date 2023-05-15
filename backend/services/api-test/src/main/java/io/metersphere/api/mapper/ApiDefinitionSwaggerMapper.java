package io.metersphere.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.metersphere.api.domain.ApiDefinitionSwagger;

/**
 * 定时同步配置数据库访问层
 *
 * @date : 2023-5-15
 */
@Mapper
public interface ApiDefinitionSwaggerMapper  extends BaseMapper<ApiDefinitionSwagger> {
}