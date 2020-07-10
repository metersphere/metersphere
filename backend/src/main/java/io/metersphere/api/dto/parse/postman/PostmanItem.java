package io.metersphere.api.dto.parse.postman;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class PostmanItem {

    private String name;
    private PostmanRequest request;
}
