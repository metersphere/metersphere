package io.metersphere.service.issue.domain.zentao;

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
        private Map users;
        private Map customFields;
        private Map<String, Object> builds;
    }
}
