package io.metersphere.api.jmeter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.metersphere.commons.constants.DelimiterConstants;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Data
public class TestResult {

    private String testId;

    private int scenarioTotal;

    private int scenarioSuccess;

    private int scenarioError;

    private String userId;

    private boolean isDebug;

    private boolean end;

    private String runMode;

    private int success = 0;

    private int error = 0;

    private int total = 0;

    private int totalAssertions = 0;

    private int passAssertions = 0;

    private String console;

    private String runningDebugSampler;

    private List<ScenarioResult> scenarios = new ArrayList<>();

    private Map<String, Boolean> margeScenariMap = new HashMap<>();

    private Map<String, Boolean> scenarioStepMap = new HashMap<>();

    private int scenarioStepSuccess = 0;
    private int scenarioStepError = 0;
    private int scenarioStepTotal = 0;
    public void addError(int count) {
        this.error += count;
    }

    public void addSuccess() {
        this.success++;
    }

    public void addTotalAssertions(int count) {
        this.totalAssertions += count;
    }

    public void addPassAssertions(int count) {
        this.passAssertions += count;
    }

    private static final String SEPARATOR = DelimiterConstants.SEPARATOR.toString();

    private void setStatus(List<String> id_names, boolean status) {
        if (CollectionUtils.isNotEmpty(id_names)) {
            id_names.forEach(item -> {
                if (!margeScenariMap.containsKey(item) || status) {
                    margeScenariMap.put(item, status);
                }
            });
        }
    }

    private void setStatus(String id_names, boolean status) {
        if (!margeScenariMap.containsKey(id_names) || status) {
            margeScenariMap.put(id_names, status);
        }
    }

    private void setStepStatus(String step_names, boolean status) {
        if (!scenarioStepMap.containsKey(step_names) || status) {
            scenarioStepMap.put(step_names, status);
        }
    }


    public void addScenario(ScenarioResult result) {
        /**
         * 1.10.2统计逻辑修改：
         * 不统计所有的请求，改为统计场景和场景步骤
         * 场景里的第一层视为步骤，不考虑深层情况
         */
        if (result != null && CollectionUtils.isNotEmpty(result.getRequestResults())) {
            result.getRequestResults().forEach(item -> {
                String itemAndScenarioName = "";
                if (StringUtils.isNotEmpty(item.getScenario())) {
                    //第1个：当前场景， 第all_id_names个：最后一层场景
                    List<String> all_id_names = JSON.parseObject(item.getScenario(), List.class);
                    if(all_id_names.size()>1){
                        StringBuffer scenarioNames = new StringBuffer();
                        //因为要进行步骤统计，第一层级下的场景算作步骤，所以统计视角只按照第一级别场景来计算
                        scenarioNames.append(all_id_names.get(all_id_names.size()-1)+all_id_names.get(all_id_names.size()-2));
                        this.setStatus(scenarioNames.toString(), item.getError() > 0);
                        itemAndScenarioName = scenarioNames.toString();
                    }else{
                        //不存在多场景时需要补上步骤名字做唯一判断  添加UUID进行处理
                        itemAndScenarioName = item.getName()+":"+JSONArray.toJSONString(all_id_names.get(0))+ UUID.randomUUID().toString();
                        this.setStatus(all_id_names, item.getError() > 0);
                    }

                }
                if (StringUtils.isNotEmpty(item.getName()) && item.getName().indexOf(SEPARATOR) != -1) {
                    String array[] = item.getName().split(SEPARATOR);
                    item.setName(array[1] + array[0]);
                    item.getSubRequestResults().forEach(subItem -> {
                        subItem.setName(array[0]);
                    });
                } else {
                    item.getSubRequestResults().forEach(subItem -> {
                        if (StringUtils.isNotEmpty(subItem.getName()) && subItem.getName().indexOf(SEPARATOR) != -1) {
                            String array[] = subItem.getName().split(SEPARATOR);
                            subItem.setName(array[0]);
                            try {
                                if (StringUtils.isNotEmpty(subItem.getScenario())) {
                                    List<String> id_names = JSON.parseObject(subItem.getScenario(), List.class);
                                    this.setStatus(id_names, subItem.getError() > 0);
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
                }
                this.setStepStatus(itemAndScenarioName,item.getError()>0);
            });
            scenarios.add(result);
        }
        /**
         * 1.10.2 场景成功/失败统计，不再按照请求为纬度，按照场景为纬度，
         */
        for (String key : scenarioStepMap.keySet()) {
            if (scenarioStepMap .get(key)) {
                this.scenarioStepError++;
            } else {
                this.scenarioStepSuccess++;
            }
        }
        boolean hasError = false;
        for (String key : margeScenariMap.keySet()) {
            if (margeScenariMap.get(key)) {
                hasError = true;
                break;
            }
        }
        if(!margeScenariMap.isEmpty()){
            if(hasError){
                this.scenarioError ++;
            }else {
                this.scenarioSuccess++;
            }
            this.scenarioTotal++;
        }


        this.setScenarioStepTotal(this.scenarioStepMap.size());

    }
}