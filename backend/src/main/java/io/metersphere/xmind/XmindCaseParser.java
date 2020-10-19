package io.metersphere.xmind;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.utils.BeanUtils;
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
    /**
     * 过程校验记录
     */
    private StringBuffer process;
    /**
     * 已存在用例名称
     */
    private Set<String> testCaseNames;
    /**
     * 转换后的案例信息
     */
    private List<TestCaseWithBLOBs> testCases;
    /**
     * 案例详情重写了hashCode方法去重用
     */
    private List<TestCaseExcelData> compartDatas;
    /**
     * 记录没有用例的目录
     */
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

    private static final String TC_REGEX = "(?:tc:|tc：|tc)";
    private static final String PC_REGEX = "(?:pc:|pc：|pc)";
    private static final String RC_REGEX = "(?:rc:|rc：|rc)";

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
    private final List<String> priorityList = Arrays.asList("P0", "P1", "P2", "P3");

    /**
     * 验证模块的合规性
     */
    public void validate() {
        nodePaths.forEach(nodePath -> {
            String[] nodes = nodePath.split("/");
            if (nodes.length > TestCaseConstants.MAX_NODE_DEPTH + 1) {
                process.append(Translator.get("test_case_node_level_tip") +
                        TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level") + "; ");
            }
            String path = "";
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), "")) {
                    process.append(Translator.get("module") + "：【" + path + "】" + Translator.get("module_not_null") + "; ");
                } else if (nodes[i].trim().length() > 30) {
                    process.append(nodes[i].trim() + "：" + Translator.get("test_track.length_less_than") + "30 ;");
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
        String nodePath = data.getNodePath();
        if (!nodePath.startsWith("/")) {
            nodePath = "/" + nodePath;
        }
        if (nodePath.endsWith("/")) {
            nodePath = nodePath.substring(0, nodePath.length() - 1);
        }
        data.setNodePath(nodePath);

        StringBuilder stringBuilder = new StringBuilder();

        if (data.getName().length() > 50) {
            stringBuilder.append(data.getName() + "：" + Translator.get("test_case") + Translator.get("test_track.length_less_than") + "50 ;");
        }

        if (!StringUtils.isEmpty(nodePath)) {
            String[] nodes = nodePath.split("/");
            if (nodes.length > TestCaseConstants.MAX_NODE_DEPTH + 1) {
                stringBuilder.append(Translator.get("test_case_node_level_tip") +
                        TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level") + "; ");
            }
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), "")) {
                    stringBuilder.append(Translator.get("test_case") + "：【" + data.getName() + "】" + Translator.get("module_not_null") + "; ");
                    break;
                } else if (nodes[i].trim().length() > 30) {
                    stringBuilder.append(nodes[i].trim() + "：" + Translator.get("module") + Translator.get("test_track.length_less_than") + "30 ;");
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
                stringBuilder.append(Translator.get("test_case_already_exists_excel") + "：" + data.getName() + "; ");
            }
        } else {
            testCaseNames.add(data.getName());
        }

        // 用例等级和用例性质处理
        if (!priorityList.contains(data.getPriority())) {
            stringBuilder.append(data.getName() + "：" + Translator.get("test_case_priority") + Translator.get("incorrect_format") + "; ");
        }
        if (data.getType() == null) {
            stringBuilder.append(data.getName() + "：" + Translator.get("test_case_type") + Translator.get("incorrect_format") + "; ");
        }


        // 重复用例校验
        TestCaseExcelData compartData = new TestCaseExcelData();
        BeanUtils.copyBean(compartData, data);
        if (compartDatas.contains(compartData)) {
            stringBuilder.append(Translator.get("test_case_already_exists_excel") + "：" + compartData.getName() + "; ");
        }
        if (!StringUtils.isEmpty(stringBuilder.toString())) {
            process.append(stringBuilder.toString());
            return false;
        }
        compartDatas.add(compartData);

        return true;
    }

    /**
     * 递归处理案例数据
     */
    private void recursion(Attached parent, int level, List<Attached> attacheds) {
        for (Attached item : attacheds) {
            if (isAvailable(item.getTitle(), TC_REGEX)) {
                item.setParent(parent);
                this.formatTestCase(item.getTitle(), parent.getPath(), item.getChildren() != null ? item.getChildren().getAttached() : null);
            } else {
                String nodePath = parent.getPath() + "/" + item.getTitle();
                item.setPath(nodePath);
                item.setParent(parent);
                if (item.getChildren() != null && !item.getChildren().getAttached().isEmpty()) {
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
                if (attacheds.get(i).getChildren() != null && !attacheds.get(i).getChildren().getAttached().isEmpty()) {
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
        testCase.setProjectId(projectId);
        testCase.setMaintainer(maintainer);
        testCase.setPriority(priorityList.get(0));
        testCase.setMethod("manual");
        testCase.setType("functional");

        String tc = title.replace("：", ":");
        String[] tcArr = tc.split(":");
        if (tcArr.length != 2) {
            process.append(Translator.get("test_case_name") + "【 " + title + " 】" + Translator.get("incorrect_format"));
            return;
        }
        // 用例名称
        testCase.setName(this.replace(tcArr[1], TC_REGEX));
        testCase.setNodePath(nodePath);

        // 用例等级和用例性质处理
        if (tcArr[0].indexOf("-") != -1) {
            for (String item : tcArr[0].split("-")) {
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
        if (attacheds != null && !attacheds.isEmpty()) {
            attacheds.forEach(item -> {
                if (isAvailable(item.getTitle(), PC_REGEX)) {
                    testCase.setPrerequisite(replace(item.getTitle(), PC_REGEX));
                } else if (isAvailable(item.getTitle(), RC_REGEX)) {
                    testCase.setRemark(replace(item.getTitle(), RC_REGEX));
                } else {
                    steps.add(item);
                }
            });
        }
        testCase.setSteps(this.getSteps(steps));

        // 校验合规性
        if (validate(testCase)) {
            testCases.add(testCase);
        }
    }

    /**
     * 导入思维导图处理
     */
    public String parse(MultipartFile multipartFile) {
        try {
            // 获取思维导图内容
            List<JsonRootBean> roots = XmindParser.parseObject(multipartFile);
            for (JsonRootBean root : roots) {
                if (root != null && root.getRootTopic() != null && root.getRootTopic().getChildren() != null) {
                    // 判断是模块还是用例
                    for (Attached item : root.getRootTopic().getChildren().getAttached()) {
                        // 用例
                        if (isAvailable(item.getTitle(), TC_REGEX)) {
                            return replace(item.getTitle(), TC_REGEX) + "：" + Translator.get("test_case_create_module_fail");
                        } else {
                            String nodePath = item.getTitle();
                            item.setPath(nodePath);
                            if (item.getChildren() != null && !item.getChildren().getAttached().isEmpty()) {
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
            return ex.getMessage();
        }
        return process.toString();
    }
}
