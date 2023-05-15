package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiScenarioReportLog;
import io.metersphere.api.mapper.ApiScenarioReportLogMapper;

 /**
 * 场景报告过程日志服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiScenarioReportLogService extends ServiceImpl<ApiScenarioReportLogMapper, ApiScenarioReportLog> {
}