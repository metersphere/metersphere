package io.metersphere.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.excel.utils.ExcelValidateHelper;
import io.metersphere.i18n.Translator;
import io.metersphere.track.service.TestCaseService;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestCaseDataIgnoreErrorListener extends EasyExcelListener<TestCaseExcelData> {

    private TestCaseService testCaseService;

    private String projectId;

    protected List<TestCaseExcelData> updateList = new ArrayList<>();  //存储待更新用例的集合

    protected boolean isUpdated = false;  //判断是否更新过用例，将会传给前端

    Set<String> testCaseNames;

    Set<String> userIds;

    public boolean isUpdated() {
        return isUpdated;
    }

    public TestCaseDataIgnoreErrorListener(Class clazz, String projectId, Set<String> testCaseNames, Set<String> userIds) {
        this.clazz = clazz;
        this.testCaseService = (TestCaseService) CommonBeanFactory.getBean("testCaseService");
        this.projectId = projectId;
        this.testCaseNames = testCaseNames;
        this.userIds = userIds;
    }

    @Override
    public String validate(TestCaseExcelData data, String errMsg) {
        String nodePath = data.getNodePath();
        StringBuilder stringBuilder = new StringBuilder(errMsg);
        //校验”所属模块"
        if (nodePath != null) {
            String[] nodes = nodePath.split("/");
            //校验模块深度
            if (nodes.length > TestCaseConstants.MAX_NODE_DEPTH + 1) {
                stringBuilder.append(Translator.get("test_case_node_level_tip") +
                        TestCaseConstants.MAX_NODE_DEPTH + Translator.get("test_case_node_level") + "; ");
            }
            //模块名不能为空
            for (int i = 0; i < nodes.length; i++) {
                if (i != 0 && StringUtils.equals(nodes[i].trim(), "")) {
                    stringBuilder.append(Translator.get("module_not_null") + "; ");
                    break;
                }
            }
            //增加字数校验，每一层不能超过100字
            for (int i = 0; i < nodes.length; i++) {
                String nodeStr = nodes[i];
                if (StringUtils.isNotEmpty(nodeStr)) {
                    if (nodeStr.trim().length() > 100) {
                        stringBuilder.append(Translator.get("module") + Translator.get("test_track.length_less_than") + "100:" + nodeStr);
                        break;
                    }
                }
            }
        }
        //校验维护人
        if (!userIds.contains(data.getMaintainer())) {
            stringBuilder.append(Translator.get("user_not_exists") + "：" + data.getMaintainer() + "; ");
        }

        /*
        校验Excel中是否有ID
            有的话校验ID是否已在当前项目中存在，存在则更新用例，
            不存在则继续校验看是否重复，不重复则新建用例。
         */
        if (null != data.getNum()) {  //当前读取的数据有ID
            if (null != testCaseService.checkIdExist(data.getNum(), projectId)) {  //该ID在当前项目中存在
                //如果前面所经过的校验都没报错
                if (StringUtils.isEmpty(stringBuilder)) {
                    updateList.add(data);   //将当前数据存入更新列表
                    stringBuilder.append("update_testcase");   //该信息用于在invoke方法中判断是否该更新用例
                }
                return stringBuilder.toString();
            } else {
                /*
                该ID在当前数据库中不存在，应当继续校验用例是否重复,
                在下面的校验过程中，num的值会被用于判断是否重复，所以应当先设置为null
                 */
                data.setNum(null);
            }
        }
        /*
        校验用例
         */
        if (testCaseNames.contains(data.getName())) {
            TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
            BeanUtils.copyBean(testCase, data);
            testCase.setProjectId(projectId);
            String steps = getSteps(data);
            testCase.setSteps(steps);
            boolean dbExist = testCaseService.exist(testCase);
            boolean excelExist = false;
            if (dbExist) {
                // db exist
                stringBuilder.append(Translator.get("test_case_already_exists") + "：" + data.getName() + "; ");
            } else {
                // @Data 重写了 equals 和 hashCode 方法
                excelExist = excelDataList.contains(data);
            }
            if (excelExist) {
                // excel exist
                stringBuilder.append(Translator.get("test_case_already_exists_excel") + "：" + data.getName() + "; ");
            } else {
                excelDataList.add(data);
            }
        } else {
            testCaseNames.add(data.getName());
            excelDataList.add(data);
        }
        return stringBuilder.toString();
    }

    @Override
    public void saveData() {
        if (!(list.size() == 0)) {
            Collections.reverse(list);  //因为saveImportData里面是先分配最大的ID，这个ID应该先发给list中最后的数据，所以要reverse
            List<TestCaseWithBLOBs> result = list.stream()
                    .map(item -> this.convert2TestCase(item))
                    .collect(Collectors.toList());
            testCaseService.saveImportData(result, projectId);
            this.isUpdated = true;
        }

        if (!(updateList.size() == 0)) {
            List<TestCaseWithBLOBs> result2 = updateList.stream()
                    .map(item -> this.convert2TestCaseForUpdate(item))
                    .collect(Collectors.toList());
            testCaseService.updateImportDataCarryId(result2, projectId);
            this.isUpdated = true;
            updateList.clear();
        }

    }


    private TestCaseWithBLOBs convert2TestCase(TestCaseExcelData data) {
        TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
        BeanUtils.copyBean(testCase, data);
        testCase.setId(UUID.randomUUID().toString());
        testCase.setProjectId(this.projectId);
        testCase.setCreateTime(System.currentTimeMillis());
        testCase.setUpdateTime(System.currentTimeMillis());
        testCase.setCustomNum(data.getCustomNum());
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
        TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
        BeanUtils.copyBean(testCase, data);
        testCase.setProjectId(this.projectId);
        testCase.setUpdateTime(System.currentTimeMillis());

        //调整nodePath格式
        String nodePath = data.getNodePath();
        if (!nodePath.startsWith("/")) {
            nodePath = "/" + nodePath;
        }
        if (nodePath.endsWith("/")) {
            nodePath = nodePath.substring(0, nodePath.length() - 1);
        }
        testCase.setNodePath(nodePath);

        String steps = getSteps(data);
        testCase.setSteps(steps);

        //将标签设置为前端可解析的格式
        String modifiedTags = modifyTagPattern(data);
        testCase.setTags(modifiedTags);

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
                JSONArray.parse(tags);
                return tags;
            }
            return "[]";
        } catch (Exception e) {
            if (tags != null) {
                Stream<String> stringStream = Arrays.stream(tags.split("[,;，；]"));  //当标签值以中英文的逗号和分号分隔时才能正确解析
                List<String> tagList = stringStream.map(tag -> tag = "\"" + tag + "\"")
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
        JSONArray jsonArray = new JSONArray();

        String[] stepDesc = new String[1];
        String[] stepRes = new String[1];

        if (data.getStepDesc() != null) {
            stepDesc = data.getStepDesc().split("\r\n|\n");
        } else {
            stepDesc[0] = "";
        }
        if (data.getStepResult() != null) {
            stepRes = data.getStepResult().split("\r\n|\n");
        } else {
            stepRes[0] = "";
        }

        String pattern = "(^\\d+)(\\.)?";
        int index = stepDesc.length > stepRes.length ? stepDesc.length : stepRes.length;

        for (int i = 0; i < index; i++) {

            // 保持插入顺序，判断用例是否有相同的steps
            JSONObject step = new JSONObject(true);
            step.put("num", i + 1);

            Pattern descPattern = Pattern.compile(pattern);
            Pattern resPattern = Pattern.compile(pattern);

            if (i < stepDesc.length) {
                Matcher descMatcher = descPattern.matcher(stepDesc[i]);
                if (descMatcher.find()) {
                    step.put("desc", descMatcher.replaceAll(""));
                } else {
                    step.put("desc", stepDesc[i]);
                }
            }

            if (i < stepRes.length) {
                Matcher resMatcher = resPattern.matcher(stepRes[i]);
                if (resMatcher.find()) {
                    step.put("result", resMatcher.replaceAll(""));
                } else {
                    step.put("result", stepRes[i]);
                }
            }

            jsonArray.add(step);
        }
        return jsonArray.toJSONString();
    }

    @Override
    public void invoke(TestCaseExcelData testCaseExcelData, AnalysisContext analysisContext) {
        String errMsg;
        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();
        String updateMsg = "update_testcase";
        try {
            //根据excel数据实体中的javax.validation + 正则表达式来校验excel数据
            errMsg = ExcelValidateHelper.validateEntity(testCaseExcelData);
            //自定义校验规则
            errMsg = validate(testCaseExcelData, errMsg);
        } catch (NoSuchFieldException e) {
            errMsg = Translator.get("parse_data_error");
            LogUtil.error(e.getMessage(), e);
        }

        if (!StringUtils.isEmpty(errMsg)) {

            //如果errMsg只有"update testcase"，说明用例待更新
            if (!errMsg.equals(updateMsg)) {
                ExcelErrData excelErrData = new ExcelErrData(testCaseExcelData, rowIndex,
                        Translator.get("number") + " " + rowIndex + " " + Translator.get("row") + Translator.get("error")
                                + "：" + errMsg);

                errList.add(excelErrData);
            }
        } else {
            list.add(testCaseExcelData);
        }

        if (list.size() > BATCH_COUNT) {
            saveData();
            list.clear();
        }
    }
}
