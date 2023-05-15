package io.metersphere.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.metersphere.api.domain.ApiScenarioReportStructure;

/**
 * 场景报告结构数据库访问层
 *
 * @date : 2023-5-15
 */
@Mapper
public interface ApiScenarioReportStructureMapper  extends BaseMapper<ApiScenarioReportStructure> {
}