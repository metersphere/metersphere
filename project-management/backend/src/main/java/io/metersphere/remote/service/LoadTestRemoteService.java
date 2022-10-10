package io.metersphere.remote.service;

import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.remote.dto.LoadTestBatchRequest;
import io.metersphere.remote.dto.LoadTestFileDTO;
import io.metersphere.service.MicroService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class LoadTestRemoteService {

    private static final String SERVICE_NAME = MicroServiceName.PERFORMANCE_TEST;

    @Resource
    private MicroService microService;

    public List<LoadTestFileDTO> selectByFileIds(List<String> idList) {
        if (CollectionUtils.isNotEmpty(idList)) {
            LoadTestBatchRequest request = new LoadTestBatchRequest();
            request.setIds(idList);
            try {
                return microService.postForDataArray(SERVICE_NAME,
                        "/performance/file/list/",
                        request, LoadTestFileDTO.class);
            } catch (Exception e) {
                LogUtil.error("调用 性能测试服务 失败", e);
            }
        }
        return new ArrayList<>();
    }
}
