package io.metersphere.base.domain;

import lombok.Data;

@Data
public class NoticeDetail extends Notice {
    private String[] names;

    private String[] emails;

    private String event;

    private String testId;

    private String name;

    private String email;

    private String enable;
}
