package io.metersphere.excel.listener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.DateUtils;
import com.alibaba.fastjson.JSONArray;
import io.metersphere.base.domain.Issues;
import io.metersphere.commons.constants.CustomFieldType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldItemDTO;
import io.metersphere.dto.CustomFieldResourceDTO;
import io.metersphere.excel.constants.IssueExportHeadField;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.IssueExcelData;
import io.metersphere.excel.domain.IssueExcelDataFactory;
import io.metersphere.excel.utils.ExcelImportType;
import io.metersphere.excel.utils.ExcelValidateHelper;
import io.metersphere.i18n.Translator;
import io.metersphere.request.issues.IssueImportRequest;
import io.metersphere.service.IssuesService;
import io.metersphere.xpack.track.dto.request.IssuesUpdateRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 缺陷导入读取
 * @author songcc
 */

public class IssueExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    private Class dataClass;
    private IssueImportRequest request;
    private Boolean isThirdPlatform;
    private Map<Integer, String> headMap;
    private List<CustomFieldDao> customFields;
    private IssuesService issuesService;
    private Map<String, String> memberMap;
    /**
     * excel表头字段字典值
     */
    private Map<String, String> headFieldTransferDic = new HashMap<>();
    private Map<String, List<CustomFieldResourceDTO>> issueCustomFieldMap = new HashMap<>();

    /**
     * 每超过2000条数据, 则插入数据库
     */
    protected static final int BATCH_THRESHOLD = 2000;

    /**
     * insertList: 新增缺陷集合
     * updateList: 覆盖缺陷集合
     * errList: 校验失败缺陷集合
     */
    protected List<IssueExcelData> insertList = new ArrayList<>();
    protected List<IssueExcelData> updateList = new ArrayList<>();
    protected List<ExcelErrData<IssueExcelData>> errList = new ArrayList<>();

    public IssueExcelListener(IssueImportRequest request, Class clazz, Boolean isThirdPlatform, List<CustomFieldDao> customFields, Map<String, String> memberMap) {
        this.request = request;
        this.dataClass = clazz;
        this.isThirdPlatform = isThirdPlatform;
        this.customFields = customFields;
        this.issuesService = CommonBeanFactory.getBean(IssuesService.class);
        this.memberMap = memberMap;
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext analysisContext) {
        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();
        IssueExcelData issueExcelData = null;
        StringBuilder errMsg;
        try {
            issueExcelData = this.parseDataToModel(data);
            // EXCEL校验, 如果不是第三方模板则需要校验
            errMsg = new StringBuilder(!isThirdPlatform ? ExcelValidateHelper.validateEntity(issueExcelData) : StringUtils.EMPTY);
            // 校验自定义字段
            if (StringUtils.isEmpty(errMsg)) {
                validateCustomField(issueExcelData, errMsg);
            }
        } catch (Exception e) {
            errMsg = new StringBuilder(Translator.get("parse_data_error"));
            LogUtil.error(e.getMessage(), e);
        }

        if (StringUtils.isNotEmpty(errMsg)) {
            ExcelErrData excelErrData = new ExcelErrData(issueExcelData, rowIndex,
                    Translator.get("number")
                            .concat(StringUtils.SPACE)
                            .concat(String.valueOf(rowIndex + 1)).concat(StringUtils.SPACE)
                            .concat(Translator.get("row"))
                            .concat(Translator.get("error"))
                            .concat("：")
                            .concat(errMsg.toString()));
            errList.add(excelErrData);
        } else {
            if (issueExcelData.getNum() == null) {
                // ID为空或不存在, 新增
                issueExcelData.setAddFlag(Boolean.TRUE);
                insertList.add(issueExcelData);
            } else {
                Issues issues = checkIssueExist(issueExcelData.getNum(), request.getProjectId());
                if (issues == null) {
                    // ID列值不存在, 则新增
                    issueExcelData.setAddFlag(Boolean.TRUE);
                    insertList.add(issueExcelData);
                } else {
                    // ID存在
                    if (StringUtils.equals(request.getImportType(), ExcelImportType.Update.name())) {
                        // 覆盖模式
                        issueExcelData.setId(issues.getId());
                        issueExcelData.setAddFlag(Boolean.FALSE);
                        updateList.add(issueExcelData);
                    } else {
                        // 不覆盖模式
                        issueExcelData.setId(issues.getId());
                        issueExcelData.setAddFlag(Boolean.FALSE);
                        issueExcelData.setUpdateFlag(Boolean.FALSE);
                        updateList.add(issueExcelData);
                    }
                }
            }
        }
        if (insertList.size() > BATCH_THRESHOLD || updateList.size() > BATCH_THRESHOLD) {
            saveData();
            insertList.clear();
            updateList.clear();
        }
    }

    public void saveData() {
        //excel中用例都有错误时就返回，只要有用例可用于更新或者插入就不返回
        if (!errList.isEmpty()) {
            return;
        }

        if (CollectionUtils.isEmpty(insertList) && CollectionUtils.isEmpty(updateList)) {
            MSException.throwException(Translator.get("no_legitimate_issue_tip"));
        }

        if (CollectionUtils.isNotEmpty(insertList)) {
            List<IssuesUpdateRequest> issues = insertList.stream().map(this::convertToIssue).collect(Collectors.toList());
            issuesService.saveImportData(issues);
        }

        if (CollectionUtils.isNotEmpty(updateList)) {
            List<IssuesUpdateRequest> issues = updateList.stream().filter(IssueExcelData::getUpdateFlag).map(this::convertToIssue).collect(Collectors.toList());
            issuesService.updateImportData(issues);
        }
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
        this.genExcelHeadFieldTransferDic();
        this.formatHeadMap();
        super.invokeHeadMap(headMap, context);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
        insertList.clear();
        updateList.clear();
        issueCustomFieldMap.clear();
    }

    private void formatHeadMap() {
        for (Integer key : headMap.keySet()) {
            String name = headMap.get(key);
            if (headFieldTransferDic.containsKey(name)) {
                headMap.put(key, headFieldTransferDic.get(name));
            }
        }
    }

    public void validateCustomField(IssueExcelData data, StringBuilder errMsg) {
        Map<String, List<CustomFieldDao>> customFieldMap = customFields.stream().collect(Collectors.groupingBy(CustomFieldDao::getName));
        data.getCustomData().forEach((k, v) -> {
            List<CustomFieldDao> customFieldDaos = customFieldMap.get(k);
            if (CollectionUtils.isNotEmpty(customFieldDaos) && customFieldDaos.size() > 0) {
                CustomFieldDao customFieldDao = customFieldDaos.get(0);
                String type = customFieldDao.getType();
                Boolean required = customFieldDao.getRequired();
                String options = StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.MEMBER.getValue(), CustomFieldType.MULTIPLE_MEMBER.getValue()) ?
                        JSON.toJSONString(this.memberMap) : customFieldDao.getOptions();
                if (required && StringUtils.isEmpty(v.toString())) {
                    errMsg.append(k).append(Translator.get("can_not_be_null")).append(";");
                } else if (StringUtils.isNotEmpty(v.toString()) && isIllegalFormat(type, v)) {
                    errMsg.append(k).append(Translator.get("format_error")).append(";");
                } else if (StringUtils.isNotEmpty(v.toString()) && isSelect(type) && !isOptionInclude(v, options)) {
                    errMsg.append(k).append(Translator.get("options_not_exist")).append(";");
                }
            } else {
                if (!exportFieldsContains(k)) {
                    errMsg.append(k).append(Translator.get("excel_field_not_exist")).append(";");
                }
            }
        });
    }

    private IssueExcelData parseDataToModel(Map<Integer, String> rowData) {
        IssueExcelData data = new IssueExcelDataFactory().getIssueExcelDataLocal();
        for (Map.Entry<Integer, String> headEntry : headMap.entrySet()) {
            Integer index = headEntry.getKey();
            String field = headEntry.getValue();
            if (StringUtils.isBlank(field)) {
                continue;
            }
            String value = StringUtils.isEmpty(rowData.get(index)) ? StringUtils.EMPTY : rowData.get(index);

            if (StringUtils.equalsIgnoreCase(field, IssueExportHeadField.ID.getName())) {
                data.setNum(StringUtils.isEmpty(value) ? null : Integer.parseInt(value));
            } else if (StringUtils.equalsAnyIgnoreCase(field, IssueExportHeadField.TITLE.getId())) {
                data.setTitle(value);
            } else if (StringUtils.equalsAnyIgnoreCase(field, IssueExportHeadField.DESCRIPTION.getId())) {
                data.setDescription(value);
            } else {
                // 自定义字段
                if (StringUtils.isNotEmpty(value) && (value.contains(","))) {
                    // 逗号分隔
                    List<String> dataList = Arrays.asList(org.springframework.util.StringUtils.trimAllWhitespace(value).split(","));
                    List<String> formatDataList = dataList.stream().map(item -> "\"" + item + "\"").collect(Collectors.toList());
                    data.getCustomData().put(field, formatDataList);
                } else if (StringUtils.isNotEmpty(value) && (value.contains(";"))){
                    // 分号分隔
                    List<String> dataList = Arrays.asList(org.springframework.util.StringUtils.trimAllWhitespace(value).split(";"));
                    List<String> formatDataList = dataList.stream().map(item -> "\"" + item + "\"").collect(Collectors.toList());
                    data.getCustomData().put(field, formatDataList);
                } else {
                    data.getCustomData().put(field, value);
                }
            }
        }
        return data;
    }

    private IssuesUpdateRequest convertToIssue(IssueExcelData issueExcelData) {
        IssuesUpdateRequest issuesUpdateRequest = new IssuesUpdateRequest();
        issuesUpdateRequest.setWorkspaceId(request.getWorkspaceId());
        issuesUpdateRequest.setProjectId(request.getProjectId());
        issuesUpdateRequest.setThirdPartPlatform(isThirdPlatform);
        issuesUpdateRequest.setDescription(issueExcelData.getDescription());
        issuesUpdateRequest.setTitle(issueExcelData.getTitle());
        if (BooleanUtils.isTrue(issueExcelData.getAddFlag())) {
            issuesUpdateRequest.setCreator(SessionUtils.getUserId());
        } else {
            issuesUpdateRequest.setPlatformId(getPlatformId(issueExcelData.getId()));
            issuesUpdateRequest.setId(issueExcelData.getId());
        }
        buildFields(issueExcelData, issuesUpdateRequest);
        return issuesUpdateRequest;
    }

    public List<ExcelErrData<IssueExcelData>> getErrList() {
        return this.errList;
    }

    /**
     * 获取注解ExcelProperty的value和对应field
     */
    public void genExcelHeadFieldTransferDic() {
        Field[] fields = dataClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty != null) {
                StringBuilder value = new StringBuilder();
                for (String v : excelProperty.value()) {
                    value.append(v);
                }
                headFieldTransferDic.put(value.toString(), field.getName());
            }
        }
    }

    private Issues checkIssueExist(Integer num, String projectId) {
        return issuesService.checkIssueExist(num, projectId);
    }

    private void buildFields(IssueExcelData issueExcelData, IssuesUpdateRequest issuesUpdateRequest) {
        if (MapUtils.isEmpty(issueExcelData.getCustomData())) {
            return;
        }
        Boolean addFlag = issueExcelData.getAddFlag();
        List<CustomFieldResourceDTO> addFields = new ArrayList<>();
        List<CustomFieldResourceDTO> editFields = new ArrayList<>();
        List<CustomFieldItemDTO> requestFields = new ArrayList<>();
        Map<String, List<CustomFieldDao>> customFieldMap = customFields.stream().collect(Collectors.groupingBy(CustomFieldDao::getName));
        issueExcelData.getCustomData().forEach((k, v) -> {
            try {
                List<CustomFieldDao> customFieldDaoList = customFieldMap.get(k);
                if (CollectionUtils.isNotEmpty(customFieldDaoList) && customFieldDaoList.size() > 0) {
                    CustomFieldDao customFieldDao = customFieldDaoList.get(0);
                    String type = customFieldDao.getType();
                    // add field
                    CustomFieldResourceDTO customFieldResourceDTO = new CustomFieldResourceDTO();
                    customFieldResourceDTO.setFieldId(customFieldDao.getId());
                    // request field
                    CustomFieldItemDTO customFieldItemDTO = new CustomFieldItemDTO();
                    BeanUtils.copyBean(customFieldItemDTO, customFieldDao);
                    if (StringUtils.isEmpty(v.toString())) {
                        if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.MULTIPLE_MEMBER.getValue(),
                                CustomFieldType.MULTIPLE_SELECT.getValue(), CustomFieldType.CHECKBOX.getValue(),
                                CustomFieldType.CASCADING_SELECT.getValue())) {
                            customFieldItemDTO.setValue("[]");
                            customFieldResourceDTO.setValue("[]");
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.MULTIPLE_INPUT.getValue())) {
                            customFieldItemDTO.setValue(Collections.emptyList());
                            customFieldResourceDTO.setValue("[]");
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.RADIO.getValue(),
                                CustomFieldType.RICH_TEXT.getValue(), CustomFieldType.SELECT.getValue(),
                                CustomFieldType.FLOAT.getValue(), CustomFieldType.DATE.getValue(),
                                CustomFieldType.DATETIME.getValue(), CustomFieldType.INPUT.getValue())) {
                            customFieldItemDTO.setValue(StringUtils.EMPTY);
                            customFieldResourceDTO.setValue(StringUtils.EMPTY);
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.TEXTAREA.getValue())) {
                            customFieldItemDTO.setValue(StringUtils.EMPTY);
                        }
                    } else {
                        if (StringUtils.equalsAnyIgnoreCase(type,
                                CustomFieldType.RICH_TEXT.getValue(), CustomFieldType.TEXTAREA.getValue())) {
                            customFieldItemDTO.setValue(v.toString());
                            customFieldResourceDTO.setTextValue(v.toString());
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.FLOAT.getValue())) {
                            customFieldItemDTO.setValue(Float.parseFloat(v.toString()));
                            customFieldResourceDTO.setValue(v.toString());
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.MULTIPLE_SELECT.getValue(),
                                CustomFieldType.CHECKBOX.getValue())) {
                            if (!v.toString().contains("[")) {
                                v = List.of("\"" + v + "\"");
                            }
                            customFieldItemDTO.setValue(parseOptionText(customFieldDao.getOptions(), v.toString()));
                            customFieldResourceDTO.setValue(parseOptionText(customFieldDao.getOptions(), v.toString()));
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.SELECT.getValue(),
                                CustomFieldType.RADIO.getValue())) {
                            customFieldItemDTO.setValue(parseOptionText(customFieldDao.getOptions(), v.toString()));
                            customFieldResourceDTO.setValue("\"" + parseOptionText(customFieldDao.getOptions(), v.toString()) + "\"");
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.MULTIPLE_INPUT.getValue())) {
                            if (!v.toString().contains("[")) {
                                v = List.of("\"" + v + "\"");
                            }
                            customFieldItemDTO.setValue(v);
                            customFieldResourceDTO.setValue(v.toString());
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.MULTIPLE_MEMBER.getValue(),
                                CustomFieldType.CASCADING_SELECT.getValue())) {
                            if (!v.toString().contains("[")) {
                                v = List.of("\"" + v + "\"");
                            }
                            customFieldItemDTO.setValue(v.toString());
                            customFieldResourceDTO.setValue(v.toString());
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.DATE.getValue())) {
                            Date vdate = DateUtils.parseDate(v.toString(), "yyyy/MM/dd");
                            v = DateUtils.format(vdate, "yyyy-MM-dd");
                            customFieldItemDTO.setValue(v.toString());
                            customFieldResourceDTO.setValue("\"" + v + "\"");
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.DATETIME.getValue())) {
                            Date vdate = DateUtils.parseDate(v.toString());
                            v =  DateUtils.format(vdate, "yyyy-MM-dd'T'HH:mm");
                            customFieldItemDTO.setValue(v.toString());
                            customFieldResourceDTO.setValue("\"" + v + "\"");
                        } else {
                            customFieldItemDTO.setValue(v.toString());
                            customFieldResourceDTO.setValue("\"" + v + "\"");
                        }
                    }
                    if (addFlag) {
                        addFields.add(customFieldResourceDTO);
                    } else {
                        editFields.add(customFieldResourceDTO);
                    }
                    requestFields.add(customFieldItemDTO);
                }
            } catch (Exception e) {
                MSException.throwException(e.getMessage());
            }
        });
        if (addFlag) {
            issuesUpdateRequest.setAddFields(addFields);
        } else {
            issuesUpdateRequest.setEditFields(editFields);
        }
        issuesUpdateRequest.setRequestFields(requestFields);
    }

    private String getPlatformId(String issueId) {
        return issuesService.getIssue(issueId).getPlatformId();
    }

    private Boolean isSelect(String type) {
        return StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.SELECT.getValue(), CustomFieldType.RADIO.getValue(),
                CustomFieldType.MULTIPLE_SELECT.getValue(), CustomFieldType.CHECKBOX.getValue(),
                CustomFieldType.CASCADING_SELECT.getValue(), CustomFieldType.MEMBER.getValue(), CustomFieldType.MULTIPLE_MEMBER.getValue());
    }

    private Boolean isIllegalFormat(String type, Object value) {
        try {
            if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.DATE.getValue())) {
                DateUtils.parseDate(value.toString(), "yyyy/MM/dd");
            } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.DATETIME.getValue())) {
                DateUtils.parseDate(value.toString());
            } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.INT.getValue())) {
                Integer.parseInt(value.toString());
            } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.FLOAT.getValue())) {
                Float.parseFloat(value.toString());
            } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.SELECT.getValue(), CustomFieldType.RADIO.getValue(), CustomFieldType.MEMBER.getValue())) {
                if (value instanceof List) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        } catch (Exception e) {
            return Boolean.TRUE;
        }
    }

    private Boolean isOptionInclude(Object value, String options) {
        AtomicReference<Boolean> isInclude = new AtomicReference<>(Boolean.TRUE);
        if (value instanceof List) {
            ((List<?>) value).forEach(item -> {
                String s = item.toString().replaceAll("\"", StringUtils.EMPTY);
                if (!StringUtils.contains(options, s))  {
                    isInclude.set(Boolean.FALSE);
                }
            });
        } else {
            isInclude.set(StringUtils.contains(options, "\"" + value.toString() + "\""));
        }
        return isInclude.get();
    }

    public Boolean exportFieldsContains(String name) {
        for (IssueExportHeadField issueExportHeadField : IssueExportHeadField.values()) {
            if (StringUtils.equals(name, issueExportHeadField.getName())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public String parseOptionText(String options, String tarVal) {
        if (StringUtils.isEmpty(options)) {
            return StringUtils.EMPTY;
        }

        List<Map> optionList = JSON.parseArray(options, Map.class);
        if (StringUtils.containsAny(tarVal, "[", "]")) {
            List<String> parseArr = new ArrayList<>();
            List<String> tarArr = JSONArray.parseArray(tarVal, String.class);
            for (Map option : optionList) {
                String text = option.get("text").toString();
                String value = option.get("value").toString();
                if (tarArr.contains(text)) {
                    parseArr.add("\"" + value + "\"");
                }
            }
            return parseArr.toString();
        } else {
            tarVal = tarVal + ",";
            for (Map option : optionList) {
                String text = option.get("text").toString();
                String value = option.get("value").toString();
                if (StringUtils.containsIgnoreCase(tarVal, text + ",")) {
                    tarVal = tarVal.replaceAll(text, value);
                }
            }
            return tarVal.substring(0, tarVal.length() - 1);
        }
    }
}
