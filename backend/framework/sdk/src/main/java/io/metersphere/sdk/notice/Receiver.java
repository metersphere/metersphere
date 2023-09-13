package io.metersphere.sdk.notice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class Receiver {
    private String userId;
    private String type;
}
