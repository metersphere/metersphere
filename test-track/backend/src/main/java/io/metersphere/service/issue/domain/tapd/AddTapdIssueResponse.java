package io.metersphere.service.issue.domain.tapd;

import com.fasterxml.jackson.annotation.JsonAlias;
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
        @JsonAlias("Bug")
        private TapdBug bug;
    }

}
