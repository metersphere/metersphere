package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiScenarioReportBaseInfoDTO;
import io.metersphere.api.dto.ErrorReportLibraryParseDTO;
import io.metersphere.base.domain.ApiScenarioReportResultWithBLOBs;
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
            if (queue.size() == 1) {
                queue.forEach(item -> {
                    // 事物控制器出来的结果特殊处理
                    if (StringUtils.isNotEmpty(item.getName()) && item.getName().startsWith("Transaction=") && CollectionUtils.isEmpty(item.getSubRequestResults())) {
                        LoggerUtil.debug("合并事物请求暂不入库");
                    } else {
                        apiScenarioReportResultMapper.insert(this.newApiScenarioReportResult(reportId, item));
                    }
                });
            } else {
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                ApiScenarioReportResultMapper batchMapper = sqlSession.getMapper(ApiScenarioReportResultMapper.class);
                queue.forEach(item -> {
                    if (StringUtils.isEmpty(item.getName()) || !item.getName().startsWith("Transaction=") || !CollectionUtils.isEmpty(item.getSubRequestResults())) {
                        batchMapper.insert(this.newApiScenarioReportResult(reportId, item));
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
                    apiScenarioReportResultMapper.insert(this.newApiScenarioReportResult(dtos.get(0).getReportId(), requestResult));
                }
            } else {
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
    }

    public ApiScenarioReportResultWithBLOBs newScenarioReportResult(String reportId, String resourceId) {
        ApiScenarioReportResultWithBLOBs report = new ApiScenarioReportResultWithBLOBs();
        report.setId(UUID.randomUUID().toString());
        report.setResourceId(resourceId);
        report.setReportId(reportId);
        report.setTotalAssertions(0L);
        report.setPassAssertions(0L);
        report.setCreateTime(System.currentTimeMillis());
        return report;
    }

    //记录基础信息
    private ApiScenarioReportBaseInfoDTO getBaseInfo(RequestResult result) {
        ApiScenarioReportBaseInfoDTO baseInfoDTO = new ApiScenarioReportBaseInfoDTO();
        baseInfoDTO.setReqName(result.getName());
        baseInfoDTO.setReqSuccess(result.isSuccess());
        baseInfoDTO.setReqError(result.getError());
        baseInfoDTO.setReqStartTime(result.getStartTime());
        if (result.getResponseResult() != null) {
            baseInfoDTO.setRspCode(result.getResponseResult().getResponseCode());
            baseInfoDTO.setRspTime(result.getResponseResult().getResponseTime());
        }
        return baseInfoDTO;
    }

    private ApiScenarioReportResultWithBLOBs newApiScenarioReportResult(String reportId, RequestResult baseResult) {
        //解析误报内容
        ErrorReportLibraryParseDTO errorCodeDTO = ErrorReportLibraryUtil.parseAssertions(baseResult);
        RequestResult result = errorCodeDTO.getResult();
        String resourceId = result.getResourceId();

        ApiScenarioReportResultWithBLOBs report = newScenarioReportResult(reportId, resourceId);
        report.setTotalAssertions(Long.parseLong(result.getTotalAssertions() + ""));
        report.setPassAssertions(Long.parseLong(result.getPassAssertions() + ""));
        String status = result.getError() == 0 ? ExecuteResult.Success.name() : ExecuteResult.Error.name();
        if (CollectionUtils.isNotEmpty(errorCodeDTO.getErrorCodeList())) {
            report.setErrorCode(errorCodeDTO.getErrorCodeStr());
        }
        if (StringUtils.equalsIgnoreCase(errorCodeDTO.getRequestStatus(), ExecuteResult.errorReportResult.name())) {
            status = errorCodeDTO.getRequestStatus();
        }
        report.setStatus(status);
        report.setRequestTime(result.getEndTime() - result.getStartTime());

        report.setBaseInfo(JSONObject.toJSONString(getBaseInfo(result)));
        report.setContent(JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8));

        LoggerUtil.info("报告ID [ " + reportId + " ] 执行请求：【 " + baseResult.getName() + "】 入库存储");
        return report;
    }
}
