package io.metersphere.api.jmeter;

import io.metersphere.commons.utils.BeanUtils;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Data
public class TestResult {

    private String testId;

    private int success = 0;

    private int error = 0;

    private int total = 0;

    private int totalAssertions = 0;

    private int passAssertions = 0;

    private List<ScenarioResult> scenarios = new ArrayList<>();

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

    private static final String SEPARATOR = "<->";

    public void addScenario(ScenarioResult result) {
        Map<String, List<RequestResult>> requestResultMap = new LinkedHashMap<>();
        if (result != null && CollectionUtils.isNotEmpty(result.getRequestResults())) {
            result.getRequestResults().forEach(item -> {
                if (StringUtils.isNotEmpty(item.getName()) && item.getName().indexOf(SEPARATOR) != -1) {
                    String array[] = item.getName().split(SEPARATOR);
                    String scenarioName = item.getName().replace(array[0] + SEPARATOR, "");
                    item.setName(array[0]);
                    if (requestResultMap.containsKey(scenarioName)) {
                        requestResultMap.get(scenarioName).add(item);
                    } else {
                        List<RequestResult> requestResults = new LinkedList<>();
                        requestResults.add(item);
                        requestResultMap.put(scenarioName, requestResults);
                    }
                    item.getSubRequestResults().forEach(subItem -> {
                        subItem.setName(item.getName());
                    });
                } else {
                    if (requestResultMap.containsKey(result.getName())) {
                        requestResultMap.get(result.getName()).add(item);
                    } else {
                        List<RequestResult> requestResults = new LinkedList<>();
                        requestResults.add(item);
                        requestResultMap.put(result.getName(), requestResults);
                    }
                    item.getSubRequestResults().forEach(subItem -> {
                        subItem.setName(item.getName());
                    });

                }
            });
        }
        if (!requestResultMap.isEmpty()) {
            requestResultMap.forEach((k, v) -> {
                ScenarioResult scenarioResult = new ScenarioResult();
                BeanUtils.copyBean(scenarioResult, result);
                scenarioResult.setName(k);
                if (k.indexOf(SEPARATOR) != -1) {
                    scenarioResult.setName(k.split(SEPARATOR)[1]);
                }
                scenarioResult.setRequestResults(v);
                scenarios.add(scenarioResult);
            });
        } else {
            scenarios.add(result);
        }
    }
}
