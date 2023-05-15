package io.metersphere.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.metersphere.api.domain.ApiDefinitionBlob;

/**
 * 接口定义详情内容数据库访问层
 *
 * @date : 2023-5-15
 */
@Mapper
public interface ApiDefinitionBlobMapper extends BaseMapper<ApiDefinitionBlob> {
}