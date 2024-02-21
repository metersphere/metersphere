package io.metersphere.api.dto.mockserver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

@Data
public class keyValueMatchRole {
    @Schema(description = "是否是全部匹配 （false为任意匹配）")
    private boolean isMatchAll;
    @Schema(description = "匹配规则")
    private List<KeyValueInfo> matchRules;

    public boolean match(Map<String, String> matchParam) {
        if (MapUtils.isEmpty(matchParam)) {
            return true;
        }
        if (isMatchAll) {
            for (KeyValueInfo matchRule : matchRules) {
                if (!matchRule.matchValue(matchParam.get(matchRule.getKey()))) {
                    return false;
                }
            }
            return true;
        } else {
            for (KeyValueInfo matchRule : matchRules) {
                if (matchRule.matchValue(matchParam.get(matchRule.getKey()))) {
                    return true;
                }
            }
            return false;
        }
    }
}
