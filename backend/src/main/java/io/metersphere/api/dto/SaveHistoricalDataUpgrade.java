package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SaveHistoricalDataUpgrade {
    private List<String> testIds;

    private String projectId;

    private String modulePath;

    private String moduleId;
}
