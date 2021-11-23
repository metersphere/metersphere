package io.metersphere.api.dto.definition.parse.swagger;
import lombok.*;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/*
该类表示 swagger3 的 paths 字段下每个请求类型中的 value，即表示一个 api 定义
 */
@Getter
@Setter
public class SwaggerApiInfo {
    private List<String> tags;  //  对应一个 API 在MS项目中所在的 module 名称
    private String summary; //  对应 API 的名字
    private List<JSONObject> parameters; //  对应 API 的请求参数
    private JSONObject requestBody;
    private JSONObject responses;
    private String description;
}
