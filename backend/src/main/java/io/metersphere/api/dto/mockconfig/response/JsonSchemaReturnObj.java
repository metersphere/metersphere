package io.metersphere.api.dto.mockconfig.response;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

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

    public String getMockValue() {
        if (mock == null) {
            return "";
        } else {
            return mock.mock;
        }
    }
}

@Getter
@Setter
class mockObj {
    String mock;
}
