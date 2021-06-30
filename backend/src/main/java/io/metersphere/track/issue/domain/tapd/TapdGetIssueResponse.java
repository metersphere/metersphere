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
        private Bug bug;
    }

    @Getter
    @Setter
    public static class Bug {
        private String id;
        private String title;
        private String description;
//        private String priority;
//        private String severity;
//        private String reporter;
        private String status;
    }
}
