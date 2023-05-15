package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.mapper.ApiScenarioReportMapper;

 /**
 * 场景报告服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiScenarioReportService extends ServiceImpl<ApiScenarioReportMapper, ApiScenarioReport> {
}