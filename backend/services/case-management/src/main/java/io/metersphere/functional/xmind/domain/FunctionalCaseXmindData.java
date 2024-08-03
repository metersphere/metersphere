package io.metersphere.functional.xmind.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
@Data
public class FunctionalCaseXmindData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<FunctionalCaseXmindDTO> functionalCaseList;
    private String moduleName;
    private String moduleId;
    private List<FunctionalCaseXmindData> children = new ArrayList<>();


}
