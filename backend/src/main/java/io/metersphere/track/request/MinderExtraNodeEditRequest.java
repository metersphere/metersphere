package io.metersphere.track.request;

import io.metersphere.base.domain.MinderExtraNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MinderExtraNodeEditRequest extends MinderExtraNode {
    private String projectId;
    // 删除的id
    private List<String> ids;
    // key 为父节点id
    private Map<String, List<String>> data;
}
