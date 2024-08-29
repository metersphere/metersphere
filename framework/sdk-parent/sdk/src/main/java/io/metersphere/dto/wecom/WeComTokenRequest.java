package io.metersphere.dto.wecom;

import lombok.Data;

import java.io.Serializable;

@Data
public class WeComTokenRequest implements Serializable {

    private String code;

    private String state;
}
