package io.metersphere.api.parse.postman;

import lombok.Data;

import java.util.List;

@Data
public class PostmanScript {
    private List<String> exec;
    private String type;
}
