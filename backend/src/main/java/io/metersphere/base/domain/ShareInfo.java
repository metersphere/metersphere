package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ShareInfo implements Serializable {
    private String id;

    private Long createTime;

    private String createUserId;

    private Long updateTime;

    private String shareType;

    private String customData;

    private static final long serialVersionUID = 1L;
}