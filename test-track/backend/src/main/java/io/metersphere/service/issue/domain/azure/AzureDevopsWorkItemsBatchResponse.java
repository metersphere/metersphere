package io.metersphere.service.issue.domain.azure;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

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
        private Map fields;
        private String url;

        public String getFieldsValue(String name) {
            if (fields != null && fields.containsKey(name)) {
                return fields.get(name).toString();
            }
            return "";
        }
    }
}
