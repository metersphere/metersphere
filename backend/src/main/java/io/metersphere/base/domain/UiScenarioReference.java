package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class UiScenarioReference implements Serializable {
    private String id;

    private String uiScenarioId;

    private Long createTime;

    private String createUserId;

    private String referenceId;

    private String referenceType;

    private String dataType;

    private Integer customId;

    private static final long serialVersionUID = 1L;
}