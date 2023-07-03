package io.metersphere.system.dto.request;

import lombok.Data;

@Data
public class UserChangeEnableRequest extends UserBatchProcessRequest {
    boolean enable;
}
