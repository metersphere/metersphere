package io.metersphere.remote.service;

import io.metersphere.base.domain.TestCase;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.request.QueryTestCaseRequest;
import io.metersphere.service.MicroService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestTrackRemoteService {

    private static final String SERVICE_NAME = MicroServiceName.TEST_TRACK;

    @Resource
    private MicroService microService;

    public List<TestCase> selectTestCaseByIds(List<String> testCaseIdList) {
        if (CollectionUtils.isNotEmpty(testCaseIdList)) {
            QueryTestCaseRequest request = new QueryTestCaseRequest();
            request.setIds(testCaseIdList);
            try {
                return microService.postForDataArray(SERVICE_NAME,
                        "/test/case/select/by/id",
                        request, TestCase.class);
            } catch (Exception e) {
                LogUtil.error("调用 测试跟踪服务失败", e);
            }
        }
        return new ArrayList<>(0);
    }
}
