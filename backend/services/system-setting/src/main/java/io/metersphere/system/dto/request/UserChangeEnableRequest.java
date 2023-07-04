package io.metersphere.system.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserChangeEnableRequest extends UserBatchProcessRequest {
    boolean enable;
}
