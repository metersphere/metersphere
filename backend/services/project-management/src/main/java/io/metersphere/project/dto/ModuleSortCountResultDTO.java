package io.metersphere.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModuleSortCountResultDTO {
    private boolean isRefreshPos;
    private long pos;
}
