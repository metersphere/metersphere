package io.metersphere.track.issue.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TapdStatusMapResponse {

    private int status;
    private Data data;
    private String info;

    @Getter
    @Setter
    public class Data {
        @JSONField(name = "new")
        private String create;
        @JSONField(name = "in_progress")
        private String inProgress;
        private String resolved;
        private String verified;
        private String reopened;
        private String rejected;
        private String closed;
    }

}
