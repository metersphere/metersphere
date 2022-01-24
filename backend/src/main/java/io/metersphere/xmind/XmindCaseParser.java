package io.metersphere.xmind;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.excel.utils.FunctionCaseImportEnum;
import io.metersphere.i18n.Translator;
import io.metersphere.track.request.testcase.TestCaseImportRequest;
import io.metersphere.track.service.TestCaseService;
import io.metersphere.xmind.parser.XmindParser;
import io.metersphere.xmind.parser.pojo.Attached;
import io.metersphere.xmind.parser.pojo.JsonRootBean;
import io.metersphere.xmind.utils.DetailUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据转换
 * 1 解析Xmind文件 XmindParser.parseObject
 * 2 解析后的JSON this.parse 转成测试用例
 */
public class XmindCaseParser {

    private TestCaseService testCaseService;
    /**
     * 过程校验记录
     */
    private DetailUtil process;
    /**
     * 转换后的案例信息
     */
    private List<TestCaseWithBLOBs> testCases;
    /**
     * 需要更新的用例
     */
    private List<TestCaseWithBLOBs> updateTestCases;

    /**
     * 案例详情重写了hashCode方法去重用
     */
    private List<TestCaseExcelData> compartDatas;
    /**
     * 记录没有用例的目录
     */
    private List<String> nodePaths;

    private List<TestCaseWithBLOBs> continueValidatedCase;

    private List<String> errorPath;

    private TestCaseImportRequest request;

    public XmindCaseParser(TestCaseImportRequest request) {
        this.testCaseService = CommonBeanFactory.getBean(TestCaseService.class);
        this.request = request;
        testCases = new LinkedList<>();
        updateTestCases = new LinkedList<>();
        compartDatas = new ArrayList<>();
        process = new DetailUtil();
        nodePaths = new ArrayList<>();
        continueValidatedCase = new ArrayList<>();
        errorPath = new ArrayList<>();
    }

    private static final String TC_REGEX = "(?:tc:|tc：|tc)";
    private static final String PC_REGEX = "(?:pc:|pc：)";
    private static final String RC_REGEX = "(?:rc:|rc：)";
    private static final String ID_REGEX = "(?:id:|id：)";
    private static final String TAG_REGEX = "(?:tag:|tag：)";

    public void clear() {
        compartDatas.clear();
        testCases.clear();
        updateTestCases.clear();
        request.getTestCaseNames().clear();
        nodePaths.clear();
    }

    public List<TestCaseWithBLOBs> getTestCase() {
        if (StringUtils.equals(request.getImportType(), FunctionCaseImportEnum.Create.name())) {
            return this.testCases;
        } else {
            return new ArrayList<>();
        }
    }

    public List<TestCaseWithBLOBs> getUpdateTestCase() {
        if (StringUtils.equals(request.getImportType(), FunctionCaseImportEnum.Update.name())) {
            return this.updateTestCases;
        } else {
            return new ArrayList<>();
        }
    }

    public List<String> getNodePaths() {
        return this.nodePaths;
    }

    private final Map<String, String> caseTypeMap = ImmutableMap.of("功能测试", "functional", "性能测试", "performance", "接口测试", "api");
    private final List<String> priorityList = Arrays.asList("P0", "P1", "P2", "P3");

