package io.metersphere.service.remote.project;

import io.metersphere.commons.constants.CustomFieldScene;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldOptionDTO;
import io.metersphere.dto.TestCaseTemplateDao;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrackTestCaseTemplateService extends TrackProjectSettingService {

    private static final String BASE_UEL = "/field/template/case";

    public Map<String, List<String>> getCaseLevelAndStatusMapByProjectId(String projectId) {
        TestCaseTemplateDao template = getTemplate(projectId);
        List<CustomFieldDao> result = template.getCustomFields();

        Map<String, List<String>> returnMap = new HashMap<>();
        for (CustomFieldDao field : result) {
            if (StringUtils.equalsAnyIgnoreCase(field.getScene(), CustomFieldScene.TEST_CASE.name())) {
                if (StringUtils.equalsAnyIgnoreCase(field.getName(), "用例等级")) {
                    List<String> values = getOptionValues(field);
                    returnMap.put("caseLevel", values);
                } else if (StringUtils.equalsAnyIgnoreCase(field.getName(), "用例状态")) {
                    List<String> values = getOptionValues(field);
                    returnMap.put("caseStatus", values);
                }
            }
        }
        return returnMap;
    }

    @NotNull
    private List<String> getOptionValues(CustomFieldDao field) {
        List<CustomFieldOptionDTO> options = JSON.parseArray(field.getOptions(), CustomFieldOptionDTO.class);
        List<String> values = new ArrayList<>();
        if (options != null) {
            for (CustomFieldOptionDTO option : options) {
                values.add(option.getValue());
            }
        }
        return values;
    }

    public TestCaseTemplateDao getTemplate(String projectId) {
        return microService.getForData(serviceName, BASE_UEL + "/get/relate/" + projectId,
                TestCaseTemplateDao.class);
    }

}
