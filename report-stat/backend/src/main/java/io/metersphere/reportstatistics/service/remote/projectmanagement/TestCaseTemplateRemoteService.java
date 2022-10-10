package io.metersphere.reportstatistics.service.remote.projectmanagement;

import io.metersphere.commons.utils.LogUtil;
import io.metersphere.reportstatistics.dto.TestCaseTemplateDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseTemplateRemoteService extends ProjectSettingBaseService {

    private static final String URL_SELECT_TEST_CASE_FIELD = "/field/template/case/get/relate/%s";

    public TestCaseTemplateDTO selectTemplateByProjectId(String projectId) {
        TestCaseTemplateDTO returnDTO = null;
        try {
            returnDTO = microService.getForData(serviceName, String.format(URL_SELECT_TEST_CASE_FIELD, projectId),
                    TestCaseTemplateDTO.class);
        } catch (Exception e) {
            LogUtil.error("调用微服务失败!", e);
        }
        return returnDTO;
    }

}
