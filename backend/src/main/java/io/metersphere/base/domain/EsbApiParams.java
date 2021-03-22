package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class EsbApiParams implements Serializable {
    private String id;

    private String resourceId;

    private static final long serialVersionUID = 1L;
}