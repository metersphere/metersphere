package io.metersphere.api.dto.definition.parse.postman;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class PostmanCollectionInfo {

    @JSONField(name = "_postman_id")
    private String postmanId;
    private String name;
    private String schema;
}
