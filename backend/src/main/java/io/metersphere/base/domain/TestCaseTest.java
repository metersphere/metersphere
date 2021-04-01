package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseTest implements Serializable {
    private String testCaseId;

    private String testId;

    private String testType;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}