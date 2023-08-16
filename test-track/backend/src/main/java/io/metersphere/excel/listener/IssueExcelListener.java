package io.metersphere.excel.listener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.DateUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
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
import io.metersphere.xpack.track.dto.PlatformStatusDTO;
import io.metersphere.xpack.track.dto.request.IssuesUpdateRequest;
import lombok.Getter;
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
 *
 * @author songcc
 */

public class IssueExcelListener extends AnalysisEventListener<Map<Integer, String>> {

    /**
     * dataClass: EXCEL数据实例class
     * request: 导入参数
     * isThirdPlatform: 是否是第三方平台
     * headMap: excel表头字段集合
     * customFields: 自定义字段集合(模板自定义字段 + 平台自定义字段)
     * issuesService: 业务类
     * memberMap: 成员集合
     * platformStatusList: 平台状态列表
     * excelHeadFieldMap: excel表头字段字典值
     * issueCustomFieldMap: 缺陷自定义字段KeyMap
     */
    private Class<?> dataClass;
    private IssueImportRequest request;
    private Boolean isThirdPlatform;
    private Map<Integer, String> headMap;
    private List<CustomFieldDao> customFields;
    private IssuesService issuesService;
    private Map<String, String> memberMap;
    private List<PlatformStatusDTO> platformStatusList;
    private List<String> tapdUsers;
    private Map<String, String> headFieldTransferDic = new HashMap<>();

    /**
     * 每超过2000条数据, 则插入数据库
     */
    protected static final int BATCH_THRESHOLD = 2000;

    /**
     * insertList: 新增缺陷集合
     * updateList: 覆盖缺陷集合
     * errList: 校验失败错误信息集合
     */
    protected List<IssueExcelData> insertList = new ArrayList<>();
    protected List<IssueExcelData> updateList = new ArrayList<>();
    @Getter
    protected List<ExcelErrData<IssueExcelData>> errList = new ArrayList<>();

    /**
     * 选项值文本数组
     */
    public static final String OPTION_TEXT_ARRAY_PREFIX = "[\"";
    public static final String OPTION_TEXT_ARRAY_SUFFIX = "\"]";

