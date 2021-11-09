package io.metersphere.api.jmeter;

import io.metersphere.api.dto.JvmInfoDTO;
import lombok.Data;

import java.util.List;

@Data
public class ReportCounter {
    private List<String> completedIds;
    private List<JvmInfoDTO> poolUrls;
    private List<String> reportIds;
}
