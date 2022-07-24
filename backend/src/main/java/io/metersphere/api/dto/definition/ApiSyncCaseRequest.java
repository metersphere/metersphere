package io.metersphere.api.dto.definition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiSyncCaseRequest {

    private Boolean protocol;

    private Boolean method;

    private Boolean path;

    private Boolean headers;

    private Boolean query;

    private Boolean rest;

    private Boolean body;

    private Boolean delNotSame;

    private Boolean runError;

    private Boolean unRun;

    private List<String> ids;

    private Boolean selectAll;
}
