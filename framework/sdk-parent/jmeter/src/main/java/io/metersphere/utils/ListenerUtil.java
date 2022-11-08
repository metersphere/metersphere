package io.metersphere.utils;

import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterVariables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListenerUtil {
    public static final String ERROR_LOGGING = "MsResultCollector.error_logging";

    public static final String TEST_IS_LOCAL = "*local*";

    public static final String SUCCESS_ONLY_LOGGING = "MsResultCollector.success_only_logging";

    public static final String RUNNING_DEBUG_SAMPLER_NAME = "RunningDebugSampler";

    private static final String PRE_PROCESS_SCRIPT = "PRE_PROCESSOR_ENV_";
    private static final String POST_PROCESS_SCRIPT = "POST_PROCESSOR_ENV_";

    /**
     * 判断结果是否需要被过滤
     *
     * @param result
     * @return
     */
    public static boolean checkResultIsNotFilterOut(RequestResult result) {
        boolean resultNotFilterOut = true;
        if (StringUtils.startsWithAny(result.getName(), PRE_PROCESS_SCRIPT)) {
            resultNotFilterOut = Boolean.parseBoolean(StringUtils.substring(result.getName(), PRE_PROCESS_SCRIPT.length()));
        } else if (StringUtils.startsWithAny(result.getName(), POST_PROCESS_SCRIPT)) {
            resultNotFilterOut = Boolean.parseBoolean(StringUtils.substring(result.getName(), POST_PROCESS_SCRIPT.length()));
        }
        return resultNotFilterOut;
    }

    public static void setVars(SampleResult result) {
        if (StringUtils.isNotEmpty(result.getSampleLabel()) && result.getSampleLabel().startsWith("Transaction=")) {
            for (int i = 0; i < result.getSubResults().length; i++) {
                SampleResult subResult = result.getSubResults()[i];
                setVars(subResult);
            }
        }
        JMeterVariables variables = JMeterVars.get(result.getResourceId());
        if (variables != null && CollectionUtils.isNotEmpty(variables.entrySet())) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                builder.append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
            }
            if (StringUtils.isNotEmpty(builder)) {
                result.setExtVars(builder.toString());
            }
        }
    }

    public static void setEev(ResultDTO dto, List<String> environmentList) {
        dto.setArbitraryData(new HashMap<String, Object>() {{
            this.put("ENV", environmentList);
        }});
    }
}
