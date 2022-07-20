package io.metersphere.api.service;

import io.metersphere.api.service.utils.ResultConversionUtil;
import io.metersphere.base.domain.ApiScenarioReportResultWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioReportResultMapper;
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
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportResultService {
    @Resource
    private ApiScenarioReportResultMapper apiScenarioReportResultMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void save(String reportId, List<RequestResult> queue) {
        if (CollectionUtils.isNotEmpty(queue)) {
            if (queue.size() == 1) {
                queue.forEach(item -> {
                    // 事物控制器出来的结果特殊处理
                    if (StringUtils.isNotEmpty(item.getName()) && item.getName().startsWith("Transaction=") && CollectionUtils.isEmpty(item.getSubRequestResults())) {
                        LoggerUtil.debug("合并事物请求暂不入库");
                    } else {
                        apiScenarioReportResultMapper.insert(ResultConversionUtil.getApiScenarioReportResultBLOBs(reportId, item));
                    }
                });
            } else {
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                ApiScenarioReportResultMapper batchMapper = sqlSession.getMapper(ApiScenarioReportResultMapper.class);
                queue.forEach(item -> {
                    if (StringUtils.isEmpty(item.getName()) || !item.getName().startsWith("Transaction=") || !CollectionUtils.isEmpty(item.getSubRequestResults())) {
                        batchMapper.insert(ResultConversionUtil.getApiScenarioReportResultBLOBs(reportId, item));
                    }
                });
                sqlSession.flushStatements();
                if (sqlSession != null && sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        }
    }

    public void batchSave(List<ResultDTO> dtos) {
        if (CollectionUtils.isNotEmpty(dtos)) {
            if (dtos.size() == 1 && CollectionUtils.isNotEmpty(dtos.get(0).getRequestResults()) && dtos.get(0).getRequestResults().size() == 1) {
                // 单条储存
                RequestResult requestResult = dtos.get(0).getRequestResults().get(0);
                if (StringUtils.isEmpty(requestResult.getName()) || !requestResult.getName().startsWith("Transaction=") || !CollectionUtils.isEmpty(requestResult.getSubRequestResults())) {
                    apiScenarioReportResultMapper.insert(ResultConversionUtil.getApiScenarioReportResultBLOBs(dtos.get(0).getReportId(), requestResult));
                }
            } else {
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                ApiScenarioReportResultMapper batchMapper = sqlSession.getMapper(ApiScenarioReportResultMapper.class);
                for (ResultDTO dto : dtos) {
                    if (CollectionUtils.isNotEmpty(dto.getRequestResults())) {
                        dto.getRequestResults().forEach(item -> {
                            if (StringUtils.isEmpty(item.getName()) || !item.getName().startsWith("Transaction=") || !CollectionUtils.isEmpty(item.getSubRequestResults())) {
                                batchMapper.insert(ResultConversionUtil.getApiScenarioReportResultBLOBs(dto.getReportId(), item));
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
    }

    public ApiScenarioReportResultWithBLOBs newScenarioReportResult(String reportId, String resourceId) {
        return ResultConversionUtil.newScenarioReportResult(reportId, resourceId);
    }
}
