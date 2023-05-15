package io.metersphere.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.metersphere.api.domain.ApiDefinitionTemplate;

/**
 * API模版表数据库访问层
 *
 * @date : 2023-5-15
 */
@Mapper
public interface ApiDefinitionTemplateMapper  extends BaseMapper<ApiDefinitionTemplate> {
}