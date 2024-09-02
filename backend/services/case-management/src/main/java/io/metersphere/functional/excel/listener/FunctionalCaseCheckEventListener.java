package io.metersphere.functional.excel.listener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import io.metersphere.functional.constants.FunctionalCaseTypeConstants;
import io.metersphere.functional.excel.annotation.NotRequired;
import io.metersphere.functional.excel.domain.ExcelMergeInfo;
import io.metersphere.functional.excel.domain.FunctionalCaseExcelData;
import io.metersphere.functional.excel.domain.FunctionalCaseExcelDataFactory;
import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.functional.excel.validate.AbstractCustomFieldValidator;
import io.metersphere.functional.excel.validate.CustomFieldValidatorFactory;
import io.metersphere.functional.request.FunctionalCaseImportRequest;
import io.metersphere.functional.service.FunctionalCaseService;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.excel.FunctionalCaseExcelValidateHelper;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.excel.domain.ExcelErrData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wx
 */
public class FunctionalCaseCheckEventListener extends AnalysisEventListener<Map<Integer, String>> {

    private Class excelDataClass;
    private Map<Integer, String> headMap;
    private Map<String, TemplateCustomFieldDTO> customFieldsMap = new HashMap<>();
    private Set<ExcelMergeInfo> mergeInfoSet;
    private Map<String, String> excelHeadToFieldNameDic = new HashMap<>();
    /**
     * 标记下当前遍历的行是不是有合并单元格
     */
    private Boolean isMergeRow;
    /**
     * 标记下当前遍历的行是不是合并单元格的最后一行
     */
    private Boolean isMergeLastRow;
    /**
     * 存储合并单元格对应的数据，key 为重写了 compareTo 的 ExcelMergeInfo
     */
    private HashMap<ExcelMergeInfo, String> mergeCellDataMap = new HashMap<>();
    /**
     * 存储当前合并的一条完整数据，其中步骤没有合并是多行
     */
    private FunctionalCaseExcelData currentMergeData;
    private Integer firstMergeRowIndex;
    protected List<FunctionalCaseExcelData> list = new ArrayList<>();
    protected List<ExcelErrData<FunctionalCaseExcelData>> errList = new ArrayList<>();
    private static final String ERROR_MSG_SEPARATOR = ";";
    private HashMap<String, AbstractCustomFieldValidator> customFieldValidatorMap;
    protected static final int TAGS_COUNT = 10;
    protected static final int TAG_LENGTH = 64;
    protected static final int STEP_LENGTH = 1000;
    private FunctionalCaseService functionalCaseService;

