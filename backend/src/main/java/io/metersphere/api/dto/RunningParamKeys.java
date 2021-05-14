package io.metersphere.api.dto;

import org.apache.commons.lang3.StringUtils;


public class RunningParamKeys {
    public static final String API_ENVIRONMENT_ID = "${__metersphere_evn_id}";

    public static String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }
}
