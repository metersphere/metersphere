package io.metersphere.api.service;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.mapper.ApiReportMapper;

 /**
 * API/CASE执行结果服务实现类
 *
 * @date : 2023-5-15
 */
@Service
public class ApiReportService extends ServiceImpl<ApiReportMapper, ApiReport> {
}