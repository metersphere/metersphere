package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.definition.request.MsTestElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SaveApiTestCaseRequest {

    private String id;

    private String projectId;

    private String name;

    private String priority;

    private String apiDefinitionId;

    private String description;

    private MsTestElement request;

    private String response;

    private String crateUserId;

    private String updateUserId;

    private Long createTime;

    private Long updateTime;

    private List<String> bodyUploadIds;

    private String tags;

    //ESB参数。  可为null
    private String esbDataStruct;
    private String backEsbDataStruct;
}
