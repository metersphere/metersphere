package io.metersphere.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.metersphere.api.domain.ApiEnvironmentConfig;

/**
 * 接口定义环境数据库访问层
 *
 * @date : 2023-5-15
 */
@Mapper
public interface ApiEnvironmentConfigMapper extends BaseMapper<ApiEnvironmentConfig> {
}