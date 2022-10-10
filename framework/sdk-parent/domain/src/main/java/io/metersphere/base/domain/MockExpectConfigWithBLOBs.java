package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MockExpectConfigWithBLOBs extends MockExpectConfig implements Serializable {
    private String request;

    private String response;

    private static final long serialVersionUID = 1L;
}