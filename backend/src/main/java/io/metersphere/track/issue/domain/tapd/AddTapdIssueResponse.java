package io.metersphere.track.issue.domain.tapd;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddTapdIssueResponse {

    private int status;
    private Data data;
    private String info;

    @Getter
    @Setter
    public static class Data {
        private TapdBug bug;
    }

}
