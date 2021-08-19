package io.metersphere.notice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Receiver {
    private String userId;
    private String type;
}
