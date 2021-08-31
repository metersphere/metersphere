package io.metersphere.api.jmeter;

import io.metersphere.api.dto.JvmInfoDTO;
import lombok.Data;

import java.util.List;

@Data
public class ReportCounter {
    private int number;
    private List<JvmInfoDTO> poolUrls;
    private List<String> reportIds;
}
