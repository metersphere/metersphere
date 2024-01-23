package io.metersphere.plugin.platform.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PlatformCustomFieldMultiLevelOption implements Serializable {

    private String value;

    private String text;

    private List<PlatformCustomFieldMultiLevelOption> children;
}
