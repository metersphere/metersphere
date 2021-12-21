package io.metersphere.reportstatistics.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class HeadlessRequest {
    private Map<String, String> urlMap;
    private String remoteDriverUrl;

    public boolean isEmpty() {
        return MapUtils.isEmpty(this.urlMap) || StringUtils.isEmpty(this.remoteDriverUrl);
    }
}
