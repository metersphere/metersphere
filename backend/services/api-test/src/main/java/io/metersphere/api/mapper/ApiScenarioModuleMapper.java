package io.metersphere.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.metersphere.api.domain.ApiScenarioModule;

/**
 * 场景模块数据库访问层
 *
 * @date : 2023-5-15
 */
@Mapper
public interface ApiScenarioModuleMapper  extends BaseMapper<ApiScenarioModule> {
}