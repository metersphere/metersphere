package io.metersphere.track.issue.domain.azure;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
// 放xpack，代码混淆会导致 System.WorkItemType， System.Title 反序列化失败
public class AzureDevopsWorkItemsBatchResponse {

    private int count;
    private List<Value> value;

    @Getter
    @Setter
    public class Value {
        private int id;
        private int rev;
        private JSONObject fields;
        private String url;

        public String getFieldsValue(String name) {
            if (fields != null && fields.containsKey(name)) {
                return fields.getString(name);
            }
            return "";
        }
    }
}
