package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestReviewDTOWithMetric extends TestCaseReviewDTO {
    private static final Integer ZERO = 0;
    private Double testRate;
    private Integer reviewed = ZERO;
    private Integer total = ZERO;
    private Integer pass = ZERO;
    private Integer prepare = ZERO;
    private Integer again = ZERO;
}
