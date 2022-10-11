package io.metersphere.service.remote.project;

import io.metersphere.dto.CustomFieldDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackCustomFieldTemplateService extends TrackProjectSettingService {

    private static final String BASE_UEL = "/custom/field/template";

    public List<CustomFieldDao> getCustomFieldByTemplateId(String templateId) {
        return microService.getForDataArray(serviceName, BASE_UEL + "/list/" + templateId, CustomFieldDao.class);
    }
}
