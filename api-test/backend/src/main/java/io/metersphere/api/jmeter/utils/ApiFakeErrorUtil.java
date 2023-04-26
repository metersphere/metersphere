package io.metersphere.api.jmeter.utils;

import io.metersphere.base.domain.ErrorReportLibraryExample;
import io.metersphere.base.domain.ErrorReportLibraryWithBLOBs;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.MsRegexDTO;
import io.metersphere.xpack.fake.error.ErrorReportLibraryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApiFakeErrorUtil {

    private static final String REGEX_CONFIG = "regexConfig";

    public static Map<String, List<MsRegexDTO>> get(List<String> projectIds) {
        Map<String, List<MsRegexDTO>> fakeErrorMap = new HashMap<>();
        if (CollectionUtils.isEmpty(projectIds)) {
            return fakeErrorMap;
        }
        ErrorReportLibraryService service = CommonBeanFactory.getBean(ErrorReportLibraryService.class);
        if (service != null) {
            ErrorReportLibraryExample example = new ErrorReportLibraryExample();
            example.createCriteria().andProjectIdIn(projectIds).andStatusEqualTo(true);
            List<ErrorReportLibraryWithBLOBs> bloBs = service.selectByExampleWithBLOBs(example);
            bloBs.forEach(item -> {
                if (StringUtils.isNotEmpty(item.getContent())) {
                    try {
                        Map<String, Object> assertionMap = JSON.parseObject(item.getContent(), Map.class);
                        if (assertionMap != null) {
                            MsRegexDTO regexConfig = JSON.parseObject(
                                    JSONUtil.toJSONString(assertionMap.get(REGEX_CONFIG)), MsRegexDTO.class);
                            regexConfig.setErrorCode(item.getErrorCode());
                            if (fakeErrorMap.containsKey(item.getProjectId())) {
                                fakeErrorMap.get(item.getProjectId()).add(regexConfig);
                            } else {
                                fakeErrorMap.put(item.getProjectId(), new LinkedList<>() {{
                                    this.add(regexConfig);
                                }});
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.error(e);
                    }
                }
            });
        }
        return fakeErrorMap;
    }
}
