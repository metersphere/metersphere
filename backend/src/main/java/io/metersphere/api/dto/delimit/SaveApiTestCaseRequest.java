package io.metersphere.api.dto.delimit;

import io.metersphere.api.dto.scenario.request.Request;
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

    private String apiDelimitId;

    private String description;

    private Request request;

    private String response;

    private String crateUserId;

    private String updateUserId;

    private Long createTime;

    private Long updateTime;

    private List<String> bodyUploadIds;
}
