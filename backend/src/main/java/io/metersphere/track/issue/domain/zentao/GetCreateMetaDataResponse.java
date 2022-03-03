package io.metersphere.track.issue.domain.zentao;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class GetCreateMetaDataResponse extends ZentaoResponse {

    @Getter
    @Setter
    public static class MetaData {
        private String title;
        private JSONObject users;
        private JSONObject customFields;
        private Map<String, Object> builds;
    }
}
