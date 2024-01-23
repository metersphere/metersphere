package io.metersphere.functional.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import io.metersphere.functional.dto.response.FunctionalCaseImportResponse;
import io.metersphere.functional.excel.constants.FunctionalCaseImportFiled;
import io.metersphere.functional.excel.domain.ExcelMergeInfo;
import io.metersphere.functional.excel.domain.FunctionalCaseExcelData;
import io.metersphere.functional.excel.domain.FunctionalCaseExcelDataFactory;
import io.metersphere.functional.excel.handler.FunctionCaseTemplateWriteHandler;
import io.metersphere.functional.excel.listener.FunctionalCaseCheckEventListener;
import io.metersphere.functional.excel.listener.FunctionalCaseImportEventListener;
import io.metersphere.functional.excel.listener.FunctionalCasePretreatmentListener;
import io.metersphere.functional.request.FunctionalCaseImportRequest;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.excel.utils.EasyExcelExporter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wx
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class FunctionalCaseFileService {


    @Resource
    private ProjectTemplateService projectTemplateService;
    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;


    /**
     * 下载excel导入模板
     *
     * @param projectId
     * @param response
     */
    public void downloadExcelTemplate(String projectId, HttpServletResponse response) {
        //获取当前项目下默认模板的自定义字段属性
        List<TemplateCustomFieldDTO> customFields = getCustomFields(projectId);

        //获取表头字段 当前项目下默认模板的自定义字段  heads:默认表头名称+自定义字段名称
        List<List<String>> heads = getTemplateHead(projectId, customFields);

        FunctionalCaseExcelData caseExcelData = new FunctionalCaseExcelDataFactory().getFunctionalCaseExcelDataLocal();

        //默认字段+自定义字段的 options集合
        Map<String, List<String>> customFieldOptionsMap = getCustomFieldOptionsMap(customFields);
        Map<String, TemplateCustomFieldDTO> customFieldMap = customFields.stream().collect(Collectors.toMap(TemplateCustomFieldDTO::getFieldName, templateCustomFieldDTO -> templateCustomFieldDTO));

        //表头备注信息
        FunctionCaseTemplateWriteHandler handler = new FunctionCaseTemplateWriteHandler(heads, customFieldOptionsMap, customFieldMap);

        List<FunctionalCaseExcelData> functionalCaseExcelData = generateExportData();
        List<List<Object>> data = parseExcelData2List(heads, functionalCaseExcelData);

        new EasyExcelExporter(caseExcelData.getClass())
                .exportByCustomWriteHandler(response, heads, data, Translator.get("test_case_import_template_name"),
                        Translator.get("test_case_import_template_sheet"), handler);
    }


    private List<List<Object>> parseExcelData2List(List<List<String>> headListParams, List<FunctionalCaseExcelData> data) {
        List<List<Object>> result = new ArrayList<>();
        //转化excel头
        List<String> headList = new ArrayList<>();
        for (List<String> list : headListParams) {
            for (String head : list) {
                headList.add(head);
            }
        }

        FunctionalCaseImportFiled[] importFields = FunctionalCaseImportFiled.values();


        for (FunctionalCaseExcelData model : data) {
            List<Object> fields = new ArrayList<>();
            Map<String, Object> customDataMaps = Optional.ofNullable(model.getCustomData())
                    .orElse(new HashMap<>());
            Map<String, String> otherFieldMaps = Optional.ofNullable(model.getOtherFields())
                    .orElse(new HashMap<>());
            for (String head : headList) {
                boolean isSystemField = false;
                for (FunctionalCaseImportFiled importFiled : importFields) {
                    if (importFiled.containsHead(head)) {
                        fields.add(importFiled.parseExcelDataValue(model));
                        isSystemField = true;
                    }
                }
                if (!isSystemField) {
                    Object value = customDataMaps.get(head);
                    if (value == null) {
                        value = otherFieldMaps.get(head);
                    }
                    if (value == null) {
                        value = StringUtils.EMPTY;
                    }
                    fields.add(value);
                }
            }
            result.add(fields);
        }

        return result;
    }


    private List<FunctionalCaseExcelData> generateExportData() {
        List<FunctionalCaseExcelData> list = new ArrayList<>();
        StringBuilder path = new StringBuilder();
        for (int i = 1; i <= 4; i++) {
            path.append("/" + Translator.get("module") + i);
            FunctionalCaseExcelData testCaseDTO = new FunctionalCaseExcelData();
            testCaseDTO.setNum(StringUtils.EMPTY);
            testCaseDTO.setName(Translator.get("test_case") + i);
            testCaseDTO.setModule(path.toString());
            testCaseDTO.setPrerequisite(Translator.get("test_case_prerequisite"));
            testCaseDTO.setCaseEditType("STEP");
            String textDescription = "";
            String expectedResult = "";
            for (int j = 1; j < 5; j++) {
                textDescription = textDescription + "[" + j + "]" + Translator.get("test_case_step_desc") + i + "\n";

                expectedResult = expectedResult + "[" + j + "]" + Translator.get("test_case_step_result") + i + "\n";

            }
            testCaseDTO.setTextDescription(textDescription);
            testCaseDTO.setExpectedResult(expectedResult);

            list.add(testCaseDTO);
        }
        return list;
    }

    private Map<String, List<String>> getCustomFieldOptionsMap(List<TemplateCustomFieldDTO> customFields) {
        Map<String, List<String>> returnMap = new HashMap<>();
        customFields.forEach(item -> {
            List<String> values = getOptionValues(Optional.ofNullable(item.getOptions()).orElse(new ArrayList<>()));
            returnMap.put(item.getFieldName(), values);
        });
        return returnMap;
    }

    private List<String> getOptionValues(List<CustomFieldOption> options) {
        List<String> values = new ArrayList<>();
        options.forEach(item -> {
            values.add(item.getText());
        });
        return values;
    }

    /**
     * 获取表头字段
     *
     * @param projectId
     * @param customFields
     * @return
     */
    private List<List<String>> getTemplateHead(String projectId, List<TemplateCustomFieldDTO> customFields) {
        List<List<String>> heads = new FunctionalCaseExcelDataFactory().getFunctionalCaseExcelDataLocal().getHead(customFields);
        return heads;
    }


    /**
     * 导入前校验excel模板
     *
     * @param request
     * @param file
     * @return
     */
    public FunctionalCaseImportResponse preCheckExcel(FunctionalCaseImportRequest request, MultipartFile file) {
        if (file == null) {
            throw new MSException(Translator.get("file_cannot_be_null"));
        }
        FunctionalCaseImportResponse response = new FunctionalCaseImportResponse();
        checkImportExcel(response, request, file);
        return response;
    }

    private void checkImportExcel(FunctionalCaseImportResponse response, FunctionalCaseImportRequest request, MultipartFile file) {
        try {
            //根据本地语言环境选择用哪种数据对象进行存放读取的数据
            Class clazz = new FunctionalCaseExcelDataFactory().getExcelDataByLocal();
            //获取当前项目默认模板的自定义字段
            List<TemplateCustomFieldDTO> customFields = getCustomFields(request.getProjectId());
            Set<ExcelMergeInfo> mergeInfoSet = new TreeSet<>();
            // 预处理，查询合并单元格信息
            EasyExcel.read(file.getInputStream(), null, new FunctionalCasePretreatmentListener(mergeInfoSet))
                    .extraRead(CellExtraTypeEnum.MERGE).sheet().doRead();
            FunctionalCaseCheckEventListener eventListener = new FunctionalCaseCheckEventListener(request, clazz, customFields, mergeInfoSet);
            EasyExcelFactory.read(file.getInputStream(), eventListener).sheet().doRead();
            response.setErrorMessages(eventListener.getErrList());
            response.setSuccessCount(eventListener.getList().size());
            response.setFailCount(eventListener.getErrList().size());
        } catch (Exception e) {
            LogUtils.error("checkImportExcel error", e);
            throw new MSException(Translator.get("check_import_excel_error"));
        }
    }

    private List<TemplateCustomFieldDTO> getCustomFields(String projectId) {
        TemplateDTO defaultTemplateDTO = projectTemplateService.getDefaultTemplateDTO(projectId, TemplateScene.FUNCTIONAL.name());
        List<TemplateCustomFieldDTO> customFields = Optional.ofNullable(defaultTemplateDTO.getCustomFields()).orElse(new ArrayList<>());
        return customFields;
    }


    /**
     * 导入excel
     *
     * @param request
     * @param userId
     * @param file
     */
    public FunctionalCaseImportResponse importExcel(FunctionalCaseImportRequest request, String userId, MultipartFile file) {
        if (file == null) {
            throw new MSException(Translator.get("file_cannot_be_null"));
        }
        try {
            FunctionalCaseImportResponse response = new FunctionalCaseImportResponse();
            //设置默认版本
            if (StringUtils.isEmpty(request.getVersionId())) {
                request.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
            }
            //根据本地语言环境选择用哪种数据对象进行存放读取的数据
            Class clazz = new FunctionalCaseExcelDataFactory().getExcelDataByLocal();
            //获取当前项目默认模板的自定义字段
            List<TemplateCustomFieldDTO> customFields = getCustomFields(request.getProjectId());
            Set<ExcelMergeInfo> mergeInfoSet = new TreeSet<>();
            // 预处理，查询合并单元格信息
            EasyExcel.read(file.getInputStream(), null, new FunctionalCasePretreatmentListener(mergeInfoSet))
                    .extraRead(CellExtraTypeEnum.MERGE).sheet().doRead();
            FunctionalCaseImportEventListener eventListener = new FunctionalCaseImportEventListener(request, clazz, customFields, mergeInfoSet, userId);
            EasyExcelFactory.read(file.getInputStream(), eventListener).sheet().doRead();
            response.setErrorMessages(eventListener.getErrList());
            response.setSuccessCount(eventListener.getSuccessCount());
            response.setFailCount(eventListener.getErrList().size());
            return response;
        } catch (Exception e) {
            LogUtils.error("checkImportExcel error", e);
            throw new MSException(Translator.get("check_import_excel_error"));
        }
    }
}
