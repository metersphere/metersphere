package io.metersphere.functional.xmind.parser;

import io.metersphere.functional.constants.FunctionalCaseTypeConstants;
import io.metersphere.functional.dto.FunctionalCaseStepDTO;
import io.metersphere.functional.excel.domain.FunctionalCaseExcelData;
import io.metersphere.functional.excel.exception.CustomFieldValidateException;
import io.metersphere.functional.excel.validate.AbstractCustomFieldValidator;
import io.metersphere.functional.excel.validate.CustomFieldValidatorFactory;
import io.metersphere.functional.request.FunctionalCaseImportRequest;
import io.metersphere.functional.service.FunctionalCaseModuleService;
import io.metersphere.functional.service.FunctionalCaseService;
import io.metersphere.functional.xmind.pojo.Attached;
import io.metersphere.functional.xmind.pojo.JsonRootBean;
import io.metersphere.functional.xmind.utils.DetailUtil;
import io.metersphere.plugin.platform.enums.PlatformCustomFieldType;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.excel.domain.ExcelErrData;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * 数据转换
 * 1 解析Xmind文件 XmindParser.parseObject
 * 2 解析后的JSON this.parse 转成测试用例
 */
public class XMindCaseParser {

    Map<String, TemplateCustomFieldDTO> customFieldsMap;
    @Getter
    protected List<FunctionalCaseExcelData> list = new ArrayList<>();
    @Getter
    protected List<FunctionalCaseExcelData> checkSuccessList = new ArrayList<>();
    @Getter
    protected List<FunctionalCaseExcelData> updateList = new ArrayList<>();

    private FunctionalCaseService functionalCaseService;

    private FunctionalCaseImportRequest request;
    /**
     * 所有的模块集合
     */
    private List<BaseTreeNode> moduleTree;
    private SessionUser user;
    private Map<String, String> pathMap = new HashMap<>();
    protected static final int TAGS_COUNT = 10;
    protected static final int TAG_LENGTH = 64;
    protected static final int STEP_LENGTH = 1000;
    private AtomicLong lastPos;
    private HashMap<String, AbstractCustomFieldValidator> customFieldValidatorMap;
    /**
     * 过程校验记录
     */
    private DetailUtil process;

    public XMindCaseParser(FunctionalCaseImportRequest request, List<TemplateCustomFieldDTO> customFields, SessionUser user, Long pos) {
        //当前项目模板的自定义字段
        this.request = request;
        customFieldsMap = customFields.stream().collect(Collectors.toMap(TemplateCustomFieldDTO::getFieldName, i -> i));
        functionalCaseService = CommonBeanFactory.getBean(FunctionalCaseService.class);
        moduleTree = CommonBeanFactory.getBean(FunctionalCaseModuleService.class).getTree(request.getProjectId());
        customFieldValidatorMap = CustomFieldValidatorFactory.getValidatorMap(request.getProjectId());
        lastPos = new AtomicLong(pos);
        this.user = user;
        process = new DetailUtil();
    }

    private static final String ID = "(?:id:|id：|Id:|Id：|iD:|iD：)";
    private static final String CASE = "((?i)case)";
    private static final String PREREQUISITE = "(?:" + Translator.get("xmind_prerequisite") + ":|" + Translator.get("xmind_prerequisite") + "：)";
    private static final String STEP = "(?:" + Translator.get("xmind_step") + ":|" + Translator.get("xmind_step") + "：)";
    private static final String STEP_DESCRIPTION = Translator.get("xmind_stepDescription");
    private static final String TEXT_DESCRIPTION = "(?:" + Translator.get("xmind_textDescription") + ":|" + Translator.get("xmind_textDescription") + "：)";
    private static final String EXPECTED_RESULT = "(?:" + Translator.get("xmind_expectedResult") + ":|" + Translator.get("xmind_expectedResult") + "：)";
    private static final String DESCRIPTION = "(?:" + Translator.get("xmind_description") + ":|" + Translator.get("xmind_description") + "：)";
    private static final String TAGS = "(?:" + Translator.get("xmind_tags") + ":|" + Translator.get("xmind_tags") + "：)";


    public void clear() {
        list.clear();
        updateList.clear();
        checkSuccessList.clear();
        pathMap.clear();
        moduleTree = new ArrayList<>();
        customFieldValidatorMap = new HashMap<>();
        lastPos = null;
    }

