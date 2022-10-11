package io.metersphere.service.remote.project;

import io.metersphere.xpack.track.dto.IssueTemplateDao;
import org.springframework.stereotype.Service;

@Service
public class TrackIssueTemplateService extends TrackProjectSettingService {
    private static final String BASE_UEL = "/field/template/issue";

    public IssueTemplateDao getTemplate(String projectId) {
        return microService.getForData(serviceName, BASE_UEL + "/get/relate/" + projectId,
                IssueTemplateDao.class);
    }
}
