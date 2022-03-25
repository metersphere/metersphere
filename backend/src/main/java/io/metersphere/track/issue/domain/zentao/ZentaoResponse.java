package io.metersphere.track.issue.domain.zentao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZentaoResponse {
    private String status;
    private String md5;
    private String data;
}
