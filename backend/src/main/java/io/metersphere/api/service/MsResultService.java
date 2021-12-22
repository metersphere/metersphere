package io.metersphere.api.service;

import io.metersphere.api.jmeter.FixedCapacityUtils;
import io.metersphere.api.jmeter.MessageCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class MsResultService {
    public String getJmeterLogger(String testId) {
        try {
            Long startTime = MessageCache.jmeterLogTask.get(testId);
            if (startTime == null) {
                startTime = MessageCache.jmeterLogTask.get("[" + testId + "]");
            }
            if (startTime == null) {
                startTime = System.currentTimeMillis();
            }
            Long endTime = System.currentTimeMillis();
            Long finalStartTime = startTime;
            String logMessage = FixedCapacityUtils.fixedCapacityCache.entrySet().stream()
                    .filter(map -> map.getKey() > finalStartTime && map.getKey() < endTime)
                    .map(map -> map.getValue()).collect(Collectors.joining());
            return logMessage;
        } catch (Exception e) {
            return "";
        }
    }
}