    public FunctionalCaseCheckEventListener(FunctionalCaseImportRequest request, Class clazz, List<TemplateCustomFieldDTO> customFields, Set<ExcelMergeInfo> mergeInfoSet) {
        this.mergeInfoSet = mergeInfoSet;
        excelDataClass = clazz;
        //当前项目模板的自定义字段
        customFieldsMap = customFields.stream().collect(Collectors.toMap(TemplateCustomFieldDTO::getFieldName, i -> i));
        customFieldValidatorMap = CustomFieldValidatorFactory.getValidatorMap(request.getProjectId());
        functionalCaseService = CommonBeanFactory.getBean(FunctionalCaseService.class);

    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
        try {
            genExcelHeadToFieldNameDicAndGetNotRequiredFields();
        } catch (NoSuchFieldException e) {
            LogUtils.error(e);
        }
        formatHeadMap();
        super.invokeHeadMap(headMap, context);
    }


    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext analysisContext) {
        if (headMap == null) {
            throw new MSException(Translator.get("case_import_table_header_missing"));
        }
        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();
        //处理合并单元格
        handleMergeData(data, rowIndex);
        FunctionalCaseExcelData functionalCaseExcelData;
        // 读取名称列，如果该列是合并单元格，则读取多行数据后合并步骤
        if (isMergeRow) {
            if (currentMergeData == null) {
                firstMergeRowIndex = rowIndex;
                // 如果是合并单元格的首行
                functionalCaseExcelData = parseDataToModel(data);
                //合并文本描述
                functionalCaseExcelData.setMergeTextDescription(new ArrayList<>() {
                    @Serial
                    private static final long serialVersionUID = -2563948462432733672L;

                    {
                        add(functionalCaseExcelData.getTextDescription());
                    }
                });
                //合并
                functionalCaseExcelData.setMergeExpectedResult(new ArrayList<>() {
                    @Serial
                    private static final long serialVersionUID = 8985001651375529701L;

                    {
                        add(functionalCaseExcelData.getExpectedResult());
                    }
                });
                // 记录下数据并返回
                currentMergeData = functionalCaseExcelData;
                if (!isMergeLastRow) {
                    return;
                } else {
                    currentMergeData = null;
                }
            } else {
                // 获取存储的数据，并添加多个步骤
                currentMergeData.getMergeTextDescription()
                        .add(data.get(getTextDescriptionColIndex()));
                currentMergeData.getMergeExpectedResult()
                        .add(data.get(getExpectedResultColIndex()));
                // 是最后一行的合并单元格，保存并清空 currentMergeData，走之后的逻辑
                if (isMergeLastRow) {
                    functionalCaseExcelData = currentMergeData;
                    currentMergeData = null;
                } else {
                    return;
                }
            }
        } else {
            firstMergeRowIndex = null;
            functionalCaseExcelData = parseDataToModel(data);
        }
        //校验数据
        buildUpdateOrErrorList(rowIndex, functionalCaseExcelData);
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 如果文件最后一行是没有内容的步骤，这里处理最后一条合并单元格的数据
        if (currentMergeData != null) {
            buildUpdateOrErrorList(firstMergeRowIndex, currentMergeData);
        }
    }


    /**
     * 构建数据
     *
     * @param rowIndex
     * @param functionalCaseExcelData
     */
    private void buildUpdateOrErrorList(Integer rowIndex, FunctionalCaseExcelData functionalCaseExcelData) {
        StringBuilder errMsg;
        try {
            //根据excel数据实体中的javax.validation + 正则表达式来校验excel数据
            errMsg = new StringBuilder(FunctionalCaseExcelValidateHelper.validateEntity(functionalCaseExcelData));
            //自定义校验规则
            if (StringUtils.isEmpty(errMsg)) {
                //开始校验
                validate(functionalCaseExcelData, errMsg);
            }
        } catch (NoSuchFieldException e) {
            errMsg = new StringBuilder(Translator.get("parse_data_error"));
            LogUtils.error(e.getMessage(), e);
        }

        if (StringUtils.isNotEmpty(errMsg)) {
            Integer errorRowIndex = rowIndex;
            if (firstMergeRowIndex != null) {
                errorRowIndex = firstMergeRowIndex;
            }
            ExcelErrData excelErrData = new ExcelErrData(rowIndex,
                    Translator.get("number")
                            .concat(StringUtils.SPACE)
                            .concat(String.valueOf(errorRowIndex + 1)).concat(StringUtils.SPACE)
                            .concat(Translator.get("row"))
                            .concat(Translator.get("error"))
                            .concat("：")
                            .concat(errMsg.toString()));
            //错误信息
            errList.add(excelErrData);
        } else {
            //通过数量
            list.add(functionalCaseExcelData);
        }
    }


    public void validate(FunctionalCaseExcelData data, StringBuilder errMsg) {
        //模块校验
        validateModule(data, errMsg);
        //校验自定义字段
        validateCustomField(data, errMsg);
        //校验id
        validateIdExist(data, errMsg);
        //标签长度校验
        validateTags(data, errMsg);
        //校验用例步骤和预期结果长度
        validateStep(data, errMsg);
    }

    /**
     * 校验用例步骤 预期结果
     *
     * @param data
     * @param errMsg
     */
    private void validateStep(FunctionalCaseExcelData data, StringBuilder errMsg) {
        if (StringUtils.equalsIgnoreCase(data.getCaseEditType(), FunctionalCaseTypeConstants.CaseEditType.STEP.name())) {
            getSteps(data, errMsg);
        }
    }

    private String getSteps(FunctionalCaseExcelData data, StringBuilder errMsg) {
        List<Map<String, Object>> steps = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(data.getMergeTextDescription()) || CollectionUtils.isNotEmpty(data.getMergeExpectedResult())) {
            // 如果是合并单元格，则组合多条单元格的数据
            for (int i = 0; i < data.getMergeTextDescription().size(); i++) {
                List<Map<String, Object>> rowSteps = getSingleRowSteps(data.getMergeTextDescription().get(i), data.getMergeExpectedResult().get(i), steps.size(), errMsg);
                steps.addAll(rowSteps);
            }
        } else {
            // 如果不是合并单元格，则直接解析单元格数据
            steps.addAll(getSingleRowSteps(data.getTextDescription(), data.getExpectedResult(), steps.size(), errMsg));
        }
        return JSON.toJSONString(steps);
    }


    private List<Map<String, Object>> getSingleRowSteps(String cellDesc, String cellResult, Integer startStepIndex, StringBuilder errMsg) {
        List<Map<String, Object>> steps = new ArrayList<>();

        List<String> stepDescList = parseStepCell(cellDesc);
        List<String> stepResList = parseStepCell(cellResult);

        int index = Math.max(stepDescList.size(), stepResList.size());
        for (int i = 0; i < index; i++) {
            // 保持插入顺序，判断用例是否有相同的steps
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("id", UUID.randomUUID().toString());
            step.put("num", startStepIndex + i);
            if (i < stepDescList.size()) {
                step.put("desc", stepDescList.get(i));
                if (stepDescList.get(i).length() > STEP_LENGTH) {
                    errMsg.append(Translator.get("step_length"))
                            .append(ERROR_MSG_SEPARATOR);
                    return steps;
                }
            } else {
                step.put("desc", StringUtils.EMPTY);
            }

            if (i < stepResList.size()) {
                step.put("result", stepResList.get(i));
                if (stepResList.get(i).length() > STEP_LENGTH) {
                    errMsg.append(Translator.get("result_length"))
                            .append(ERROR_MSG_SEPARATOR);
                    return steps;
                }
            } else {
                step.put("result", StringUtils.EMPTY);
            }

            steps.add(step);
        }
        return steps;
    }

    private List<String> parseStepCell(String cellContent) {
        List<String> cellStepContentList = new ArrayList<>();
        if (StringUtils.isNotEmpty(cellContent)) {
            // 根据[1], [2]...分割步骤描述, 开头空字符去掉, 末尾保留
            String[] cellContentArr = cellContent.split("\\[\\d+]", -1);
            if (StringUtils.isEmpty(cellContentArr[0])) {
                cellContentArr = Arrays.copyOfRange(cellContentArr, 1, cellContentArr.length);
            }
            for (String stepContent : cellContentArr) {
                cellStepContentList.add(stepContent.replaceAll("(?m)^\\s*|\\s*$", StringUtils.EMPTY));
            }
        } else {
            cellStepContentList.add(StringUtils.EMPTY);
        }
        return cellStepContentList;
    }


    /**
     * 校验标签长度 个数
     *
     * @param data
     * @param errMsg
     */
    private void validateTags(FunctionalCaseExcelData data, StringBuilder errMsg) {
        if (StringUtils.isBlank(data.getTags())) {
            data.setTags(StringUtils.EMPTY);
            return;
        }
        List<String> tags = functionalCaseService.handleImportTags(data.getTags());
        if (tags.size() > TAGS_COUNT) {
            errMsg.append(Translator.get("tags_count"))
                    .append(ERROR_MSG_SEPARATOR);
            return;
        }
        tags.forEach(tag -> {
            if (tag.length() > TAG_LENGTH) {
                errMsg.append(Translator.get("tag_length"))
                        .append(ERROR_MSG_SEPARATOR);
            }
            return;
        });
    }

    /**
     * 校验Excel中是否有ID
     * 是否覆盖：
     * 1.覆盖：id存在则更新，id不存在则新增
     * 2.不覆盖：id存在不处理，id不存在新增
     *
     * @param data
     * @param errMsg
     */
    @Nullable
    private void validateIdExist(FunctionalCaseExcelData data, StringBuilder errMsg) {
        //当前读取的数据有ID
        if (StringUtils.isNotEmpty(data.getNum())) {
            Integer num = -1;
            try {
                num = Integer.parseInt(data.getNum());
            } catch (Exception e) {
                return;
            }
            if (num < 0) {
                errMsg.append(Translator.get("id_not_rightful"))
                        .append("[")
                        .append(data.getNum())
                        .append("]; ");
            }
        }
    }


    /**
     * 校验自定义字段，并记录错误提示
     * 如果填写的是自定义字段的选项值，则转换成ID保存
     *
     * @param data
     * @param errMsg
     */
    private void validateCustomField(FunctionalCaseExcelData data, StringBuilder errMsg) {
        Map<String, Object> customData = data.getCustomData();
        for (String fieldName : customData.keySet()) {
            Object value = customData.get(fieldName);
            TemplateCustomFieldDTO templateCustomFieldDTO = customFieldsMap.get(fieldName);
            if (templateCustomFieldDTO == null) {
                continue;
            }
            AbstractCustomFieldValidator customFieldValidator = customFieldValidatorMap.get(templateCustomFieldDTO.getType());
            try {
                customFieldValidator.validate(templateCustomFieldDTO, value.toString());
            } catch (CustomFieldValidateException e) {
                errMsg.append(e.getMessage().concat(ERROR_MSG_SEPARATOR));
            }
        }
    }


    /**
     * 校验模块
     *
     * @param data
     * @param errMsg
     */
    private void validateModule(FunctionalCaseExcelData data, StringBuilder errMsg) {
        if (!StringUtils.startsWith(data.getModule(), "/")) {
            data.setModule("/" + data.getModule());
        }
        String module = data.getModule();
        if (StringUtils.isNotEmpty(module)) {
            String[] nodes = module.split("/");
            //模块名不能为空
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), StringUtils.EMPTY)) {
                    errMsg.append(Translator.get("module_not_null"))
                            .append(ERROR_MSG_SEPARATOR);
                    break;
                }
            }
            //增加字数校验，每一层不能超过100个字
            for (int i = 0; i < nodes.length; i++) {
                String nodeStr = nodes[i];
                if (StringUtils.isNotEmpty(nodeStr)) {
                    if (nodeStr.trim().length() > 100) {
                        errMsg.append(Translator.get("module"))
                                .append(Translator.get("functional_case.module.length_less_than"))
                                .append("100:")
                                .append(nodeStr);
                        break;
                    }
                }
            }
        }
    }


    /**
     * 数据转换
     *
     * @param row
     * @return
     */
    private FunctionalCaseExcelData parseDataToModel(Map<Integer, String> row) {
        FunctionalCaseExcelData data = new FunctionalCaseExcelDataFactory().getFunctionalCaseExcelDataLocal();
        for (Map.Entry<Integer, String> headEntry : headMap.entrySet()) {
            Integer index = headEntry.getKey();
            String field = headEntry.getValue();
            if (StringUtils.isBlank(field)) {
                continue;
            }
            String value = StringUtils.isEmpty(row.get(index)) ? StringUtils.EMPTY : row.get(index);

            if (excelHeadToFieldNameDic.containsKey(field)) {
                field = excelHeadToFieldNameDic.get(field);
            }

            if (StringUtils.equals(field, "id")) {
                data.setName(value);
            } else if (StringUtils.equals(field, "num")) {
                data.setNum(value);
            } else if (StringUtils.equals(field, "name")) {
                data.setName(value);
            } else if (StringUtils.equals(field, "module")) {
                data.setModule(value);
            } else if (StringUtils.equals(field, "tags")) {
                data.setTags(value);
            } else if (StringUtils.equals(field, "prerequisite")) {
                data.setPrerequisite(value);
            } else if (StringUtils.equals(field, "description")) {
                data.setDescription(value);
            } else if (StringUtils.equals(field, "textDescription")) {
                data.setTextDescription(value);
            } else if (StringUtils.equals(field, "expectedResult")) {
                data.setExpectedResult(value);
            } else if (StringUtils.equals(field, "caseEditType")) {
                data.setCaseEditType(value);
            } else {
                data.getCustomData().put(field, value);
            }
        }
        return data;
    }


    /**
     * 处理合并单元格
     *
     * @param data
     * @param rowIndex
     */
    private void handleMergeData(Map<Integer, String> data, Integer rowIndex) {
        isMergeRow = false;
        isMergeLastRow = false;
        if (getNameColIndex() == null) {
            throw new MSException(Translator.get("case_import_table_header_missing"));
        }
        data.keySet().forEach(col -> {
            Iterator<ExcelMergeInfo> iterator = mergeInfoSet.iterator();
            while (iterator.hasNext()) {
                ExcelMergeInfo mergeInfo = iterator.next();
                // 如果单元格的行号在合并单元格的范围之间，并且列号相等，说明该单元格是合并单元格中的一部分
                if (mergeInfo.getFirstRowIndex() <= rowIndex && rowIndex <= mergeInfo.getLastRowIndex()
                        && col.equals(mergeInfo.getFirstColumnIndex())) {
                    // 根据名称列是否是合并单元格判断是不是同一条用例
                    if (getNameColIndex().equals(col)) {
                        isMergeRow = true;
                    }
                    // 如果是合并单元格的第一个cell，则把这个单元格的数据存起来
                    if (rowIndex.equals(mergeInfo.getFirstRowIndex())) {
                        if (StringUtils.isNotBlank(data.get(col))) {
                            mergeCellDataMap.put(mergeInfo, data.get(col));
                        }
                    } else {
                        // 非第一个，获取存储的数据填充
                        String cellData = mergeCellDataMap.get(mergeInfo);
                        if (StringUtils.isNotBlank(cellData)) {
                            data.put(col, cellData);
                        }
                    }
                    // 如果合并单元格的最后一个单元格，标记下
                    if (rowIndex.equals(mergeInfo.getLastRowIndex())) {
                        // 根据名称列是否是合并单元格判断是不是同一条用例
                        if (getNameColIndex().equals(col)) {
                            isMergeLastRow = true;
                            // 清除掉上一次已经遍历完成的数据，提高查询效率
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
        });
    }

    private Integer getNameColIndex() {
        return findColIndex("name");
    }

    private Integer getTextDescriptionColIndex() {
        return findColIndex("textDescription");
    }

    private Integer getExpectedResultColIndex() {
        return findColIndex("expectedResult");
    }

    private Integer findColIndex(String colName) {
        for (Integer key : headMap.keySet()) {
            if (StringUtils.equals(headMap.get(key), colName)) {
                return key;
            }
        }
        return null;
    }

    /**
     * @description: 获取注解里ExcelProperty的value
     */
    public Set<String> genExcelHeadToFieldNameDicAndGetNotRequiredFields() throws NoSuchFieldException {

        Set<String> result = new HashSet<>();
        Field field;
        Field[] fields = excelDataClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            field = excelDataClass.getDeclaredField(fields[i].getName());
            field.setAccessible(true);
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty != null) {
                StringBuilder value = new StringBuilder();
                for (String v : excelProperty.value()) {
                    value.append(v);
                }
                excelHeadToFieldNameDic.put(value.toString(), field.getName());
                // 检查是否必有的头部信息
                if (field.getAnnotation(NotRequired.class) != null) {
                    result.add(value.toString());
                }
            }
        }
        return result;
    }

    private void formatHeadMap() {
        for (Integer key : headMap.keySet()) {
            String name = headMap.get(key);
            if (excelHeadToFieldNameDic.containsKey(name)) {
                headMap.put(key, excelHeadToFieldNameDic.get(name));
            }
        }
    }

    public List<ExcelErrData<FunctionalCaseExcelData>> getErrList() {
        return errList;
    }

    public List<FunctionalCaseExcelData> getList() {
        return list;
    }
}
