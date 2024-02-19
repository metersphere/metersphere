package io.metersphere.api.dto.report;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ReportDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String poolId;

}
