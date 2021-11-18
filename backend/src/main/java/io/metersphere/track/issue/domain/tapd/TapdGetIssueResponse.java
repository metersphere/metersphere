package io.metersphere.track.issue.domain.tapd;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TapdGetIssueResponse {

    private int status;
    private List<JSONObject> data;
    private String info;
}
