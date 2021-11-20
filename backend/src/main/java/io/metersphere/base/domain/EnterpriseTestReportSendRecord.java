package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class EnterpriseTestReportSendRecord implements Serializable {
    private String id;

    private String enterpriseTestReportId;

    private Long createTime;

    private String createUser;

    private String status;

    private static final long serialVersionUID = 1L;
}