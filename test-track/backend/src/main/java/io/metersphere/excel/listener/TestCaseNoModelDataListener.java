package io.metersphere.excel.listener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.CustomFieldType;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldResourceDTO;
import io.metersphere.excel.annotation.NotRequired;
import io.metersphere.excel.constants.TestCaseImportFiled;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.ExcelMergeInfo;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.excel.domain.TestCaseExcelDataFactory;
import io.metersphere.excel.utils.ExcelImportType;
import io.metersphere.excel.utils.ExcelValidateHelper;
import io.metersphere.exception.CustomFieldValidateException;
import io.metersphere.i18n.Translator;
import io.metersphere.request.testcase.TestCaseImportRequest;
import io.metersphere.service.TestCaseService;
import io.metersphere.validate.AbstractCustomFieldValidator;
import io.metersphere.validate.CustomFieldValidatorFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 由于功能案例中含有自定义字段。导入的时候使用无模板对象的读取方式
 *
 * @author song.tianyang
 * @Date 2021/7/7 4:25 下午
 */
public class TestCaseNoModelDataListener extends AnalysisEventListener<Map<Integer, String>> {

    private Class excelDataClass;

    protected List<ExcelErrData<TestCaseExcelData>> errList = new ArrayList<>();

    protected List<TestCaseExcelData> excelDataList = new ArrayList<>();

    private Map<Integer, String> headMap;
    private Map<String, String> excelHeadToFieldNameDic = new HashMap<>();

    /**
     * 每隔2000条存储数据库，然后清理list ，方便内存回收
     */
    protected static final int BATCH_COUNT = 5000;

    private TestCaseService testCaseService;

    protected List<TestCaseExcelData> updateList = new ArrayList<>();  //存储待更新用例的集合

    protected List<TestCaseExcelData> list = new ArrayList<>();

    protected boolean isUpdated = false;  //判断是否更新过用例，将会传给前端

    Set<String> customIds;

    private List<String> names = new LinkedList<>();
    private List<String> ids = new LinkedList<>();

    Map<String, CustomFieldDao> customFieldsMap = new HashMap<>();

    private TestCaseImportRequest request;

    private Set<ExcelMergeInfo> mergeInfoSet;

    // 存储当前合并的一条完整数据，其中步骤没有合并是多行
    private TestCaseExcelData currentMergeData;

    private static final String ERROR_MSG_SEPARATOR = ";";

    /**
     * 标记下当前遍历的行是不是有合并单元格
     */
    private Boolean isMergeRow;

    /**
     * 标记下当前遍历的行是不是合并单元格的最后一行
     */
    private Boolean isMergeLastRow;

    private Integer firstMergeRowIndex;

    /**
     * 存储合并单元格对应的数据，key 为重写了 compareTo 的 ExcelMergeInfo
     */
    private HashMap<ExcelMergeInfo, String> mergeCellDataMap = new HashMap<>();

    private HashMap<String, AbstractCustomFieldValidator> customFieldValidatorMap;

    private Map<String, List<CustomFieldResourceDTO>> testCaseCustomFieldMap = new HashMap<>();

    public boolean isUpdated() {
        return isUpdated;
    }

