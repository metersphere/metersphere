package io.metersphere.track.issue.domain.azure;

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
        private Fields fields;
        private String url;

        @Getter
        @Setter
        public class Fields {
            @JSONField(name = "System.Id")
            private int systemId;
            @JSONField(name = "System.WorkItemType")
            private String systemWorkItemType;
            @JSONField(name = "System.Title")
            private String systemTitle;
        }
    }
}
