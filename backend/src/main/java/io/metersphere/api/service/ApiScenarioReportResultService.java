package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.ApiScenarioReportResult;
import io.metersphere.base.mapper.ApiScenarioReportResultMapper;
import io.metersphere.dto.RequestResult;
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
    private SqlSessionFactory sqlSessionFactory;

    public void save(String reportId, List<RequestResult> queue) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiScenarioReportResultMapper batchMapper = sqlSession.getMapper(ApiScenarioReportResultMapper.class);
        queue.forEach(item -> {
            // 事物控制器出来的结果特殊处理
            if (StringUtils.isNotEmpty(item.getName()) && item.getName().startsWith("Transaction=") && CollectionUtils.isEmpty(item.getSubRequestResults())) {
                LoggerUtil.debug("合并事物请求暂不入库");
            } else if (StringUtils.isNotEmpty(item.getName()) && item.getName().startsWith("Transaction=") && CollectionUtils.isNotEmpty(item.getSubRequestResults())) {
                item.getSubRequestResults().forEach(subItem -> {
                    batchMapper.insert(this.newApiScenarioReportResult(reportId, subItem));
                });
            } else {
                batchMapper.insert(this.newApiScenarioReportResult(reportId, item));
            }
        });
        sqlSession.flushStatements();
        if (sqlSession != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private ApiScenarioReportResult newApiScenarioReportResult(String reportId, RequestResult result) {
        ApiScenarioReportResult report = new ApiScenarioReportResult();
        report.setId(UUID.randomUUID().toString());
        String resourceId = result.getResourceId();
        if (StringUtils.isNotEmpty(resourceId) && resourceId.contains("_")) {
            resourceId = StringUtils.substringBefore(result.getResourceId(), "_");
        }
        report.setResourceId(resourceId);
        report.setReportId(reportId);
        report.setTotalAssertions(Long.parseLong(result.getTotalAssertions() + ""));
        report.setPassAssertions(Long.parseLong(result.getPassAssertions() + ""));
        report.setCreateTime(System.currentTimeMillis());
        report.setStatus(result.getError() == 0 ? "Success" : "Error");
        report.setRequestTime(result.getEndTime() - result.getStartTime());
        report.setContent(JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8));
        return report;
    }
}
