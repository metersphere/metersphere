package io.metersphere.api.dto.delimit;

import io.metersphere.api.dto.scenario.request.Request;
import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SaveApiDelimitRequest {

    private String id;

    private String projectId;

    private String name;

    private String url;

    private String moduleId;

    private String status;

    private String description;

    private String modulePath;

    private String path;

    private Request request;

    private String userId;

    private Schedule schedule;

    private String triggerMode;

    private List<String> bodyUploadIds;
}
