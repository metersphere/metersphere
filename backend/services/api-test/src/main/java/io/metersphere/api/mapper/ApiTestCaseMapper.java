package io.metersphere.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import io.metersphere.api.domain.ApiTestCase;

/**
 * 接口用例数据库访问层
 *
 * @date : 2023-5-15
 */
@Mapper
public interface ApiTestCaseMapper  extends BaseMapper<ApiTestCase> {
}