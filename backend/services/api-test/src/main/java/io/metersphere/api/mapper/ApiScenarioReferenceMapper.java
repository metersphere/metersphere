package io.metersphere.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.metersphere.api.domain.ApiScenarioReference;

/**
 * 场景步骤引用CASE关系记录数据库访问层
 *
 * @date : 2023-5-15
 */
@Mapper
public interface ApiScenarioReferenceMapper  extends BaseMapper<ApiScenarioReference> {
}