    public TestCaseNoModelDataListener(TestCaseImportRequest request, Class c, Set<ExcelMergeInfo> mergeInfoSet) {
        this.mergeInfoSet = mergeInfoSet;
        this.excelDataClass = c;
        this.testCaseService = CommonBeanFactory.getBean(TestCaseService.class);
        customIds = new HashSet<>();

        this.request = request;

        this.customFieldValidatorMap = CustomFieldValidatorFactory.getValidatorMap();

        List<CustomFieldDao> customFields = request.getCustomFields();
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFieldsMap = customFields.stream().collect(Collectors.toMap(CustomFieldDao::getName, i -> i));
        }
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
        try {
            this.genExcelHeadToFieldNameDicAndGetNotRequiredFields();
        } catch (NoSuchFieldException e) {
            LogUtil.error(e);
        }
        this.formatHeadMap();
        super.invokeHeadMap(headMap, context);
    }

    public void invoke(Map<Integer, String> data, AnalysisContext analysisContext) {

        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();

        handleMergeData(data, rowIndex);

        TestCaseExcelData testCaseExcelData;
        // 读取名称列，如果该列是合并单元格，则读取多行数据后合并步骤
        if (this.isMergeRow) {
            if (currentMergeData == null) {
                this.firstMergeRowIndex = rowIndex;
                // 如果是合并单元格的首行
                testCaseExcelData = this.parseDataToModel(data);
                testCaseExcelData.setMergeStepDesc(new ArrayList<>() {{
                    add(testCaseExcelData.getStepDesc());
                }});
                testCaseExcelData.setMergeStepResult(new ArrayList<>() {{
                    add(testCaseExcelData.getStepResult());
                }});
                // 记录下数据并返回
                this.currentMergeData = testCaseExcelData;
                if (!this.isMergeLastRow) {
                    return;
                } else {
                    this.currentMergeData = null;
                }
            } else {
                // 获取存储的数据，并添加多个步骤
                this.currentMergeData.getMergeStepDesc()
                        .add(data.get(getStepDescColIndex()));
                this.currentMergeData.getMergeStepResult()
                        .add(data.get(getStepResultColIndex()));
                // 是最后一行的合并单元格，保存并清空 currentMergeData，走之后的逻辑
                if (this.isMergeLastRow) {
                    testCaseExcelData = this.currentMergeData;
                    this.currentMergeData = null;
                } else {
                    return;
                }
            }
        } else {
            this.firstMergeRowIndex = null;
            testCaseExcelData = this.parseDataToModel(data);
        }

        StringBuilder errMsg;
        try {
            //根据excel数据实体中的javax.validation + 正则表达式来校验excel数据
            errMsg = new StringBuilder(ExcelValidateHelper.validateEntity(testCaseExcelData));
            //自定义校验规则
            if (StringUtils.isEmpty(errMsg)) {
                validate(testCaseExcelData, errMsg);
            }
        } catch (NoSuchFieldException e) {
            errMsg = new StringBuilder(Translator.get("parse_data_error"));
            LogUtil.error(e.getMessage(), e);
        }

        if (!StringUtils.isEmpty(errMsg)) {
            Integer errorRowIndex = rowIndex;
            if (firstMergeRowIndex != null) {
                errorRowIndex = firstMergeRowIndex;
            }
            ExcelErrData excelErrData = new ExcelErrData(testCaseExcelData, rowIndex,
                    Translator.get("number")
                            .concat(StringUtils.SPACE)
                            .concat(String.valueOf(errorRowIndex + 1)).concat(StringUtils.SPACE)
                            .concat(Translator.get("row"))
                            .concat(Translator.get("error"))
                            .concat("：")
                            .concat(errMsg.toString()));
            errList.add(excelErrData);
        } else {
            if (isCreateModel()) {
                list.add(testCaseExcelData);
            }
        }
        if (list.size() > BATCH_COUNT || updateList.size() > BATCH_COUNT) {
            saveData();
            list.clear();
            updateList.clear();
        }
    }

    /**
     * 处理合并单元格
     *
     * @param data
     * @param rowIndex
     */
    private void handleMergeData(Map<Integer, String> data, Integer rowIndex) {
        this.isMergeRow = false;
        this.isMergeLastRow = false;
        if (getNameColIndex() == null) {
            MSException.throwException("缺少名称表头");
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
                        this.isMergeRow = true;
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
                            this.isMergeLastRow = true;
                            // 清除掉上一次已经遍历完成的数据，提高查询效率
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
        });
    }

    public void validate(TestCaseExcelData data, StringBuilder errMsg) {

        validateCustomNum(data, errMsg);

        validateModule(data, errMsg);

        validateCustomField(data, errMsg);

        validateIdExist(data, errMsg);

        validateDbExist(data, errMsg);
    }

    private void validateDbExist(TestCaseExcelData data, StringBuilder stringBuilder) {
        if (this.isUpdateModel()) {
            return;
        }
        if (request.getTestCaseNames().contains(data.getName())) {
            TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
            BeanUtils.copyBean(testCase, data);
            testCase.setProjectId(request.getProjectId());
            String steps = getSteps(data);
            testCase.setSteps(steps);
            testCase.setType(TestCaseConstants.Type.Functional.getValue());

            boolean dbExist = testCaseService.exist(testCase);
            boolean excelExist = false;

            if (dbExist) {
                // db exist
                stringBuilder.append(
                        Translator.get("test_case_already_exists")
                                .concat("：")
                                .concat(data.getName())
                                .concat(ERROR_MSG_SEPARATOR));
            } else {
                // @Data 重写了 equals 和 hashCode 方法
                excelExist = excelDataList.contains(data);
            }

            if (excelExist) {
                // excel exist
                stringBuilder.append(
                        Translator.get("test_case_already_exists_excel")
                                .concat("：")
                                .concat(data.getName())
                                .concat(ERROR_MSG_SEPARATOR));
            } else {
                if (!dbExist) {
                    excelDataList.add(data);
                }
            }

        } else {
            request.getTestCaseNames().add(data.getName());
            excelDataList.add(data);
        }
    }

    /**
     * 校验Excel中是否有ID
     * 有的话校验ID是否已在当前项目中存在，存在则更新用例，
     * 不存在则继续校验看是否重复，不重复则新建用例
     *
     * @param data
     * @param stringBuilder
     */
    @Nullable
    private void validateIdExist(TestCaseExcelData data, StringBuilder stringBuilder) {

        //当前读取的数据有ID
        if (null != data.getCustomNum()) {
            if (isUpdateModel()) {
                String checkResult = null;
                if (request.isUseCustomId()) {
                    checkResult = testCaseService.checkCustomIdExist(data.getCustomNum(), request.getProjectId());
                } else {
                    int customNumId = -1;
                    try {
                        customNumId = Integer.parseInt(data.getCustomNum());
                    } catch (Exception e) {
                        LogUtil.error(e);
                    }
                    if (customNumId < 0) {
                        stringBuilder.append(Translator.get("id_not_rightful"))
                                .append("[")
                                .append(data.getCustomNum())
                                .append("]; ");
                    } else {
                        checkResult = testCaseService.checkIdExist(customNumId, request.getProjectId());
                    }
                }
                //该ID在当前项目中存在
                if (null != checkResult) {
                    //如果前面所经过的校验都没报错
                    if (StringUtils.isEmpty(stringBuilder)) {
                        data.setId(checkResult);
                        //将当前数据存入更新列表
                        updateList.add(data);
                    }
                } else {
                    // 该ID在当前数据库中不存在，应当继续校验用例是否重复,
                    // 在下面的校验过程中，num的值会被用于判断是否重复，所以应当先设置为null
                    data.setNum(null);
                }
            }
        }
    }

    private boolean isUpdateModel() {
        return StringUtils.equals(request.getImportType(), ExcelImportType.Update.name());
    }

    private boolean isCreateModel() {
        return StringUtils.equals(request.getImportType(), ExcelImportType.Create.name());
    }

    /**
     * 校验自定义字段，并记录错误提示
     * 如果填写的是自定义字段的选项值，则转换成ID保存
     * @param data
     * @param stringBuilder
     */
    private void validateCustomField(TestCaseExcelData data, StringBuilder stringBuilder) {
        Map<String, Object> customData = data.getCustomData();
        for (String fieldName : customData.keySet()) {
            Object value = customData.get(fieldName);
            CustomFieldDao customField = customFieldsMap.get(fieldName);
            if (customField == null) {
                continue;
            }
            AbstractCustomFieldValidator customFieldValidator = customFieldValidatorMap.get(customField.getType());
            try {
                customFieldValidator.validate(customField, value.toString());
                if (customFieldValidator.isKVOption) {
                    // 这里如果填的是选项值，替换成选项ID，保存
                    customData.put(fieldName, customFieldValidator.parse2Key(value.toString(), customField));
                }
                if (StringUtils.equalsAny(customField.getType(), CustomFieldType.TEXTAREA.getValue(), CustomFieldType.RICH_TEXT.getValue())) {
                    data.getTextFieldSet().add(fieldName);
                }
            } catch (CustomFieldValidateException e) {
                stringBuilder.append(e.getMessage().concat(ERROR_MSG_SEPARATOR));
            }
            if (StringUtils.equals(fieldName, TestCaseImportFiled.STATUS.getFiledLangMap().get(Locale.SIMPLIFIED_CHINESE))) {
                data.setStatus(customData.get(fieldName).toString());
            } else if (StringUtils.equals(fieldName, TestCaseImportFiled.PRIORITY.getFiledLangMap().get(Locale.SIMPLIFIED_CHINESE))) {
                data.setPriority(customData.get(fieldName).toString());
            } else if (StringUtils.equals(fieldName, TestCaseImportFiled.MAINTAINER.getFiledLangMap().get(Locale.SIMPLIFIED_CHINESE))) {
                data.setMaintainer(customData.get(fieldName).toString());
            }
        }
    }

    private void validateModule(TestCaseExcelData data, StringBuilder stringBuilder) {
        String nodePath = data.getNodePath();
        //校验”所属模块"
        if (nodePath != null) {
            String[] nodes = nodePath.split("/");
            //校验模块深度
            if (nodes.length > TestCaseConstants.MAX_NODE_DEPTH + 1) {
                stringBuilder.append(Translator.get("test_case_node_level_tip"))
                        .append(TestCaseConstants.MAX_NODE_DEPTH)
                        .append(Translator.get("test_case_node_level"))
                        .append("; ");
            }
            //模块名不能为空
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), StringUtils.EMPTY)) {
                    stringBuilder.append(Translator.get("module_not_null"))
                            .append(ERROR_MSG_SEPARATOR);
                    break;
                }
            }
            //增加字数校验，每一层不能超过100字
            for (int i = 0; i < nodes.length; i++) {
                String nodeStr = nodes[i];
                if (StringUtils.isNotEmpty(nodeStr)) {
                    if (nodeStr.trim().length() > 100) {
                        stringBuilder.append(Translator.get("module"))
                                .append(Translator.get("test_track.length_less_than"))
                                .append("100:")
                                .append(nodeStr);
                        break;
                    }
                }
            }
        }
    }

    private void validateCustomNum(TestCaseExcelData data, StringBuilder stringBuilder) {
        if (request.isUseCustomId() || isUpdateModel()) {
            if (data.getCustomNum() == null) {
                stringBuilder.append(Translator.get("id_required"))
                        .append(ERROR_MSG_SEPARATOR);
            } else {
                String customId = data.getCustomNum();
                if (StringUtils.isEmpty(customId)) {
                    stringBuilder.append(Translator.get("id_required"))
                            .append(ERROR_MSG_SEPARATOR);
                } else if (customIds.contains(customId.toLowerCase())) {
                    stringBuilder.append(Translator.get("id_repeat_in_table"))
                            .append(ERROR_MSG_SEPARATOR);
                } else if (isCreateModel() && request.getSavedCustomIds().contains(customId)) {
                    stringBuilder.append(Translator.get("custom_num_is_exist"))
                            .append(ERROR_MSG_SEPARATOR);
                } else if (isUpdateModel() && !request.getSavedCustomIds().contains(customId)) {
                    stringBuilder.append(Translator.get("custom_num_is_not_exist"))
                            .append(ERROR_MSG_SEPARATOR);
                } else {
                    customIds.add(customId.toLowerCase());
                }
            }
        }
    }

    public List<String> getNames() {
        return this.names;
    }

    public List<String> getIds() {
        return this.ids;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public void saveData() {

        //excel中用例都有错误时就返回，只要有用例可用于更新或者插入就不返回
        if (!errList.isEmpty() && !request.isIgnore()) {
            return;
        }
        if ((isCreateModel() && CollectionUtils.isEmpty(list)) || (isUpdateModel() && CollectionUtils.isEmpty(updateList))) {
           MSException.throwException(Translator.get("no_legitimate_case_tip"));
        }

        if (CollectionUtils.isNotEmpty(list)) {
            List<TestCaseWithBLOBs> result = list.stream()
                    .map(item -> this.convert2TestCase(item))
                    .collect(Collectors.toList());
            testCaseService.saveImportData(result, request, testCaseCustomFieldMap);
            this.names = result.stream().map(TestCase::getName).collect(Collectors.toList());
            this.ids = result.stream().map(TestCase::getId).collect(Collectors.toList());
            this.isUpdated = true;
        }

        if (CollectionUtils.isNotEmpty(updateList)) {
            List<TestCaseWithBLOBs> result2 = updateList.stream()
                    .map(item -> this.convert2TestCaseForUpdate(item))
                    .collect(Collectors.toList());
            testCaseService.updateImportData(result2, request, testCaseCustomFieldMap);
            this.isUpdated = true;
            this.names = result2.stream().map(TestCase::getName).collect(Collectors.toList());
            this.ids = result2.stream().map(TestCase::getId).collect(Collectors.toList());
            updateList.clear();
        }
    }

    private TestCaseWithBLOBs convert2TestCase(TestCaseExcelData data) {
        TestCaseWithBLOBs testCase = parseData(data);
        testCase.setId(UUID.randomUUID().toString());
        testCase.setCreateTime(System.currentTimeMillis());
        if (request.isUseCustomId()) {
            testCase.setCustomNum(data.getCustomNum());
        }

        buildTestCaseCustomFieldMap(data, testCase);

        return testCase;
    }

    /**
     * 暂存功能自定义字段
     * @param data
     * @param testCase
     */
    private void buildTestCaseCustomFieldMap(TestCaseExcelData data, TestCaseWithBLOBs testCase) {
        Map<String, Object> customData = data.getCustomData();
        Set<String> textFieldSet = data.getTextFieldSet();
        List<CustomFieldResourceDTO> testCaseCustomFields = new ArrayList<>();
        customData.forEach((k, v) -> {
            if ((v instanceof List && CollectionUtils.isNotEmpty((List)v))
                    || StringUtils.isNotBlank(v.toString())) {
                CustomFieldDao customFieldDao = customFieldsMap.get(k);
                if (customFieldDao != null) {
                    CustomFieldResourceDTO customFieldResource = new CustomFieldResourceDTO();
                    customFieldResource.setFieldId(customFieldDao.getId());
                    if (textFieldSet.contains(customFieldDao.getId())) {
                        customFieldResource.setTextValue(v.toString());
                    } else {
                        customFieldResource.setValue(JSON.toJSONString(v));
                    }
                    customFieldResource.setResourceId(testCase.getId());
                    testCaseCustomFields.add(customFieldResource);
                }
            }
        });
        if (CollectionUtils.isNotEmpty(testCaseCustomFields)) {
            testCaseCustomFieldMap.put(testCase.getId(), testCaseCustomFields);
        }
    }

    @NotNull
    private TestCaseWithBLOBs parseData(TestCaseExcelData data) {
        TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
        BeanUtils.copyBean(testCase, data);
        testCase.setProjectId(request.getProjectId());
        testCase.setUpdateTime(System.currentTimeMillis());

        String nodePath = data.getNodePath();

        if (!nodePath.startsWith("/")) {
            nodePath = "/" + nodePath;
        }
        if (nodePath.endsWith("/")) {
            nodePath = nodePath.substring(0, nodePath.length() - 1);
        }
        testCase.setNodePath(nodePath);

        //将标签设置为前端可解析的格式
        String modifiedTags = modifyTagPattern(data);
        testCase.setTags(modifiedTags);
        testCase.setType(TestCaseConstants.Type.Functional.getValue());
        data.setStatus(data.getStatus());

        // todo 这里要获取模板的自定义字段再新建关联关系
        if (StringUtils.isNotBlank(data.getMaintainer())) {
            testCase.setMaintainer(data.getMaintainer());
        }

        if (StringUtils.isNotBlank(data.getStepModel())
                && StringUtils.equals(data.getStepModel(), TestCaseConstants.StepModel.TEXT.name())) {
            testCase.setStepDescription(data.getStepDesc());
            testCase.setExpectedResult(data.getStepResult());
        } else {
            String steps = getSteps(data);
            testCase.setSteps(steps);
        }
        return testCase;
    }

    /**
     * 将Excel中的数据对象转换为用于更新操作的用例数据对象，
     *
     * @param data
     * @return
     */
    private TestCaseWithBLOBs convert2TestCaseForUpdate(TestCaseExcelData data) {
        TestCaseWithBLOBs testCase = parseData(data);
        testCase.setUpdateTime(System.currentTimeMillis());
        if (!request.isUseCustomId()) {
            testCase.setNum(Integer.parseInt(data.getCustomNum()));
            testCase.setCustomNum(null);
        }
        buildTestCaseCustomFieldMap(data, testCase);
        return testCase;
    }

    /**
     * 调整tags格式，便于前端进行解析。
     * 例如对于：标签1，标签2。将调整为:["标签1","标签2"]。
     */
    public String modifyTagPattern(TestCaseExcelData data) {
        String tags = data.getTags();
        try {
            if (StringUtils.isNotBlank(tags)) {
                JSON.parseArray(tags);
                return tags;
            }
            return "[]";
        } catch (Exception e) {
            if (tags != null) {
                //当标签值以中英文的逗号和分号分隔时才能正确解析
                Stream<String> stringStream = Arrays.stream(tags.split("[,;，；\"\\r|\\n|\\r\\n\"]"));
                //替换非法字符反斜杠"\"为"\\"
                List<String> tagList = stringStream.map(tag -> tag = "\"" + tag.replaceAll("\\\\", "\\\\\\\\") + "\"")
                        .collect(Collectors.toList());
                String modifiedTags = StringUtils.join(tagList, ",");
                modifiedTags = "[" + modifiedTags + "]";
                return modifiedTags;
            } else {
                return "[]";
            }
        }
    }

    public String getSteps(TestCaseExcelData data) {
        List jsonArray = new ArrayList<>();

        // 如果是合并单元格，则组合多条单元格的数据
        if (CollectionUtils.isNotEmpty(data.getMergeStepDesc())
                || CollectionUtils.isNotEmpty(data.getMergeStepResult())) {
            for (int i = 0; i < data.getMergeStepDesc().size(); i++) {
                Map<String, Object> step = new LinkedHashMap<>();
                step.put("num", i + 1);
                step.put("desc", Optional.ofNullable(data.getMergeStepDesc().get(i)).orElse(StringUtils.EMPTY));
                step.put("result", Optional.ofNullable(data.getMergeStepResult().get(i)).orElse(StringUtils.EMPTY));
                jsonArray.add(step);
            }
            return JSON.toJSONString(jsonArray);
        }

        List<String> stepDescList = new ArrayList<>();
        List<String> stepResList = new ArrayList<>();

        Set<Integer> rowNums = new HashSet<>();
        if (data.getStepDesc() != null) {
            String[] stepDesc = data.getStepDesc().split("\r|\n|\r\n");

            int rowIndex = 1;
            for (String row : stepDesc) {
                RowInfo rowInfo = this.parseIndexInRow(row, rowIndex);
                stepDescList.add(rowInfo.rowInfo);
                rowNums.add(rowIndex++);
            }
        } else {
            stepDescList.add(StringUtils.EMPTY);
        }

        if (data.getStepResult() != null) {
            String[] stepRes = data.getStepResult().split("\r|\n|\r\n");
            int lastStepIndex = 1;
            for (String row : stepRes) {
                RowInfo rowInfo = this.parseIndexInRow(row, lastStepIndex);
                String rowMessage = rowInfo.rowInfo;
                stepResList.add(rowMessage);
                lastStepIndex++;
            }
        } else {
            stepResList.add(StringUtils.EMPTY);
        }

        int index = stepDescList.size() > stepResList.size() ? stepDescList.size() : stepResList.size();

        for (int i = 0; i < index; i++) {

            // 保持插入顺序，判断用例是否有相同的steps
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("num", i + 1);
            if (i < stepDescList.size()) {
                step.put("desc", stepDescList.get(i));
            } else {
                step.put("desc", StringUtils.EMPTY);
            }

            if (i < stepResList.size()) {
                step.put("result", stepResList.get(i));
            } else {
                step.put("result", StringUtils.EMPTY);
            }

            jsonArray.add(step);
        }
        return JSON.toJSONString(jsonArray);
    }

    private RowInfo parseIndexInRow(String row, int rowIndex) {
        RowInfo rowInfo = new RowInfo();
        String parseString = row;

        int index = -1;
        String rowMessage = row;
        String[] indexSplitCharArr = new String[]{")", "）", "]", "】", ".", ",", "，", "。"};
        if (StringUtils.startsWithAny(row, "(", "（", "[", "【")) {
            parseString = parseString.substring(1);
        }
        for (String splitChar : indexSplitCharArr) {
            if (StringUtils.contains(parseString, splitChar)) {
                String[] rowSplit = StringUtils.split(parseString, splitChar);
                if (rowSplit.length > 0) {
                    String indexString = rowSplit[0];
                    if (StringUtils.isNumeric(indexString) && indexString.equals(String.valueOf(rowIndex))) {
                        try {
                            index = Integer.parseInt(indexString);
                            rowMessage = StringUtils.substring(parseString, indexString.length() + splitChar.length());
                        } catch (Exception e) {
                        }

                        if (index > -1) {
                            break;
                        } else {
                            rowMessage = row;
                        }
                    }
                }
            }
        }
        rowInfo.index = index;
        if (rowMessage == null) {
            rowMessage = StringUtils.EMPTY;
        }
        rowInfo.rowInfo = rowMessage;
        return rowInfo;
    }

    private Integer getNameColIndex() {
        return findColIndex("name");
    }

    private Integer getStepResultColIndex() {
        return findColIndex("stepResult");
    }

    private Integer getStepDescColIndex() {
        return findColIndex("stepDesc");
    }

    private Integer findColIndex(String colName) {
        for (Integer key : headMap.keySet()) {
            if (StringUtils.equals(headMap.get(key), colName)) {
                return key;
            }
        }
        return null;
    }

    private TestCaseExcelData parseDataToModel(Map<Integer, String> row) {
        TestCaseExcelData data = new TestCaseExcelDataFactory().getTestCaseExcelDataLocal();
        for (Map.Entry<Integer, String> headEntry : headMap.entrySet()) {
            Integer index = headEntry.getKey();
            String field = headEntry.getValue();
            String value = StringUtils.isEmpty(row.get(index)) ? StringUtils.EMPTY : row.get(index);

            if (excelHeadToFieldNameDic.containsKey(field)) {
                field = excelHeadToFieldNameDic.get(field);
            }

            if (StringUtils.equals(field, "id")) {
                data.setId(value);
            } else if (StringUtils.equals(field, "num")) {
                try {
                    data.setNum(Integer.parseInt(value));
                } catch (Exception e) {
                    MSException.throwException("[ID]" + value + "格式化异常");
                }
            } else if (StringUtils.equals(field, "customNum")) {
                data.setCustomNum(value);
            } else if (StringUtils.equals(field, "name")) {
                data.setName(value);
            } else if (StringUtils.equals(field, "nodePath")) {
                data.setNodePath(value);
            } else if (StringUtils.equals(field, "tags")) {
                data.setTags(value);
            } else if (StringUtils.equals(field, "prerequisite")) {
                data.setPrerequisite(value);
            } else if (StringUtils.equals(field, "remark")) {
                data.setRemark(value);
            } else if (StringUtils.equals(field, "stepDesc")) {
                data.setStepDesc(value);
            } else if (StringUtils.equals(field, "stepResult")) {
                data.setStepResult(value);
            } else if (StringUtils.equals(field, "stepModel")) {
                data.setStepModel(value);
            } else {
                data.getCustomData().put(field, value);
            }
        }
        return data;
    }

    public List<ExcelErrData<TestCaseExcelData>> getErrList() {
        return this.errList;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
        list.clear();
        customFieldsMap.clear();
    }

    private void formatHeadMap() {
        for (Integer key : headMap.keySet()) {
            String name = headMap.get(key);
            if (excelHeadToFieldNameDic.containsKey(name)) {
                headMap.put(key, excelHeadToFieldNameDic.get(name));
            }
        }
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

    class RowInfo {
        public int index;
        public String rowInfo;
    }
}
