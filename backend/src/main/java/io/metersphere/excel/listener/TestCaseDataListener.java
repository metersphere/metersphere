package io.metersphere.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.commons.constants.TestCaseConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.ListUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.TestCaseExcelData;
import io.metersphere.excel.utils.ExcelValidateHelper;
import io.metersphere.excel.utils.FunctionCaseImportEnum;
import io.metersphere.i18n.Translator;
import io.metersphere.track.service.TestCaseService;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestCaseDataListener extends EasyExcelListener<TestCaseExcelData> {

    private TestCaseService testCaseService;

    private String projectId;

    protected List<TestCaseExcelData> updateList = new ArrayList<>();  //存储待更新用例的集合

    protected boolean isUpdated = false;  //判断是否更新过用例，将会传给前端

    public boolean isUseCustomId;

    public String importType;

    Set<String> testCaseNames;

    Set<String> customIds;

    Set<String> savedCustomIds;

    Set<String> userIds;

    private List<String> names = new LinkedList<>();
    private List<String> ids = new LinkedList<>();

    public boolean isUpdated() {
        return isUpdated;
    }

    public TestCaseDataListener(Class clazz, String projectId, Set<String> testCaseNames,Set<String> savedCustomIds, Set<String> userIds,boolean isUseCustomId,String importType) {
        this.clazz = clazz;
        this.testCaseService = (TestCaseService) CommonBeanFactory.getBean("testCaseService");
        this.projectId = projectId;
        this.testCaseNames = testCaseNames;
        this.userIds = userIds;
        this.isUseCustomId = isUseCustomId;
        this.importType = importType;
        customIds = new HashSet<>();
        this.savedCustomIds = savedCustomIds;
    }

    @Override
    public String validate(TestCaseExcelData data, String errMsg) {
        StringBuilder stringBuilder = new StringBuilder(errMsg);
        if(isUseCustomId || StringUtils.equals(this.importType,FunctionCaseImportEnum.Update.name())){
            if(data.getCustomNum() == null){
                stringBuilder.append(Translator.get("id_required")+";");
            }else {
                String customId = data.getCustomNum().toString();
                if(StringUtils.isEmpty(customId)){
                    stringBuilder.append(Translator.get("id_required")+";");
                }else if(customIds.contains(customId)) {
                    stringBuilder.append(Translator.get("id_repeat_in_table") + ";");
                }else if(StringUtils.equals(FunctionCaseImportEnum.Create.name(),importType) && savedCustomIds.contains(customId)){
                    stringBuilder.append(Translator.get("custom_num_is_exist") + ";");
                }else if(StringUtils.equals(FunctionCaseImportEnum.Update.name(),importType) && !savedCustomIds.contains(customId)){
                    stringBuilder.append(Translator.get("custom_num_is_not_exist") + ";");
                }else {
                    customIds.add(customId);
                }
            }
        }

        String nodePath = data.getNodePath();
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
        if (null != data.getCustomNum()) {  //当前读取的数据有ID

            if(StringUtils.equals(this.importType,FunctionCaseImportEnum.Update.name())){
                String checkResult = null;
                if(isUseCustomId){
                    checkResult = testCaseService.checkCustomIdExist(data.getCustomNum().toString(), projectId);
                }else {
                    checkResult = testCaseService.checkIdExist(Integer.parseInt(data.getCustomNum()), projectId);
                }
                if (null != checkResult) {  //该ID在当前项目中存在
                    //如果前面所经过的校验都没报错
                    if (StringUtils.isEmpty(stringBuilder)) {
                        data.setId(checkResult);
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

        }


        /*
        校验用例
         */
//        if (testCaseNames.contains(data.getName())) {
//            TestCaseWithBLOBs testCase = new TestCaseWithBLOBs();
//            BeanUtils.copyBean(testCase, data);
//            testCase.setProjectId(projectId);
//            String steps = getSteps(data);
//            testCase.setSteps(steps);
//            testCase.setType("functional");
//
//            boolean dbExist = testCaseService.exist(testCase);
//            boolean excelExist = false;
//
//            if (dbExist) {
//                // db exist
//                stringBuilder.append(Translator.get("test_case_already_exists") + "：" + data.getName() + "; ");
//            } else {
//                // @Data 重写了 equals 和 hashCode 方法
//                excelExist = excelDataList.contains(data);
//            }
//
//            if (excelExist) {
//                // excel exist
//                stringBuilder.append(Translator.get("test_case_already_exists_excel") + "：" + data.getName() + "; ");
//            } else {
//                excelDataList.add(data);
//            }
//
//        } else {
            testCaseNames.add(data.getName());
            excelDataList.add(data);
//        }
        return stringBuilder.toString();
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

    @Override
    public void saveData() {

        //excel中用例都有错误时就返回，只要有用例可用于更新或者插入就不返回
        if (!errList.isEmpty()) {
            return;
        }

        if (!(list.size() == 0)) {
            Collections.reverse(list);  //因为saveImportData里面是先分配最大的ID，这个ID应该先发给list中最后的数据，所以要reverse
            List<TestCaseWithBLOBs> result = list.stream()
                    .map(item -> this.convert2TestCase(item))
                    .collect(Collectors.toList());
            testCaseService.saveImportData(result, projectId);
            this.setNames(result.stream().map(TestCase::getName).collect(Collectors.toList()));
            this.setIds(result.stream().map(TestCase::getId).collect(Collectors.toList()));
            this.isUpdated = true;
        }

        if (!(updateList.size() == 0)) {
            List<TestCaseWithBLOBs> result2 = updateList.stream()
                    .map(item -> this.convert2TestCaseForUpdate(item))
                    .collect(Collectors.toList());
            if(this.isUseCustomId){
                testCaseService.updateImportDataCustomId(result2, projectId);
            }else {
                testCaseService.updateImportDataCarryId(result2, projectId);
            }
            this.isUpdated = true;
            this.setNames(result2.stream().map(TestCase::getName).collect(Collectors.toList()));
            this.setIds(result2.stream().map(TestCase::getId).collect(Collectors.toList()));
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
        if(this.isUseCustomId){
            testCase.setCustomNum(data.getCustomNum().toString());
        }

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
        testCase.setType("functional");

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

        if(!isUseCustomId){
            testCase.setNum(Integer.parseInt(data.getCustomNum()));
            testCase.setCustomNum(null);
        }

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

        List<String> stepDescList = new ArrayList<>();
        List<String> stepResList = new ArrayList<>();
//        String[] stepDesc = new String[1];
//        String[] stepRes = new String[1];
        ListUtils<String> listUtils = new ListUtils<String>();
        if (data.getStepDesc() != null) {
            String[] stepDesc = data.getStepDesc().split("\r\n|\n");
            StringBuffer stepBuffer = new StringBuffer();
            int lastStepIndex = 1;
            for (String row : stepDesc) {
                int rowIndex = this.parseIndexInRow(row);
                if(rowIndex > -1){

                    listUtils.set(stepDescList,lastStepIndex-1,stepBuffer.toString(),"");
//                    stepResList.set(stepIndex-1,stepBuffer.toString());
                    stepBuffer = new StringBuffer();
                    lastStepIndex = rowIndex;
                    stepBuffer.append(row);
                }else {
                    stepBuffer.append(row);
                }
//                if(StringUtils.startsWithAny(row,
//                        stepIndex+")","("+stepIndex+")","（\"+stepIndex+\"）",
//                        stepIndex+".",stepIndex+",",stepIndex+"，")){
//                    if(StringUtils.isNotEmpty(stepBuffer.toString())){
//                        stepDescList.add(stepBuffer.toString());
//                    }
//                    stepBuffer = new StringBuffer();
//                    stepIndex++;
//                    stepBuffer.append(row);
//                }else {
//                    if(StringUtils.isNotEmpty(stepBuffer.toString())){
//                        stepBuffer.append("\r\n");
//                    }
//                    stepBuffer.append(row);
//                }
            }
            if(StringUtils.isNotEmpty(stepBuffer.toString())){
                listUtils.set(stepDescList,lastStepIndex-1,stepBuffer.toString(),"");
            }
        } else {
            stepDescList.add("");
        }

        if (data.getStepResult() != null) {
            String [] stepRes = data.getStepResult().split("\r\n|\n");
            StringBuffer stepBuffer = new StringBuffer();
            int lastStepIndex = 1;
            for (String row : stepRes) {
                int rowIndex = this.parseIndexInRow(row);
                if(rowIndex > -1){

                    listUtils.set(stepResList,lastStepIndex-1,stepBuffer.toString(),"");
//                    stepResList.set(stepIndex-1,stepBuffer.toString());
                    stepBuffer = new StringBuffer();
                    lastStepIndex = rowIndex;
                    stepBuffer.append(row);
                }else {
                    stepBuffer.append(row);
                }
//                if(StringUtils.startsWithAny(row,
//                        stepIndex+")","("+stepIndex+")","（\"+stepIndex+\"）",
//                        stepIndex+".",stepIndex+",",stepIndex+"，")){
//                    if(StringUtils.isNotEmpty(stepBuffer.toString())){
//                        stepResList.add(stepBuffer.toString());
//                    }
//                    stepBuffer = new StringBuffer();
//                    stepIndex++;
//                    stepBuffer.append(row);
//                }else {
//                    if(StringUtils.isNotEmpty(stepBuffer.toString())){
//                        stepBuffer.append("\r\n");
//                    }
//                    stepBuffer.append(row);
//                }
            }
            if(StringUtils.isNotEmpty(stepBuffer.toString())){
                listUtils.set(stepResList,lastStepIndex-1,stepBuffer.toString(),"");
            }
        } else {
            stepResList.add("");
        }

//        String pattern = "(^\\d+)(\\.)?";
        int index = stepDescList.size() > stepResList.size() ? stepDescList.size() : stepResList.size();

        for (int i = 0; i < index; i++) {

            // 保持插入顺序，判断用例是否有相同的steps
            JSONObject step = new JSONObject(true);
            step.put("num", i + 1);

//            Pattern descPattern = Pattern.compile(pattern);
//            Pattern resPattern = Pattern.compile(pattern);

            if (i < stepDescList.size()) {
//                Matcher descMatcher = descPattern.matcher(stepDescList.get(i));
//                if (descMatcher.find()) {
//                    step.put("desc", descMatcher.replaceAll(""));
//                } else {
//                    step.put("desc", stepDescList.get(i));
//                }
                step.put("desc", stepDescList.get(i));
            }

            if (i < stepResList.size()) {
//                Matcher resMatcher = resPattern.matcher(stepResList.get(i));
//                if (resMatcher.find()) {
//                    step.put("result", resMatcher.replaceAll(""));
//                } else {
//                    step.put("result", stepResList.get(i));
//                }
                step.put("result", stepResList.get(i));
            }

            jsonArray.add(step);
        }
        return jsonArray.toJSONString();
    }

    private int parseIndexInRow(String row) {
        String parseString = row;
        int index = -1;
        String [] indexSplitCharArr = new String[]{")","）","]","】",".",",","，","。"};
        if(StringUtils.startsWithAny(row,"(","（","[","【")){
            parseString = parseString.substring(1);
        }
        for (String splitChar : indexSplitCharArr) {
            if(StringUtils.contains(parseString,splitChar)){
                String[] rowSplit = StringUtils.split(parseString,splitChar);
                if(rowSplit.length > 0){
                    String indexString = rowSplit[0];
                    if(StringUtils.isNumeric(indexString)){
                        try {
                            index = Integer.parseInt(indexString);
                        }catch (Exception e){}
                        if(index > -1){
                            break;
                        }
                    }
                }
            }
        }
        return index;
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
