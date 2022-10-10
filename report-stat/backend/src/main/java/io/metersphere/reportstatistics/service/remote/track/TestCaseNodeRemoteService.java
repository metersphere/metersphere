package io.metersphere.reportstatistics.service.remote.track;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.reportstatistics.dto.TreeNodeDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseNodeRemoteService extends TestTrackBaseService {
    private static final String URL_COUNT_TEST_CASE_BY_REQUEST = "/case/node/list/%s";

    public List<TreeNodeDTO> selectTreeNodesByProjectId(String projectId) {
        List<TreeNodeDTO> returnList = new ArrayList<>(0);
        try {
            returnList = microService.getForDataArray(serviceName, String.format(URL_COUNT_TEST_CASE_BY_REQUEST, projectId),
                    TreeNodeDTO.class);
        } catch (Exception e) {
            LogUtil.error("调用微服务失败!", e);
        }
        return returnList;
    }
}