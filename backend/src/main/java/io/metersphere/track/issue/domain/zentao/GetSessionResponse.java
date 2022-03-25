package io.metersphere.track.issue.domain.zentao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetSessionResponse {
    private String status;
    private String data;
//    private String md5;

    @Getter
    @Setter
    public static class Session {
//        private String title;
//        private String sessionName;
        private String sessionID;
//        private int rand;
//        private String pager;
    }
}
