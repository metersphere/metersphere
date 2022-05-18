package io.metersphere.api.dto.definition;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
