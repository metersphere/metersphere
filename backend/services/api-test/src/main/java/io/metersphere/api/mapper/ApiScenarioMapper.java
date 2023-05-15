package io.metersphere.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.metersphere.api.domain.ApiScenario;

/**
 * 场景数据库访问层
 *
 * @date : 2023-5-15
 */
@Mapper
public interface ApiScenarioMapper  extends BaseMapper<ApiScenario> {
}