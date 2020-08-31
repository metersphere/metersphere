package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DeleteAPIReportRequest {

    private String id;
    private List<String> ids;
}
