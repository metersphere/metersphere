package io.metersphere.plugin.platform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlatformBugDTO extends MsBugDTO {
    private List<PlatformCustomFieldItemDTO> customFieldList;
    private List<PlatformAttachment> attachments = new ArrayList<>();
}
