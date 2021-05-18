package io.metersphere.api.jmeter;

import com.alibaba.fastjson.JSON;
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

    private String setReportId;

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

    private List<ScenarioResult> scenarios = new ArrayList<>();

    private Map<String, Boolean> margeScenariMap = new HashMap<>();

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

    public void addScenario(ScenarioResult result) {
        if (result != null && CollectionUtils.isNotEmpty(result.getRequestResults())) {
            result.getRequestResults().forEach(item -> {
                if (StringUtils.isNotEmpty(item.getScenario())) {
                    List<String> id_names = JSON.parseObject(item.getScenario(), List.class);
                    this.setStatus(id_names, item.getError() > 0);
                }
                if (StringUtils.isNotEmpty(item.getName()) && item.getName().indexOf(SEPARATOR) != -1) {
                    String array[] = item.getName().split(SEPARATOR);
                    item.setName(array[1] + array[0]);
                    item.getSubRequestResults().forEach(subItem -> {
                        subItem.setName(array[0]);
                    });
                }else {
                    item.getSubRequestResults().forEach(subItem -> {
                        if (StringUtils.isNotEmpty(subItem.getName()) && subItem.getName().indexOf(SEPARATOR) != -1) {
                            String array[] = subItem.getName().split(SEPARATOR);
                            subItem.setName(array[0]);
                        }
                    });
                }
            });
            scenarios.add(result);
        }
        for (String key : margeScenariMap.keySet()) {
            if (margeScenariMap.get(key)) {
                this.scenarioError++;
            } else {
                this.scenarioSuccess++;
            }
        }
        this.setScenarioTotal(this.margeScenariMap.size());
    }
}
