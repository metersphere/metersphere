package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiScenarioReportBaseInfoDTO;
import io.metersphere.api.dto.ErrorReportLibraryParseDTO;
import io.metersphere.base.domain.ApiScenarioReportResultWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioReportResultMapper;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.commons.utils.ErrorReportLibraryUtil;
import io.metersphere.commons.utils.LogUtil;
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
                    apiScenarioReportResultMapper.insert(this.newApiScenarioReportResult(reportId, item));
                }
            });
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

    public void uiSave(String reportId, List<RequestResult> queue) {
        if (CollectionUtils.isNotEmpty(queue)) {
            queue.forEach(item -> {
                String header = item.getResponseResult().getHeaders();
                if (StringUtils.isNoneBlank(header)) {
                    JSONObject jsonObject = JSONObject.parseObject(header);
                    for (String resourceId : jsonObject.keySet()) {
                        if (resourceId.length() > 36) {
                            resourceId = resourceId.substring(0, 36);
                        }
                        apiScenarioReportResultMapper.insert(this.newUiScenarioReportResult(reportId, resourceId, jsonObject.getJSONObject(resourceId)));
                    }
                }
            });
        }
    }

    private ApiScenarioReportResultWithBLOBs newUiScenarioReportResult(String reportId, String resourceId, JSONObject value) {
        ApiScenarioReportResultWithBLOBs report = newScenarioReportResult(reportId, resourceId);
        String status = value.getBooleanValue("success") ? ExecuteResult.Success.name() : ExecuteResult.Error.name();
        report.setStatus(status);
        RequestResult result = JSONObject.parseObject(value.toJSONString(), RequestResult.class);
        ApiScenarioReportBaseInfoDTO baseInfo = getBaseInfo(result);
        baseInfo.setRspTime(result.getEndTime() - result.getStartTime());
        baseInfo.setUiImg(result.getUrl());
        report.setBaseInfo(JSONObject.toJSONString(baseInfo));
        report.setContent(value.toJSONString().getBytes(StandardCharsets.UTF_8));
        return report;
    }

    private ApiScenarioReportResultWithBLOBs newScenarioReportResult(String reportId, String resourceId) {
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
            status = ExecuteResult.errorReportResult.name();
            report.setErrorCode(errorCodeDTO.getErrorCodeStr());
        }
        report.setStatus(status);
        report.setRequestTime(result.getEndTime() - result.getStartTime());

        report.setBaseInfo(JSONObject.toJSONString(getBaseInfo(result)));
        report.setContent(JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        return report;
    }

    public boolean isResultFormat(ApiScenarioReportResultWithBLOBs result) {
        if (result != null && result.getBaseInfo() != null) {
            return true;
        }else {
            return false;
        }
    }

    public ApiScenarioReportResultWithBLOBs formatScenarioResult(ApiScenarioReportResultWithBLOBs result) {
        if (!this.isResultFormat(result)) {
            ApiScenarioReportResultWithBLOBs baseResult = apiScenarioReportResultMapper.selectByPrimaryKey(result.getId());
            if (baseResult != null) {
                try {
                    RequestResult requestResult = JSON.parseObject(new String(baseResult.getContent(), StandardCharsets.UTF_8), RequestResult.class);
                    //记录基础信息
                    baseResult.setBaseInfo(JSONObject.toJSONString(getBaseInfo(requestResult)));
                    apiScenarioReportResultMapper.updateByPrimaryKeySelective(baseResult);
                    return baseResult;
                } catch (Exception e) {
                    LogUtil.error("format scenario result error:" + e.getMessage());
                }
            }
        }
        return result;
    }
}
