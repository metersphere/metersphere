package io.metersphere.api.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryJsonPathRequest implements Serializable {
    private String jsonPath;
}
