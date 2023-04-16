package io.metersphere.plugin.platform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class SyncIssuesResult {
    private List<MsIssueDTO> updateIssues = new ArrayList<>();
    private List<MsIssueDTO> addIssues = new ArrayList<>();
    private Map<String, List<PlatformAttachment>> attachmentMap = new HashMap<>();
    private List<String> deleteIssuesIds = new ArrayList<>();
}
