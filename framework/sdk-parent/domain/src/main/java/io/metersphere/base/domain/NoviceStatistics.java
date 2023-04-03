package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class NoviceStatistics implements Serializable {
    private String id;

    private String userId;

    private Integer guideStep;

    private Integer guideNum;

    private Long createTime;

    private Long updateTime;

    private String dataOption;

    private static final long serialVersionUID = 1L;
}