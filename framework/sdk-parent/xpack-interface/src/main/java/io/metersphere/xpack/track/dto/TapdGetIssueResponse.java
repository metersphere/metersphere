package io.metersphere.xpack.track.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TapdGetIssueResponse extends TapdBaseResponse {
    private List<Map> data;
}
