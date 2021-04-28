package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestFile implements Serializable {
    private String testId;

    private String fileId;

    private Integer sort;

    private static final long serialVersionUID = 1L;
}