package io.metersphere.track.issue.domain.zentao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddIssueResponse extends ZentaoResponse {
    @Getter
    @Setter
    public static class Issue {
        private String status;
        private String id;
    }
}
