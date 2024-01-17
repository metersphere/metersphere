package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.domain.ApiReportStep;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ApiReportStepMapper;
import io.metersphere.api.mapper.ExtApiReportMapper;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.service.UserLoginService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiReportService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ExtApiReportMapper extApiReportMapper;
    @Resource
    private UserLoginService userLoginService;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiReport(List<ApiReport> reports) {
        if (CollectionUtils.isNotEmpty(reports)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiReportMapper reportMapper = sqlSession.getMapper(ApiReportMapper.class);
            SubListUtils.dealForSubList(reports, 1000, subList -> {
                subList.forEach(reportMapper::insertSelective);
                sqlSession.flushStatements();
                sqlSession.clearCache();
            });
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void insertApiReportStep(List<ApiReportStep> reportSteps) {
        if (CollectionUtils.isNotEmpty(reportSteps)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiReportStepMapper stepMapper = sqlSession.getMapper(ApiReportStepMapper.class);
            SubListUtils.dealForSubList(reportSteps, 1000, subList -> {
                subList.forEach(stepMapper::insertSelective);
                sqlSession.flushStatements();
                sqlSession.clearCache();
            });
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public List<ApiReport> getPage(BasePageRequest request, String projectId) {
        List<ApiReport> list = extApiReportMapper.list(request, projectId);
        //取所有的userid
        Set<String> userSet = list.stream()
                .flatMap(apiReport -> Stream.of(apiReport.getUpdateUser(), apiReport.getDeleteUser(), apiReport.getCreateUser()))
                .collect(Collectors.toSet());
        Map<String, String> userMap = userLoginService.getUserNameMap(new ArrayList<>(userSet));
        list.forEach(apiReport -> {
            apiReport.setCreateUser(userMap.get(apiReport.getCreateUser()));
            apiReport.setUpdateUser(userMap.get(apiReport.getUpdateUser()));
            apiReport.setDeleteUser(userMap.get(apiReport.getDeleteUser()));
        });
        return list;
    }
}
