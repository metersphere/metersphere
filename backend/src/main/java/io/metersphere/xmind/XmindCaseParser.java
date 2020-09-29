package io.metersphere.xmind;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.i18n.Translator;
import io.metersphere.track.service.TestCaseService;
import io.metersphere.xmind.parser.XmindParser;
import io.metersphere.xmind.parser.pojo.Attached;
import io.metersphere.xmind.parser.pojo.JsonRootBean;
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
    private String maintainer;
    private String projectId;
    private StringBuffer process; // 过程校验记录
    // 已存在用例名称
    private Set<String> testCaseNames;

    // 转换后的案例信息
    private List<TestCaseWithBLOBs> testCases;

    // 案例详情重写了hashCode方法去重用
    private List<TestCaseExcelData> compartDatas;

    // 记录没有用例的目录
    private List<String> nodePaths;

    public XmindCaseParser(TestCaseService testCaseService, String userId, String projectId, Set<String> testCaseNames) {
        this.testCaseService = testCaseService;
        this.maintainer = userId;
        this.projectId = projectId;
        this.testCaseNames = testCaseNames;
        testCases = new LinkedList<>();
        compartDatas = new ArrayList<>();
        process = new StringBuffer();
        nodePaths = new ArrayList<>();
    }

    // 这里清理是为了 加快jvm 回收
    public void clear() {
        compartDatas.clear();
        testCases.clear();
        testCaseNames.clear();
        nodePaths.clear();
    }

    public List<TestCaseWithBLOBs> getTestCase() {
        return this.testCases;
    }

    public List<String> getNodePaths() {
        return this.nodePaths;
    }

    private final Map<String, String> caseTypeMap = ImmutableMap.of("功能测试", "functional", "性能测试", "performance", "接口测试", "api");

    public void validate() {
        nodePaths.forEach(nodePath -> {
            String[] nodes = nodePath.split("/");
            if (nodes.length > TestCaseConstants.MAX_NODE_DEPTH + 1) {
                process.append(Translator.get("test_case_node_level_tip") +
                        TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level") + "; ");
            }
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), "")) {
                    process.append(Translator.get("module_not_null") + "; ");
                } else if (nodes[i].trim().length() > 30) {
                    process.append(nodes[i].trim() + "：" + Translator.get("test_track.length_less_than") + "30 ;");
                }
            }
        });
    }

    // 递归处理案例数据
    private void recursion(StringBuffer processBuffer, Attached parent, int level, List<Attached> attacheds) {
        for (Attached item : attacheds) {
            if (isAvailable(item.getTitle(), "(?:tc：|tc:|tc)")) { // 用例
                item.setParent(parent);
                this.newTestCase(item.getTitle(), parent.getPath(), item.getChildren() != null ? item.getChildren().getAttached() : null);
            } else {
                String nodePath = parent.getPath() + "/" + item.getTitle();
                item.setPath(nodePath);
                item.setParent(parent);
                if (item.getChildren() != null && !item.getChildren().getAttached().isEmpty()) {
                    recursion(processBuffer, item, level + 1, item.getChildren().getAttached());
                } else {
                    if (!nodePath.startsWith("/")) {
                        nodePath = "/" + nodePath;
                    }
                    if (nodePath.endsWith("/")) {
                        nodePath = nodePath.substring(0, nodePath.length() - 1);
                    }
                    nodePaths.add(nodePath); // 没有用例的路径
                }
            }
        }
    }

    private boolean isAvailable(String str, String regex) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(regex))
            return false;
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher result = pattern.matcher(str);
        return result.find();
    }

    private String replace(String str, String regex) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(regex))
            return str;
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher result = pattern.matcher(str);
        str = result.replaceAll("");
        return str;
    }

    // 获取步骤数据
    private String getSteps(List<Attached> attacheds) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < attacheds.size(); i++) {
            // 保持插入顺序，判断用例是否有相同的steps
            JSONObject step = new JSONObject(true);
            step.put("num", i + 1);
            step.put("desc", attacheds.get(i).getTitle());
            if (attacheds.get(i).getChildren() != null && !attacheds.get(i).getChildren().getAttached().isEmpty()) {
                step.put("result", attacheds.get(i).getChildren().getAttached().get(0).getTitle());
            }
            jsonArray.add(step);
        }
        return jsonArray.toJSONString();
    }

    // 初始化一个用例
    private void newTestCase(String title, String nodePath, List<Attached> attacheds) {
        TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
        testCase.setProjectId(projectId);
        testCase.setMaintainer(maintainer);
        testCase.setPriority("P0");
        testCase.setMethod("manual");
        testCase.setType("functional");

        String tc = title.replace("：", ":");
        String tcArr[] = tc.split(":");
        if (tcArr.length != 2) {
            process.append(Translator.get("test_case_name") + "【 " + title + " 】" + Translator.get("incorrect_format"));
            return;
        }
        // 用例名称
        testCase.setName(this.replace(tcArr[1], "tc:|tc：|tc"));

        if (!nodePath.startsWith("/")) {
            nodePath = "/" + nodePath;
        }
        if (nodePath.endsWith("/")) {
            nodePath = nodePath.substring(0, nodePath.length() - 1);
        }
        testCase.setNodePath(nodePath);

        // 用例等级和用例性质处理
        if (tcArr[0].indexOf("-") != -1) {
            String otArr[] = tcArr[0].split("-");
            for (String item : otArr) {
                if (item.toUpperCase().startsWith("P")) {
                    testCase.setPriority(item.toUpperCase());
                } else {
                    Optional.ofNullable(caseTypeMap.get(item)).ifPresent(opt -> testCase.setType(opt));
                }
            }
        }
        // 测试步骤处理
        List<Attached> steps = new LinkedList<>();
        if (attacheds != null && !attacheds.isEmpty()) {
            attacheds.forEach(item -> {
                if (isAvailable(item.getTitle(), "(?:pc:|pc：)")) {
                    testCase.setPrerequisite(replace(item.getTitle(), "(?:pc:|pc：)"));
                } else if (isAvailable(item.getTitle(), "(?:rc:|rc：)")) {
                    testCase.setRemark(replace(item.getTitle(), "(?:rc:|rc：)"));
                } else {
                    steps.add(item);
                }
            });
        }
        if (!steps.isEmpty()) {
            testCase.setSteps(this.getSteps(steps));
        } else {
            JSONArray jsonArray = new JSONArray();
            // 保持插入顺序，判断用例是否有相同的steps
            JSONObject step = new JSONObject(true);
            step.put("num", 1);
            step.put("desc", "");
            step.put("result", "");
            jsonArray.add(step);
            testCase.setSteps(jsonArray.toJSONString());
        }
        TestCaseExcelData compartData = new TestCaseExcelData();
        BeanUtils.copyBean(compartData, testCase);
        if (compartDatas.contains(compartData)) {
            process.append(Translator.get("test_case_already_exists_excel") + "：" + testCase.getName() + "; ");
        } else if (validate(testCase)) {
            testCase.setId(UUID.randomUUID().toString());
            testCase.setCreateTime(System.currentTimeMillis());
            testCase.setUpdateTime(System.currentTimeMillis());
            testCases.add(testCase);
        }
        compartDatas.add(compartData);
    }

    // 验证合法性
    private boolean validate(TestCaseWithBLOBs data) {
        String nodePath = data.getNodePath();
        StringBuilder stringBuilder = new StringBuilder();

        if (!StringUtils.isEmpty(nodePath)) {
            String[] nodes = nodePath.split("/");
            if (nodes.length > TestCaseConstants.MAX_NODE_DEPTH + 1) {
                stringBuilder.append(Translator.get("test_case_node_level_tip") +
                        TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level") + "; ");
            }
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), "")) {
                    stringBuilder.append(Translator.get("module_not_null") + "; ");
                    break;
                } else if (nodes[i].trim().length() > 30) {
                    stringBuilder.append(nodes[i].trim() + "：" + Translator.get("test_track.length_less_than") + "30 ;");
                    break;
                }
            }
        }

        if (StringUtils.equals(data.getType(), TestCaseConstants.Type.Functional.getValue()) && StringUtils.equals(data.getMethod(), TestCaseConstants.Method.Auto.getValue())) {
            stringBuilder.append(Translator.get("functional_method_tip") + "; ");
        }

        if (testCaseNames.contains(data.getName())) {
            boolean dbExist = testCaseService.exist(data);
            if (dbExist) {
                // db exist
                stringBuilder.append(Translator.get("test_case_already_exists_excel") + "：" + data.getName() + "; ");
            }

        } else {
            testCaseNames.add(data.getName());
        }
        if (!StringUtils.isEmpty(stringBuilder.toString())) {
            process.append(stringBuilder.toString());
            return false;
        }
        return true;
    }

    // 导入思维导图处理
    public String parse(MultipartFile multipartFile) {
        StringBuffer processBuffer = new StringBuffer();
        try {
            // 获取思维导图内容
            JsonRootBean root = XmindParser.parseObject(multipartFile);
            if (root != null && root.getRootTopic() != null && root.getRootTopic().getChildren() != null) {
                // 判断是模块还是用例
                for (Attached item : root.getRootTopic().getChildren().getAttached()) {
                    if (isAvailable(item.getTitle(), "(?:tc:|tc：|tc)")) { // 用例
                        return replace(item.getTitle(), "(?:tc:|tc：|tc)") + "：" + Translator.get("test_case_create_module_fail");
                    } else {
                        item.setPath(item.getTitle());
                        if (item.getChildren() != null && !item.getChildren().getAttached().isEmpty()) {
                            recursion(processBuffer, item, 1, item.getChildren().getAttached());
                        }
                    }
                }
            }
            this.validate();
        } catch (Exception ex) {
            processBuffer.append(Translator.get("incorrect_format"));
            LogUtil.error(ex.getMessage());
            return "xmind "+Translator.get("incorrect_format");
        }
        return process.toString();
    }
}
