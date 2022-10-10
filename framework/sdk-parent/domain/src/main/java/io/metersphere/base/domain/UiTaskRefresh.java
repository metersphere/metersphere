package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class UiTaskRefresh implements Serializable {

    private String id;

    private String taskKey;

    private Integer taskStatus;

    private long createTime;

    private static final long serialVersionUID = 1L;
}
