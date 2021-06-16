package io.metersphere.api.jmeter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.metersphere.commons.constants.DelimiterConstants;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TestResult {

    private String testId;

    private int scenarioTotal;

    private int scenarioSuccess;

    private int scenarioError;

    private String userId;

    private boolean isDebug;

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

    private void setStepStatus(String step_names, boolean status) {
        if (!scenarioStepMap.containsKey(step_names) || status) {
            scenarioStepMap.put(step_names, status);
        }
    }


    public void addScenario(ScenarioResult result) {
        if (result != null && CollectionUtils.isNotEmpty(result.getRequestResults())) {
            result.getRequestResults().forEach(item -> {
                String itemScenarioName = "";
                if (StringUtils.isNotEmpty(item.getScenario())) {
                    List<String> all_id_names = JSON.parseObject(item.getScenario(), List.class);
                    if(all_id_names.size()>1){
                        List<String> id_names = new ArrayList<>();
                        all_id_names.forEach(name -> {
                            if(!name.endsWith(result.getName())){
                                id_names.add(name);
                            }
                        });
                        this.setStatus(id_names, item.getError() > 0);
                        itemScenarioName = JSONArray.toJSONString(id_names);
                    }else{
                        this.setStatus(all_id_names, item.getError() > 0);
                        itemScenarioName = JSONArray.toJSONString(all_id_names);
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
                this.setStepStatus(item.getName()+itemScenarioName,item.getError()>0);
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