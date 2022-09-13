package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TestCaseReportStatusResultDTO implements Serializable {
    private String status;
    private Integer count;
}

