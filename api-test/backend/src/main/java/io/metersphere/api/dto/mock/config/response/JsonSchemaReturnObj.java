package io.metersphere.api.dto.mock.config.response;


import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

/**
 * @author song.tianyang
 * @Date 2021/4/30 3:11 下午
 * @Description
 */
@Getter
@Setter
public class JsonSchemaReturnObj {
    private mockObj mock;
    private int minLength = -1;
    private String pattern;
    private String format;
    private String description;
    private String title;
    private String type;
    private int maxLength = -1;
    private JSONObject properties;
    private JSONObject items;
}

@Getter
@Setter
class mockObj {
    String mock;
}
