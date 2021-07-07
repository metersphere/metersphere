package io.metersphere.track.issue.domain.tapd;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TapdBug {
    private String id;
    private String title;
    private String description;
//        private String priority;
//        private String severity;
//        private String reporter;
    private String status;
}
