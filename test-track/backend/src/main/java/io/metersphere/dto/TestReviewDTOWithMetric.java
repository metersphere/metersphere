package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestReviewDTOWithMetric extends TestCaseReviewDTO {
    private Double testRate;
    private Integer reviewed;
    private Integer total;
    private Integer pass;
}
