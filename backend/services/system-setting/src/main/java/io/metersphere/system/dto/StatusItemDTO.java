package io.metersphere.system.dto;

import io.metersphere.system.domain.StatusItem;
import lombok.Data;

import java.util.List;


/**
 * @author song-cc-rock
 */
@Data
public class StatusItemDTO extends StatusItem {

    private List<String> statusDefinitions;
    private List<String> statusFlowTargets;
}
