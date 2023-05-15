package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiScenarioReference;
import io.metersphere.api.mapper.ApiScenarioReferenceMapper;

 /**
 * 场景步骤引用CASE关系记录服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiScenarioReferenceService extends ServiceImpl<ApiScenarioReferenceMapper, ApiScenarioReference> {
}