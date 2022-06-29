package io.metersphere.base.domain;


import lombok.Data;

import java.io.Serializable;

@Data
public class UiElementReference implements Serializable {

  private String id;

  private String elementId;

  private String elementModuleId;

  private String scenarioId;

  private String projectId;

  private Long createTime;

  private static final long serialVersionUID = 1L;

}