    private boolean isAvailable(String str, String regex) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(regex)) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher result = pattern.matcher(str);
        return result.find();
    }

    private String replace(String str, String regex) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(regex)) {
            return str;
        }
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher result = pattern.matcher(str);
        str = result.replaceAll(StringUtils.EMPTY);
        return str;
    }

    /**
     * 递归处理案例数据
     */
    private void recursion(Attached parent, int level, List<Attached> attachedList) {
        for (Attached item : attachedList) {
            if (isAvailable(item.getTitle(), CASE)) {
                item.setParent(parent);
                // 格式化一个用例
                this.formatTestCase(item.getTitle(), parent.getPath(), item.getChildren() != null ? item.getChildren().getAttached() : null);
            } else {
                if (StringUtils.equalsIgnoreCase(parent.getPath().trim(), Translator.get("functional_case.module.default.name"))) {
                    process.add(Translator.get("incorrect_format"), Translator.get("functional_case.module.default.name.add_error"));
                    return;
                }
                String nodePath = parent.getPath().trim() + "/" + item.getTitle().trim();
                item.setPath(nodePath);
                item.setParent(parent);
                if (item.getChildren() != null && CollectionUtils.isNotEmpty(item.getChildren().getAttached())) {
                    recursion(item, level + 1, item.getChildren().getAttached());
                }
            }
        }
    }

    /**
     * 验证用例的合规性
     */
    public boolean validate(FunctionalCaseExcelData data) {
        //模块校验
        boolean validate;
        validate = validateModule(data);
        if (!validate) {
            return false;
        }
        //校验自定义字段
        validate = validateCustomField(data);
        if (!validate) {
            return false;
        }
        //校验id
        validate = validateIdExist(data);
        if (!validate) {
            return false;
        }
        //标签长度校验
        validate = validateTags(data);
        return validate;
    }


    /**
     * 校验标签长度 个数
     */
    private boolean validateTags(FunctionalCaseExcelData data) {
        AtomicBoolean validate = new AtomicBoolean(true);
        if (StringUtils.isBlank(data.getTags())) {
            data.setTags(StringUtils.EMPTY);
            return validate.get();
        }
        List<String> tags = functionalCaseService.handleImportTags(data.getTags());
        if (tags.size() > TAGS_COUNT) {
            process.add(data.getName(), Translator.get("tags_count"));
            return false;
        }
        tags.forEach(tag -> {
            if (tag.length() > TAG_LENGTH) {
                process.add(data.getName(), Translator.get("tag_length"));
                validate.set(false);
            }
        });
        return validate.get();
    }


    /**
     * 校验模块
     */
    private boolean validateModule(FunctionalCaseExcelData data) {
        boolean validate = true;
        String module = data.getModule();
        if (StringUtils.isNotEmpty(module)) {
            String[] nodes = module.split("/");
            //模块名不能为空
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), StringUtils.EMPTY)) {
                    validate = false;
                    break;
                }
            }
            //增加字数校验，每一层不能超过100个字
            for (String nodeStr : nodes) {
                if (StringUtils.isNotEmpty(nodeStr)) {
                    if (nodeStr.trim().length() > 100) {
                        validate = false;
                        break;
                    }
                }
            }
        }
        return validate;
    }

    /**
     * 校验Excel中是否有ID
     * 是否覆盖：
     * 1.覆盖：id存在则更新，id不存在则新增
     * 2.不覆盖：id存在不处理，id不存在新增
     */
    @Nullable
    private boolean validateIdExist(FunctionalCaseExcelData data) {
        boolean validate = true;
        //当前读取的数据有ID
        if (StringUtils.isNotEmpty(data.getNum())) {
            Integer num = -1;
            try {
                num = Integer.parseInt(data.getNum());
            } catch (Exception e) {
                validate = false;
                process.add(data.getName(), Translator.get("id_not_rightful") + "[" + data.getNum() + "]");
            }
            if (num < 0) {
                validate = false;
                process.add(data.getName(), Translator.get("id_not_rightful") + "[" + data.getNum() + "]");
            }
        }
        return validate;
    }

    /**
     * 校验自定义字段，并记录错误提示
     * 如果填写的是自定义字段的选项值，则转换成ID保存
     */
    private boolean validateCustomField(FunctionalCaseExcelData data) {
        boolean validate = true;
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
                if (customFieldValidator.isKVOption) {
                    // 这里如果填的是选项值，替换成选项ID，保存
                    customData.put(fieldName, customFieldValidator.parse2Key(value.toString(), templateCustomFieldDTO));
                }
            } catch (CustomFieldValidateException e) {
                validate = false;
                process.add(data.getName(), e.getMessage());
            }
        }
        return validate;
    }

    /**
     * 处理新增数据集合还是更新数据集合
     *
     * @param functionalCaseExcelData
     */
    private void handleId(FunctionalCaseExcelData functionalCaseExcelData) {
        if (StringUtils.isNotEmpty(functionalCaseExcelData.getNum())) {
            String checkResult = functionalCaseService.checkNumExist(functionalCaseExcelData.getNum(), request.getProjectId());
            if (StringUtils.isNotEmpty(checkResult)) {
                if (request.isCover()) {
                    //如果是覆盖，那么有id的需要更新
                    functionalCaseExcelData.setNum(checkResult);
                    updateList.add(functionalCaseExcelData);
                } else {
                    checkSuccessList.add(functionalCaseExcelData);
                }
            } else {
                list.add(functionalCaseExcelData);
            }
        } else {
            list.add(functionalCaseExcelData);
        }
    }


    /**
     * 格式化一个用例
     */
    private void formatTestCase(String title, String nodePath, List<Attached> attacheds) {
        FunctionalCaseExcelData testCase = new FunctionalCaseExcelData();
        String tc = title.replace("：", ":");
        String[] tcArrs = tc.split(":", 2);
        if (tcArrs.length <= 1) {
            process.add(Translator.get("test_case_name") + Translator.get("incorrect_format"), title);
            return;
        }
        // 用例名称
        String name = title.replace(tcArrs[0] + "：", StringUtils.EMPTY).replace(tcArrs[0] + ":", StringUtils.EMPTY);
        if (name.length() >= 255) {
            process.add(Translator.get("test_case_name") + Translator.get("length.too.large"), title);
            return;
        }
        testCase.setName(name);
        nodePath = nodePath.trim();
        if (!nodePath.startsWith("/")) {
            nodePath = "/" + nodePath;
        }
        if (nodePath.endsWith("/")) {
            nodePath = nodePath.substring(0, nodePath.length() - 1);
        }
        testCase.setModule(nodePath);

        // 用例id， blobs， tags, 自定义字段处理
        StringBuilder customId = new StringBuilder();
        if (attacheds != null && !attacheds.isEmpty()) {
            attacheds.forEach(item -> {
                //id
                if (isAvailable(item.getTitle(), ID)) {
                    customId.append(replace(item.getTitle(), ID));
                    testCase.setNum(customId.toString());
                } else if (isAvailable(item.getTitle(), PREREQUISITE)) {
                    testCase.setPrerequisite(replace(item.getTitle(), PREREQUISITE));
                } else if (isAvailable(item.getTitle(), TEXT_DESCRIPTION)) {
                    testCase.setTextDescription(replace(item.getTitle(), TEXT_DESCRIPTION));
                    testCase.setCaseEditType(FunctionalCaseTypeConstants.CaseEditType.TEXT.name());
                    if (item.getChildren() != null) {
                        testCase.setExpectedResult(getTextSteps(item.getChildren().getAttached()));
                    }
                } else if (isAvailable(item.getTitle(), DESCRIPTION)) {
                    testCase.setDescription(replace(item.getTitle(), DESCRIPTION));
                } else if (isAvailable(item.getTitle(), TAGS)) {
                    String tag = replace(item.getTitle(), TAGS);
                    if (StringUtils.isBlank(tag)) {
                        testCase.setTags("");
                    } else {
                        String[] tagArr = tag.split("\\s*[|｜]\\s*");
                        if (CollectionUtils.isNotEmpty(Arrays.asList(tagArr))) {
                            testCase.setTags(String.join(",", tagArr));
                        }
                    }

                } else if (isAvailable(item.getTitle(), STEP_DESCRIPTION)) {
                    if (item.getChildren() != null) {
                        testCase.setSteps(this.getSteps(item.getChildren().getAttached(), title));
                        testCase.setCaseEditType(FunctionalCaseTypeConstants.CaseEditType.STEP.name());
                    }
                } else {
                    //自定义字段
                    String[] customFiled = item.getTitle().split("(?:\\s*:|：)", 2);
                    Map<String, Object> stringObjectMap = testCase.getCustomData();
                    if (customFiled.length > 1) {
                        TemplateCustomFieldDTO templateCustomFieldDTO = customFieldsMap.get(customFiled[0]);
                        if (templateCustomFieldDTO != null) {
                            stringObjectMap.put(customFiled[0], customFiled[1].replaceAll("^\\s+", ""));
                        }
                    } else {
                        TemplateCustomFieldDTO templateCustomFieldDTO = customFieldsMap.get(customFiled[0]);
                        if (templateCustomFieldDTO != null) {
                            stringObjectMap.put(customFiled[0], StringUtils.EMPTY);
                        }
                    }

                    testCase.setCustomData(stringObjectMap);
                }
            });
        }
        boolean validate = validate(testCase);
        if (validate) {
            handleId(testCase);
        }
    }

    /**
     * 执行保存数据
     */
    public void saveData() {
        if (CollectionUtils.isNotEmpty(list)) {
            functionalCaseService.saveImportData(list, request, moduleTree, customFieldsMap, pathMap, user, lastPos);
        }

        if (CollectionUtils.isNotEmpty(updateList)) {
            functionalCaseService.updateImportData(updateList, request, moduleTree, customFieldsMap, pathMap, user);
        }
    }

    /**
     * 获取步骤数据
     */
    private String getSteps(List<Attached> attacheds, String caseName) {
        List<FunctionalCaseStepDTO> functionalCaseStepDTOS = new ArrayList<>();
        if (attacheds != null && !attacheds.isEmpty()) {
            for (int i = 0; i < attacheds.size(); i++) {
                // 保持插入顺序，判断用例是否有相同的steps
                FunctionalCaseStepDTO functionalCaseStepDTO = new FunctionalCaseStepDTO();
                functionalCaseStepDTO.setId(UUID.randomUUID().toString());
                functionalCaseStepDTO.setNum(i + 1);
                if (isAvailable(attacheds.get(i).getTitle(), STEP)) {
                    String stepDesc = attacheds.get(i).getTitle().replace("：", ":");
                    String[] stepDescArrs = stepDesc.split(":", 2);
                    functionalCaseStepDTO.setDesc(stepDescArrs.length < 2 ? StringUtils.EMPTY : stepDescArrs[1]);
                } else {
                    functionalCaseStepDTO.setDesc(StringUtils.EMPTY);
                }
                if (functionalCaseStepDTO.getDesc().length() > STEP_LENGTH) {
                    process.add(caseName, Translator.get("step_length"));
                    functionalCaseStepDTO.setResult(StringUtils.EMPTY);
                    return JSON.toJSONString(functionalCaseStepDTOS);
                }
                if (attacheds.get(i) != null && attacheds.get(i).getChildren() != null && attacheds.get(i).getChildren().getAttached() != null) {
                    String title = attacheds.get(i).getChildren().getAttached().get(0).getTitle();
                    if (isAvailable(title, EXPECTED_RESULT)) {
                        String stepDesc = title.replace("：", ":");
                        String[] stepDescArrs = stepDesc.split(":", 2);
                        functionalCaseStepDTO.setResult(stepDescArrs.length < 2 ? StringUtils.EMPTY : stepDescArrs[1]);
                    } else {
                        functionalCaseStepDTO.setResult(StringUtils.EMPTY);
                    }
                }
                if (StringUtils.isNotBlank(functionalCaseStepDTO.getResult()) && functionalCaseStepDTO.getResult().length() > STEP_LENGTH) {
                    process.add(caseName, Translator.get("result_length"));
                    return JSON.toJSONString(functionalCaseStepDTOS);
                }
                functionalCaseStepDTOS.add(functionalCaseStepDTO);
            }
        } else {
            // 保持插入顺序，判断用例是否有相同的steps
            FunctionalCaseStepDTO functionalCaseStepDTO = new FunctionalCaseStepDTO();
            functionalCaseStepDTO.setId(UUID.randomUUID().toString());
            functionalCaseStepDTO.setNum(1);
            functionalCaseStepDTO.setDesc(StringUtils.EMPTY);
            functionalCaseStepDTO.setResult(StringUtils.EMPTY);
            functionalCaseStepDTOS.add(functionalCaseStepDTO);
        }
        return JSON.toJSONString(functionalCaseStepDTOS);
    }

    /**
     * 获取步骤数据
     */
    private String getTextSteps(List<Attached> attacheds) {
        if (attacheds.get(0) != null) {
            String title = attacheds.get(0).getTitle();
            if (isAvailable(title, EXPECTED_RESULT)) {
                String stepDesc = title.replace("：", ":");
                String[] stepDescArrs = stepDesc.split(":", 2);
                return StringUtils.isNotBlank(stepDescArrs[1]) ? stepDescArrs[1] : StringUtils.EMPTY;
            } else {
                return StringUtils.EMPTY;
            }
        } else {
            return StringUtils.EMPTY;
        }

    }

    /**
     * 导入思维导图处理
     */
    public List<ExcelErrData<FunctionalCaseExcelData>> parse(MultipartFile multipartFile) {
        try {
            // 获取思维导图内容
            List<JsonRootBean> roots = XMindParser.parseObject(multipartFile);
            for (JsonRootBean root : roots) {
                if (root != null && root.getRootTopic() != null && root.getRootTopic().getChildren() != null) {
                    // 判断是模块还是用例
                    for (Attached item : root.getRootTopic().getChildren().getAttached()) {
                        // 用例
                        if (isAvailable(item.getTitle(), CASE)) {
                            return process.parse(replace(item.getTitle(), CASE) + "：" + Translator.get("test_case_create_module_fail"));
                        } else {
                            String modulePath = item.getTitle();
                            if (StringUtils.isBlank(modulePath)) {
                                return process.parse(Translator.get("module_not_null"));
                            }
                            item.setPath(modulePath);
                            if (item.getChildren() != null && !item.getChildren().getAttached().isEmpty()) {
                                // 递归处理案例数据
                                recursion(item, 1, item.getChildren().getAttached());
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LogUtils.info(ex.getMessage());
            return process.parse(ex.getMessage());
        }
        return process.parse();
    }

}
