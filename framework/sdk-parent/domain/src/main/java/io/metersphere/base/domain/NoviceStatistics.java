package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class NoviceStatistics implements Serializable {
    private String id;

    private String userId;

    private Integer guideStep;

    private Integer guideNum;

    private String dataOption;

    private Integer status;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}