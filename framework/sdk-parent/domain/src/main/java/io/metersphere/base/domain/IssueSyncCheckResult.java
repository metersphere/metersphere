package io.metersphere.base.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class IssueSyncCheckResult implements Serializable {

    private Boolean syncComplete;

    private String syncResult;
}