    /**
     * 验证模块的合规性
     */
    public void validate() {
        nodePaths.forEach(nodePath -> {
            String[] nodes = nodePath.split("/");
            if (nodes.length > TestCaseConstants.MAX_NODE_DEPTH + 1) {
                process.add(Translator.get("test_case_node_level_tip") +
                        TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level"), nodePath);
            }
            String path = "";
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), "")) {
                    process.add(Translator.get("module_not_null"), path);
                } else if (nodes[i].trim().length() > 100) {
                    process.add(Translator.get("module") + Translator.get("test_track.length_less_than") + "100", path + nodes[i].trim());
                } else {
                    path += nodes[i].trim() + "/";
                }
            }
        });
    }

    /**
     * 验证用例的合规性
     */
    private boolean validate(TestCaseWithBLOBs data) {
        boolean validatePass = true;
        String nodePath = data.getNodePath();
        if (!nodePath.startsWith("/")) {
            nodePath = "/" + nodePath;
        }
        if (nodePath.endsWith("/")) {
            nodePath = nodePath.substring(0, nodePath.length() - 1);
        }
        data.setNodePath(nodePath);


        //用例名称判断
        if (StringUtils.isEmpty(data.getName())) {
            validatePass = false;
            process.add("name" + Translator.get("cannot_be_null"), nodePath + "");
        } else {
            if (data.getName().length() > 200) {
                validatePass = false;
                process.add(Translator.get("test_case") + Translator.get("test_track.length_less_than") + "200", nodePath + data.getName());
            }
        }


        if (!StringUtils.isEmpty(nodePath)) {
            String[] nodes = nodePath.split("/");
            if (nodes.length > TestCaseConstants.MAX_NODE_DEPTH + 1) {
                validatePass = false;
                process.add(Translator.get("test_case_node_level_tip") +
                        TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level"), nodePath);
                if (!errorPath.contains(nodePath)) {
                    errorPath.add(nodePath);
                }
            }
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), "")) {
                    validatePass = false;
                    process.add(Translator.get("test_case") + Translator.get("module_not_null"), nodePath + data.getName());
                    if (!errorPath.contains(nodePath)) {
                        errorPath.add(nodePath);
                    }
                    break;
                } else if (nodes[i].trim().length() > 100) {
                    validatePass = false;
                    process.add(Translator.get("module") + Translator.get("test_track.length_less_than") + "100 ", nodes[i].trim());
                    if (!errorPath.contains(nodePath)) {
                        errorPath.add(nodePath);
                    }
                    break;
                }
            }
        }

        if (StringUtils.equals(data.getType(), TestCaseConstants.Type.Functional.getValue()) && StringUtils.equals(data.getMethod(), TestCaseConstants.Method.Auto.getValue())) {
            validatePass = false;
            process.add(Translator.get("functional_method_tip"), nodePath + data.getName());
        }

        // 用例等级和用例性质处理
        if (!priorityList.contains(data.getPriority())) {
            validatePass = false;
            process.add(Translator.get("test_case_priority") + Translator.get("incorrect_format"), nodePath + data.getName());
        }
        if (data.getType() == null) {
            validatePass = false;
            process.add(Translator.get("test_case_type") + Translator.get("incorrect_format"), nodePath + data.getName());
        }

        // 重复用例校验
        TestCaseExcelData compartData = new TestCaseExcelData();
        BeanUtils.copyBean(compartData, data);
        if (compartDatas.contains(compartData)) {
            validatePass = false;
            process.add(Translator.get("test_case_already_exists_excel"), nodePath + "/" + compartData.getName());
        }
        compartDatas.add(compartData);


        String importType = request.getImportType();
        String projectId = request.getProjectId();
        boolean isUseCustomId = request.isUseCustomId();

        //自定义ID判断
        if (StringUtils.isEmpty(data.getCustomNum())) {
            if (StringUtils.equals(importType, FunctionCaseImportEnum.Update.name())) {
                process.add(Translator.get("id_required"), nodePath + "/" + compartData.getName());
                return false;
            } else {
                if (isUseCustomId) {
                    process.add(Translator.get("custom_num_is_not_exist"), nodePath + "/" + compartData.getName());
                    return false;
                }
            }
        }

        //判断更新
        if (StringUtils.equals(importType, FunctionCaseImportEnum.Update.name())) {
            String checkResult;
            if (isUseCustomId) {
                checkResult = testCaseService.checkCustomIdExist(data.getCustomNum(), projectId);
            } else {
                checkResult = testCaseService.checkIdExist(Integer.parseInt(data.getCustomNum()), projectId);
            }
            if (null != checkResult) {  //该ID在当前项目中存在
                //如果前面所经过的校验都没报错
                if (!isUseCustomId) {
                    data.setNum(Integer.parseInt(data.getCustomNum()));
                    data.setCustomNum(null);
                }
                data.setId(checkResult);
                updateTestCases.add(data);
            } else {
                process.add(Translator.get("custom_num_is_not_exist"), nodePath + "/" + compartData.getName());
                validatePass = false;
            }
        }

        // 判断新增
        if (StringUtils.equals(importType, FunctionCaseImportEnum.Create.name())) {
            String checkResult;
            // 针对自定义 ID 的情况做重复判断
            if (isUseCustomId) {
                checkResult = testCaseService.checkCustomIdExist(data.getCustomNum(), projectId);
                if (null != checkResult) {  //该ID在当前项目中存在
                    process.add(Translator.get("custom_num_is_exist"), nodePath + "/" + compartData.getName());
                    return false;
                }
            }
        }

        if (validatePass) {
            this.continueValidatedCase.add(data);
        }
        return true;
    }

    /**
     * 递归处理案例数据
     */
    private void recursion(Attached parent, int level, List<Attached> attacheds) {
        for (Attached item : attacheds) {
            if (isAvailable(item.getTitle(), TC_REGEX)) {
                item.setParent(parent);
                // 格式化一个用例
                this.formatTestCase(item.getTitle(), parent.getPath(), item.getChildren() != null ? item.getChildren().getAttached() : null);
            } else {
                String nodePath = parent.getPath().trim() + "/" + item.getTitle().trim();
                item.setPath(nodePath);
                item.setParent(parent);
                if (item.getChildren() != null && CollectionUtils.isNotEmpty(item.getChildren().getAttached())) {
                    recursion(item, level + 1, item.getChildren().getAttached());
                } else {
                    if (!nodePath.startsWith("/")) {
                        nodePath = "/" + nodePath;
                    }
                    if (nodePath.endsWith("/")) {
                        nodePath = nodePath.substring(0, nodePath.length() - 1);
                    }
                    // 没有用例的路径
                    nodePaths.add(nodePath);
                }
            }
        }
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
        str = result.replaceAll("");
        return str;
    }

    /**
     * 获取步骤数据
     */
    private String getSteps(List<Attached> attacheds) {
        JSONArray jsonArray = new JSONArray();
        if (!attacheds.isEmpty()) {
            for (int i = 0; i < attacheds.size(); i++) {
                // 保持插入顺序，判断用例是否有相同的steps
                JSONObject step = new JSONObject(true);
                step.put("num", i + 1);
                step.put("desc", attacheds.get(i).getTitle());
                if (attacheds.get(i) != null && attacheds.get(i).getChildren() != null && attacheds.get(i).getChildren().getAttached() != null) {
                    step.put("result", attacheds.get(i).getChildren().getAttached().get(0).getTitle());
                }
                jsonArray.add(step);
            }
        } else {
            // 保持插入顺序，判断用例是否有相同的steps
            JSONObject step = new JSONObject(true);
            step.put("num", 1);
            step.put("desc", "");
            step.put("result", "");
            jsonArray.add(step);
        }
        return jsonArray.toJSONString();
    }

    /**
     * 格式化一个用例
     */
    private void formatTestCase(String title, String nodePath, List<Attached> attacheds) {
        TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
        testCase.setProjectId(request.getProjectId());
        testCase.setMaintainer(request.getUserId());
        testCase.setPriority(priorityList.get(0));
        testCase.setMethod("manual");
        testCase.setType("functional");

        String tc = title.replace("：", ":");
        String[] tcArrs = tc.split(":");
        if (tcArrs.length < 1) {
            process.add(Translator.get("test_case_name") + Translator.get("incorrect_format"), title);
            return;
        }
        // 用例名称
        String name = title.replace(tcArrs[0] + "：", "").replace(tcArrs[0] + ":", "");
        testCase.setName(name);
        testCase.setNodePath(nodePath.trim());

        // 用例等级和用例性质处理
        if (tcArrs[0].indexOf("-") != -1) {
            for (String item : tcArrs[0].split("-")) {
                if (isAvailable(item, TC_REGEX)) {
                    continue;
                } else if (item.toUpperCase().startsWith("P")) {
                    testCase.setPriority(item.toUpperCase());
                } else {
                    testCase.setType(caseTypeMap.get(item));
                }
            }
        }

        // 测试步骤处理
        List<Attached> steps = new LinkedList<>();
        StringBuilder rc = new StringBuilder();
        List<String> tags = new LinkedList<>();
        StringBuilder customId = new StringBuilder();
        if (attacheds != null && !attacheds.isEmpty()) {
            attacheds.forEach(item -> {
                if (isAvailable(item.getTitle(), PC_REGEX)) {
                    testCase.setPrerequisite(replace(item.getTitle(), PC_REGEX));
                } else if (isAvailable(item.getTitle(), RC_REGEX)) {
                    rc.append(replace(item.getTitle(), RC_REGEX));
                    rc.append("\n");
                } else if (isAvailable(item.getTitle(), TAG_REGEX)) {
                    String tag = replace(item.getTitle(), TAG_REGEX);
                    if (StringUtils.isNotEmpty(tag)) {
                        tags.add(tag);
                    }
                } else if (isAvailable(item.getTitle(), ID_REGEX)) {
                    customId.append(replace(item.getTitle(), ID_REGEX));
                } else {
                    steps.add(item);
                }
            });
        }
        testCase.setRemark(rc.toString());
        if (request.isUseCustomId() || StringUtils.equals(request.getImportType(), FunctionCaseImportEnum.Update.name())) {
            testCase.setCustomNum(customId.toString());
        }

        List<String> tagsNew = new ArrayList();
        if (CollectionUtils.isNotEmpty(tags)) {
            String tagsData = tags.get(0);
            String[] array = tagsData.split("[, ，]");
            Arrays.asList(array).stream().forEach(x -> tagsNew.add(x));
        }
        testCase.setTags(JSON.toJSONString(tagsNew));
        testCase.setSteps(this.getSteps(steps));
        // 校验合规性
        if (validate(testCase)) {
            testCases.add(testCase);
        }
    }

    /**
     * 导入思维导图处理
     */
    public List<ExcelErrData<TestCaseExcelData>> parse(MultipartFile multipartFile) {
        try {
            // 获取思维导图内容
            List<JsonRootBean> roots = XmindParser.parseObject(multipartFile);
            for (JsonRootBean root : roots) {
                if (root != null && root.getRootTopic() != null && root.getRootTopic().getChildren() != null) {
                    // 判断是模块还是用例
                    for (Attached item : root.getRootTopic().getChildren().getAttached()) {
                        // 用例
                        if (isAvailable(item.getTitle(), TC_REGEX)) {
                            return process.parse(replace(item.getTitle(), TC_REGEX) + "：" + Translator.get("test_case_create_module_fail"));
                        } else {
                            String nodePath = item.getTitle();
                            item.setPath(nodePath);
                            if (item.getChildren() != null && !item.getChildren().getAttached().isEmpty()) {
                                // 递归处理案例数据
                                recursion(item, 1, item.getChildren().getAttached());
                            } else {
                                if (!nodePath.startsWith("/")) {
                                    nodePath = "/" + nodePath;
                                }
                                if (nodePath.endsWith("/")) {
                                    nodePath = nodePath.substring(0, nodePath.length() - 1);
                                }
                                // 没有用例的路径
                                nodePaths.add(nodePath);
                            }
                        }
                    }
                }
            }
            //检查目录合规性
            this.validate();
        } catch (Exception ex) {
            ex.printStackTrace();
            return process.parse(ex.getMessage());
        }
        return process.parse();
    }

    public List<TestCaseWithBLOBs> getContinueValidatedCase() {
        return this.continueValidatedCase;
    }

    public List<String> getValidatedNodePath() {
        List<String> returnPathList = new ArrayList<>(nodePaths);
        if (CollectionUtils.isNotEmpty(returnPathList)) {
            if (CollectionUtils.isNotEmpty(errorPath)) {
                returnPathList.removeAll(errorPath);
            }
        }
        return returnPathList;
    }
}
