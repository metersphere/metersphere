package io.metersphere.plugin.platform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class SyncBugResult {
    private List<MsBugDTO> updateBug= new ArrayList<>();
    private List<MsBugDTO> addBug = new ArrayList<>();
    private Map<String, List<PlatformAttachment>> attachmentMap = new HashMap<>();
    private List<String> deleteBugIds = new ArrayList<>();
}
