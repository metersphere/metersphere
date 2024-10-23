package io.metersphere.functional.dto;

import io.metersphere.system.log.dto.LogDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class FunctionalMinderUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "新增日志")
    private List<LogDTO> addLogDTOS;

    @Schema(description = "删除日志")
    private List<LogDTO> deleteLogDTOS;

    @Schema(description = "更新日志")
    private List<LogDTO> updateLogDTOS;

    @Schema(description = "新增通知")
    private List<FunctionalCaseDTO> noticeList;

    @Schema(description = "更新通知")
    private List<FunctionalCaseDTO> updateNoticeList;

    @Schema(description = "资源Id与相邻点的Map")
    private Map<String, String> sourceIdAndTargetIdsMap;

    public FunctionalMinderUpdateDTO(List<LogDTO> addLogDTOS,List<LogDTO> deleteLogDTOS, List<LogDTO> updateLogDTOS, List<FunctionalCaseDTO> noticeList, List<FunctionalCaseDTO> updateNoticeList) {
        this.addLogDTOS = addLogDTOS;
        this.deleteLogDTOS = deleteLogDTOS;
        this.updateLogDTOS = updateLogDTOS;
        this.noticeList = noticeList;
        this.updateNoticeList = updateNoticeList;
    }
}
