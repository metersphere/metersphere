package io.metersphere.api.dto.definition.parse.swagger;

import com.alibaba.fastjson.JSONObject;
import lombok.*;

@Getter
@Setter
public class SwaggerParams {
    private String name;    //  对应 API 请求参数名
    private String in;  //  参数类型，可选值为 path,header,query 等
    private String description;
    private boolean required;   //  是否是必填参数
    private JSONObject schema;
}
