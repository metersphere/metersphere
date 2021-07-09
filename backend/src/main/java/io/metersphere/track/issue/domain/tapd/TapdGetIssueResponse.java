package io.metersphere.track.issue.domain.tapd;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TapdGetIssueResponse {

    private int status;
    private List<Data> data;
    private String info;

    @Getter
    @Setter
    public static class Data {
        private TapdBug bug;
    }
}