    public IssueExcelListener(IssueImportRequest request, Class<?> clazz, Boolean isThirdPlatform, List<CustomFieldDao> customFields, Map<String, String> memberMap, List<PlatformStatusDTO> platformStatusList, List<String> tapdUsers) {
        this.request = request;
        dataClass = clazz;
        this.isThirdPlatform = isThirdPlatform;
        this.customFields = customFields;
        issuesService = CommonBeanFactory.getBean(IssuesService.class);
        this.memberMap = memberMap;
        this.platformStatusList = platformStatusList;
        this.tapdUsers = tapdUsers;
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext analysisContext) {
        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();
        IssueExcelData issueExcelData = null;
        StringBuilder errMsg;
        try {
            issueExcelData = parseDataToModel(data);
            // EXCEL校验, 如果不是第三方模板则需要校验
            errMsg = new StringBuilder(!isThirdPlatform ? ExcelValidateHelper.validateEntity(issueExcelData) : StringUtils.EMPTY);
            // 校验自定义字段及平台状态及TAPD处理人
            if (StringUtils.isEmpty(errMsg)) {
                validateCustomField(issueExcelData, errMsg);
                validateAndTransferPlatformStatus(issueExcelData, errMsg);
                validateTapdUsers(issueExcelData, errMsg);
            }
        } catch (Exception e) {
            errMsg = new StringBuilder(Translator.get("parse_data_error"));
            LogUtil.error(e.getMessage(), e);
        }

        if (StringUtils.isNotEmpty(errMsg)) {
            ExcelErrData<IssueExcelData> excelErrData = new ExcelErrData<>(issueExcelData, rowIndex,
                    Translator.get("number")
                            .concat(StringUtils.SPACE)
                            .concat(String.valueOf(rowIndex + 1)).concat(StringUtils.SPACE)
                            .concat(Translator.get("row"))
                            .concat(Translator.get("error"))
                            .concat("：")
                            .concat(errMsg.toString()));
            errList.add(excelErrData);
        } else {
            if (issueExcelData == null) {
                return;
            }
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

    /**
     * 保存缺陷数据
     */
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
        genExcelHeadFieldTransferDic();
        formatHeadMap();
        super.invokeHeadMap(headMap, context);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
        insertList.clear();
        updateList.clear();
    }

    /**
     * headMap转换
     */
    private void formatHeadMap() {
        for (Integer key : headMap.keySet()) {
            String name = headMap.get(key);
            if (headFieldTransferDic.containsKey(name)) {
                headMap.put(key, headFieldTransferDic.get(name));
            }
        }
    }

    /**
     * 校验自定义字段
     *
     * @param data   excel数据
     * @param errMsg 错误信息
     */
    public void validateCustomField(IssueExcelData data, StringBuilder errMsg) {
        Map<String, List<CustomFieldDao>> customFieldMap = customFields.stream().collect(Collectors.groupingBy(CustomFieldDao::getName));
        data.getCustomData().forEach((k, v) -> {
            List<CustomFieldDao> customFields = customFieldMap.get(k);
            if (CollectionUtils.isNotEmpty(customFields) && !customFields.isEmpty()) {
                CustomFieldDao customFieldDao = customFields.get(0);
                String type = customFieldDao.getType();
                Boolean required = customFieldDao.getRequired();
                String options = StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.MEMBER.getValue(), CustomFieldType.MULTIPLE_MEMBER.getValue()) ?
                        JSON.toJSONString(memberMap) : customFieldDao.getOptions();
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

    /**
     * 校验并转换平台状态
     *
     * @param data   excel数据
     * @param errMsg 错误信息
     */
    public void validateAndTransferPlatformStatus(IssueExcelData data, StringBuilder errMsg) {
        String platformStatus = data.getPlatformStatus();
        if (StringUtils.isNotEmpty(platformStatus) && CollectionUtils.isNotEmpty(platformStatusList)) {
            Optional<PlatformStatusDTO> first = platformStatusList.stream().filter(status -> StringUtils.equals(status.getLabel(), platformStatus)).findFirst();
            if (first.isPresent()) {
                data.setPlatformStatus(first.get().getValue());
            } else {
                errMsg.append(IssueExportHeadField.PLATFORM_STATUS.getName()).append(Translator.get("options_not_exist")).append(";");
            }
        }
    }

    /**
     * 校验TAPD处理人
     *
     * @param data   excel数据
     * @param errMsg 错误信息
     */
    public void validateTapdUsers(IssueExcelData data, StringBuilder errMsg) {
        List<String> tarTapdUsers = data.getTapdUsers();
        if (CollectionUtils.isNotEmpty(tarTapdUsers) && CollectionUtils.isNotEmpty(tapdUsers)) {
            tarTapdUsers.forEach(tapdUser -> {
                Optional<String> first = tapdUsers.stream().filter(user -> StringUtils.equals(user, tapdUser)).findFirst();
                if (first.isEmpty()) {
                    errMsg.append(Translator.get("tapd_user")).append(Translator.get("options_not_exist")).append(";");
                }
            });
        }
    }

    /**
     * 解析表格数据 -> excel数据
     *
     * @param rowData 表格数据
     * @return excel数据
     */
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
            } else if (StringUtils.equalsAnyIgnoreCase(field, IssueExportHeadField.PLATFORM_STATUS.getName())) {
                data.setPlatformStatus(value);
            } else if (StringUtils.equalsAnyIgnoreCase(field, Translator.get("tapd_user"))) {
                if (StringUtils.isEmpty(value)) {
                    data.setTapdUsers(null);
                } else if (value.contains(",")) {
                    data.setTapdUsers(Arrays.stream(value.split(",")).map(String::trim).collect(Collectors.toList()));
                } else if (value.contains(";")) {
                    data.setTapdUsers(Arrays.stream(value.split(";")).map(String::trim).collect(Collectors.toList()));
                } else {
                    data.setTapdUsers(List.of(value.trim()));
                }
            } else {
                // 自定义字段
                if (StringUtils.isNotEmpty(value) && (value.contains(","))) {
                    // 逗号分隔
                    List<String> dataList = Arrays.stream(value.split(",")).map(String::trim).toList();
                    List<String> formatDataList = dataList.stream().map(item -> "\"" + item + "\"").collect(Collectors.toList());
                    data.getCustomData().put(field, formatDataList);
                } else if (StringUtils.isNotEmpty(value) && (value.contains(";"))) {
                    // 分号分隔
                    List<String> dataList = Arrays.stream(value.split(";")).map(String::trim).toList();
                    List<String> formatDataList = dataList.stream().map(item -> "\"" + item + "\"").collect(Collectors.toList());
                    data.getCustomData().put(field, formatDataList);
                } else {
                    data.getCustomData().put(field, value);
                }
            }
        }
        return data;
    }

    /**
     * excel数据 -> issue请求数据
     *
     * @param issueExcelData excel数据
     * @return issue请求数据
     */
    private IssuesUpdateRequest convertToIssue(IssueExcelData issueExcelData) {
        IssuesUpdateRequest issuesUpdateRequest = new IssuesUpdateRequest();
        issuesUpdateRequest.setTapdUsers(issueExcelData.getTapdUsers());
        issuesUpdateRequest.setWorkspaceId(request.getWorkspaceId());
        issuesUpdateRequest.setProjectId(request.getProjectId());
        issuesUpdateRequest.setThirdPartPlatform(isThirdPlatform);
        issuesUpdateRequest.setDescription(issueExcelData.getDescription());
        issuesUpdateRequest.setTitle(issueExcelData.getTitle());
        issuesUpdateRequest.setPlatformStatus(issueExcelData.getPlatformStatus());
        if (BooleanUtils.isTrue(issueExcelData.getAddFlag())) {
            issuesUpdateRequest.setCreator(SessionUtils.getUserId());
        } else {
            issuesUpdateRequest.setPlatformId(getPlatformId(issueExcelData.getId()));
            issuesUpdateRequest.setId(issueExcelData.getId());
        }
        buildFields(issueExcelData, issuesUpdateRequest);
        return issuesUpdateRequest;
    }

    /**
     * 获取注解ExcelProperty的value和对应field
     */
    public void genExcelHeadFieldTransferDic() {
        Field[] fields = dataClass.getDeclaredFields();
        for (Field field : fields) {
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

    /**
     * 缺陷存在
     *
     * @param num       缺陷ID
     * @param projectId 项目ID
     * @return issues
     */
    private Issues checkIssueExist(Integer num, String projectId) {
        return issuesService.checkIssueExist(num, projectId);
    }

    /**
     * 构建自定义字段
     *
     * @param issueExcelData      excel对象
     * @param issuesUpdateRequest 缺陷请求对象
     */
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
                if (CollectionUtils.isNotEmpty(customFieldDaoList) && !customFieldDaoList.isEmpty()) {
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
                            String parseStr = parseOptionText(customFieldDao.getOptions(), v.toString());
                            customFieldItemDTO.setValue(parseStr);
                            customFieldResourceDTO.setValue(parseStr);
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.SELECT.getValue(),
                                CustomFieldType.RADIO.getValue())) {
                            String parseStr = parseOptionText(customFieldDao.getOptions(), v.toString());
                            customFieldItemDTO.setValue(parseStr);
                            customFieldResourceDTO.setValue("\"" + parseStr + "\"");
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.MULTIPLE_INPUT.getValue())) {
                            if (!v.toString().contains("[")) {
                                v = List.of("\"" + v + "\"");
                            }
                            customFieldItemDTO.setValue(v);
                            customFieldResourceDTO.setValue(v.toString());
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.MULTIPLE_MEMBER.getValue())) {
                            if (!v.toString().contains("[")) {
                                v = List.of("\"" + v + "\"");
                            }
                            customFieldItemDTO.setValue(v.toString());
                            customFieldResourceDTO.setValue(v.toString());
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.CASCADING_SELECT.getValue())) {
                            if (!v.toString().contains("[")) {
                                v = List.of("\"" + v + "\"");
                            }
                            String parseStr = parseCascadingOptionText(customFieldDao.getOptions(), v.toString());
                            customFieldItemDTO.setValue(parseStr);
                            customFieldResourceDTO.setValue(parseStr);
                        } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.DATETIME.getValue())) {
                            Date vDate = DateUtils.parseDate(v.toString(), "yyyy-MM-dd HH:mm:ss");
                            v = DateUtils.format(vDate, "yyyy-MM-dd'T'HH:mm");
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

    /**
     * 校验字段是否为下拉框
     *
     * @param type 字段类型
     * @return boolean
     */
    private Boolean isSelect(String type) {
        return StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.SELECT.getValue(), CustomFieldType.RADIO.getValue(),
                CustomFieldType.MULTIPLE_SELECT.getValue(), CustomFieldType.CHECKBOX.getValue(),
                CustomFieldType.CASCADING_SELECT.getValue(), CustomFieldType.MEMBER.getValue(), CustomFieldType.MULTIPLE_MEMBER.getValue());
    }

    /**
     * 校验字段非法格式
     *
     * @param type  字段类型
     * @param value 字段值
     * @return boolean
     */
    private Boolean isIllegalFormat(String type, Object value) {
        try {
            if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.DATE.getValue())) {
                DateUtils.parseDate(value.toString(), "yyyy-MM-dd");
            } else if (StringUtils.equalsAnyIgnoreCase(type, CustomFieldType.DATETIME.getValue())) {
                DateUtils.parseDate(value.toString(), "yyyy-MM-dd HH:mm:ss");
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

    /**
     * 是否选项中包含该值
     *
     * @param value   值
     * @param options 选项
     * @return boolean
     */
    private Boolean isOptionInclude(Object value, String options) {
        AtomicReference<Boolean> isInclude = new AtomicReference<>(Boolean.TRUE);
        if (value instanceof List) {
            ((List<?>) value).forEach(item -> {
                String s = item.toString().replaceAll("\"", StringUtils.EMPTY);
                if (!StringUtils.contains(options, s)) {
                    isInclude.set(Boolean.FALSE);
                }
            });
        } else {
            isInclude.set(StringUtils.contains(options, value.toString()));
        }
        return isInclude.get();
    }

    /**
     * 是否包含导出字段
     *
     * @param name 导出字段名称
     * @return boolean
     */
    public Boolean exportFieldsContains(String name) {
        for (IssueExportHeadField issueExportHeadField : IssueExportHeadField.values()) {
            if (StringUtils.equals(name, issueExportHeadField.getName())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * 解析选择类型字段 (文本 -> 值)
     *
     * @param options 选项
     * @param tarVal  文本
     * @return 值
     */
    public String parseOptionText(String options, String tarVal) {
        if (StringUtils.isEmpty(options)) {
            return StringUtils.EMPTY;
        }

        List<Map<Object, Object>> optionList = JSON.parseArray(options, new TypeReference<>() {
        });
        if (StringUtils.containsAny(tarVal, OPTION_TEXT_ARRAY_PREFIX, OPTION_TEXT_ARRAY_SUFFIX)) {
            List<String> parseArr = new ArrayList<>();
            List<String> tarArr = JSONArray.parseArray(tarVal, String.class);
            for (Map<Object, Object> option : optionList) {
                String text = option.get("text").toString();
                String value = option.get("value").toString();
                if (tarArr.contains(text)) {
                    parseArr.add("\"" + value + "\"");
                }
            }
            return parseArr.toString();
        } else {
            for (Map<Object, Object> option : optionList) {
                String text = option.get("text").toString();
                String value = option.get("value").toString();
                if (StringUtils.containsIgnoreCase(text, tarVal)) {
                    return value;
                }
            }
            return tarVal;
        }
    }

    /**
     * 解析级联类型字段(文本 -> 值)
     *
     * @param cascadingOption 级联选项
     * @param tarVal          文本
     * @return 值
     */
    public String parseCascadingOptionText(String cascadingOption, String tarVal) {
        List<String> values = new ArrayList<>();
        if (StringUtils.isEmpty(cascadingOption)) {
            return StringUtils.EMPTY;
        }
        JSONArray options = JSONArray.parseArray(cascadingOption);
        JSONArray talVars = JSONArray.parseArray(tarVal);
        if (options.isEmpty() || talVars.isEmpty()) {
            return StringUtils.EMPTY;
        }
        for (Object talVar : talVars) {
            String val = talVar.toString();
            JSONObject jsonOption = findJsonOption(options, val);
            if (jsonOption == null) {
                return StringUtils.EMPTY;
            } else {
                values.add("\"" + jsonOption.get("value").toString() + "\"");
                options = jsonOption.getJSONArray("children");
            }
        }
        return values.toString();
    }

    /**
     * 匹配级联层级options
     *
     * @param options 级联选项
     * @param tarVal  文本
     * @return json对象
     */
    private JSONObject findJsonOption(JSONArray options, String tarVal) {
        if (options.isEmpty()) {
            return null;
        }
        List<JSONObject> jsonObjects = options.stream().map(option -> (JSONObject) option).filter(option -> StringUtils.equals(tarVal, option.get("text").toString())).toList();
        if (jsonObjects.isEmpty()) {
            return null;
        } else {
            return jsonObjects.get(0);
        }
    }
}
