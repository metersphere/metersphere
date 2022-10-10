package io.metersphere.service.issue.domain.tapd;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TapdGetIssueResponse {

    private int status;
    private List<Map> data;
    private String info;
}
