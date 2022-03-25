package io.metersphere.api.exec.utils;

import io.metersphere.dto.RequestResult;
import org.apache.commons.lang3.StringUtils;

public class ResultParseUtil {

    private static final String PRE_PROCESS_SCRIPT = "PRE_PROCESSOR_ENV_";
    private static final String POST_PROCESS_SCRIPT = "POST_PROCESSOR_ENV_";

    public static boolean isNotAutoGenerateSampler(RequestResult result) {
        if (StringUtils.equals(result.getMethod(), "Request") && StringUtils.startsWithAny(result.getName(), PRE_PROCESS_SCRIPT, POST_PROCESS_SCRIPT)) {
            return false;
        }
        return true;
    }
}
