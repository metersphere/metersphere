package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.dto.CustomFieldDao;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ApiDefinitionResult extends ApiDefinitionWithBLOBs {

    private String projectName;

    private String userName;

    private String caseTotal;

    private String caseStatus;

    private int scenarioTotal;

    private String casePassingRate;

    private String deleteUser;

    private List<String> scenarioIds;

    private String caseType;

    private String apiType;

    private String versionName;

    private Boolean versionEnable;

    private boolean updated;

    private List<CustomFieldDao> fields;
}
