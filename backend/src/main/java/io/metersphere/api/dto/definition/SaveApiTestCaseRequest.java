package io.metersphere.api.dto.definition;

import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.base.domain.ApiTestCase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SaveApiTestCaseRequest extends ApiTestCase {

    private MsTestElement request;

    private String description;

    private String response;

    private String crateUserId;

    private List<String> bodyUploadIds;

    private List<String> follows;

    private String versionId;

    //ESB参数。  可为null
    private String esbDataStruct;
    private String backEsbDataStruct;
}
