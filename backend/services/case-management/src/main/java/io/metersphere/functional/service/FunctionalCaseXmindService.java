package io.metersphere.functional.service;

import io.metersphere.functional.xmind.domain.FunctionalCaseXmindData;
import io.metersphere.functional.xmind.utils.XmindExportUtil;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseXmindService {

    public static final String template = "/template/template.json";

    @Resource
    private FunctionalCaseFileService functionalCaseFileService;


    public void downloadXmindTemplate(String projectId, HttpServletResponse response) {
        List<TemplateCustomFieldDTO> customFields = functionalCaseFileService.getCustomFields(projectId);
        //默认字段+自定义字段的 options集合
        Map<String, List<String>> customFieldOptionsMap = functionalCaseFileService.getCustomFieldOptionsMap(customFields);
        try (InputStream stream = FunctionalCaseXmindService.class.getResourceAsStream(template)) {
            FunctionalCaseXmindData functionalCaseXmindData = JSON.parseObject(stream, FunctionalCaseXmindData.class);
            setTemplateCustomFields(functionalCaseXmindData.getChildren(), customFields);

            XmindExportUtil.downloadTemplate(response, functionalCaseXmindData, true, customFieldOptionsMap);

        } catch (Exception e) {
            LogUtils.error(e.getMessage());
            throw new MSException(Translator.get("download_template_failed"));
        }
    }

    private void setTemplateCustomFields(List<FunctionalCaseXmindData> children, List<TemplateCustomFieldDTO> customFields) {
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(data -> {
                data.getFunctionalCaseList().forEach(item -> {
                    item.setTemplateCustomFieldDTOList(customFields);
                });
                if (CollectionUtils.isNotEmpty(data.getChildren())) {
                    setTemplateCustomFields(data.getChildren(), customFields);
                }
            });
        }
    }


}
