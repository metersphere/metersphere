package io.metersphere.xpack.quota.dto;

import io.metersphere.base.domain.Quota;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QuotaResult extends Quota {

    private String workspaceName;
    private String projectName;
    private Integer projectUsed;
}
