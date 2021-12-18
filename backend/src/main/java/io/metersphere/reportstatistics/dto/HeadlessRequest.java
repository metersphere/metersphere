package io.metersphere.reportstatistics.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class HeadlessRequest {
    private String url;
    private String remoteDriverUrl;

    public boolean isEmpty() {
        return StringUtils.isEmpty(this.url) || StringUtils.isEmpty(this.remoteDriverUrl);
    }
}
