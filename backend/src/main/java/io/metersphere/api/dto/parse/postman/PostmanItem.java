package io.metersphere.api.dto.parse.postman;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class PostmanItem {
    private String name;
    private List<PostmanEvent> event;
    private PostmanRequest request;
    private List<PostmanResponse> response;
    private List<PostmanItem> item;
    private ProtocolProfileBehavior protocolProfileBehavior;

    @Getter
    @Setter
    public class ProtocolProfileBehavior {
        private Boolean followRedirects = true;
    }
}
