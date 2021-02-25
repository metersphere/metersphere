package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiDocumentShare implements Serializable {
    private String id;

    private Long createTime;

    private String createUserId;

    private Long updateTime;

    private String shareType;

    private String shareApiId;

    private static final long serialVersionUID = 1L;
}