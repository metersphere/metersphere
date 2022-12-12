package io.metersphere.dto;

import io.metersphere.base.domain.TestCaseReview;
import io.metersphere.base.domain.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseReviewDTO extends TestCaseReview {

    private String projectName;
    private String reviewerName;
    private String creatorName;
    private Double passRate;
    private Integer caseCount;
    private List<CountMapDTO> statusCountItems;
    private List<User> reviewers;
}
