package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class Notification implements Serializable {
    private Long id;

    private String type;

    private String receiver;

    private String title;

    private String status;

    private Long createTime;

    private String content;

    private static final long serialVersionUID = 1L;
}