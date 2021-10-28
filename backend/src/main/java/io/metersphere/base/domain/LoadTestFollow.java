package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestFollow implements Serializable {
    private String testId;

    private String followId;

    private static final long serialVersionUID = 1L;
}