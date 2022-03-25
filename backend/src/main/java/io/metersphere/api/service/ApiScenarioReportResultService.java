package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.ErrorReportLibraryParseDTO;
import io.metersphere.base.domain.ApiScenarioReportResult;
import io.metersphere.base.mapper.ApiScenarioReportResultMapper;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.commons.utils.ErrorReportLibraryUtil;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportResultService {
    @Resource
    private ApiScenarioReportResultMapper apiScenarioReportResultMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void save(String reportId, List<RequestResult> queue) {
        if (CollectionUtils.isNotEmpty(queue)) {
            queue.forEach(item -> {
                // 事物控制器出来的结果特殊处理
                if (StringUtils.isNotEmpty(item.getName()) && item.getName().startsWith("Transaction=") && CollectionUtils.isEmpty(item.getSubRequestResults())) {
                    LoggerUtil.debug("合并事物请求暂不入库");
                } else {
                    if (!StringUtils.startsWithAny(item.getName(), "PRE_PROCESSOR_ENV_", "POST_PROCESSOR_ENV_")) {
                        apiScenarioReportResultMapper.insert(this.newApiScenarioReportResult(reportId, item));
                    }
                }
            });
        }
    }

    public void batchSave(List<ResultDTO> dtos) {
        if (CollectionUtils.isNotEmpty(dtos)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiScenarioReportResultMapper batchMapper = sqlSession.getMapper(ApiScenarioReportResultMapper.class);
            for (ResultDTO dto : dtos) {
                if (CollectionUtils.isNotEmpty(dto.getRequestResults())) {
                    dto.getRequestResults().forEach(item -> {
                        if (StringUtils.isEmpty(item.getName()) || !item.getName().startsWith("Transaction=") || !CollectionUtils.isEmpty(item.getSubRequestResults())) {
                            batchMapper.insert(this.newApiScenarioReportResult(dto.getReportId(), item));
                        }
                    });
                }
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    private ApiScenarioReportResult newApiScenarioReportResult(String reportId, RequestResult baseResult) {
        ApiScenarioReportResult report = new ApiScenarioReportResult();
        //解析误报内容
        ErrorReportLibraryParseDTO errorCodeDTO = ErrorReportLibraryUtil.parseAssertions(baseResult);
        RequestResult result = errorCodeDTO.getResult();
        report.setId(UUID.randomUUID().toString());
        String resourceId = result.getResourceId();
        report.setResourceId(resourceId);
        report.setReportId(reportId);
        report.setTotalAssertions(Long.parseLong(result.getTotalAssertions() + ""));
        report.setPassAssertions(Long.parseLong(result.getPassAssertions() + ""));
        report.setCreateTime(System.currentTimeMillis());
        String status = result.getError() == 0 ? ExecuteResult.Success.name() : ExecuteResult.Error.name();
        if (CollectionUtils.isNotEmpty(errorCodeDTO.getErrorCodeList())) {
            status = ExecuteResult.errorReportResult.name();
            report.setErrorCode(errorCodeDTO.getErrorCodeStr());
        }
        report.setStatus(status);
        report.setRequestTime(result.getEndTime() - result.getStartTime());
        report.setContent(JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        return report;
    }
}
