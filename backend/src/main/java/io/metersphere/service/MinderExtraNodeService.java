package io.metersphere.service;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.MinderExtraNode;
import io.metersphere.base.domain.MinderExtraNodeExample;
import io.metersphere.base.mapper.MinderExtraNodeMapper;
import io.metersphere.track.request.testcase.TestCaseMinderEditRequest;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class MinderExtraNodeService {

    @Resource
    MinderExtraNodeMapper minderExtraNodeMapper;

    public void batchEdit(TestCaseMinderEditRequest request) {
        TestCaseMinderEditRequest.MinderExtraNodeEditRequest extraNodeRequest = request.getExtraNodeRequest();
        Map<String, List<String>> data = extraNodeRequest.getData();
        if (data != null) {
            data.forEach((parentId, nodes) -> {
                nodes.forEach(node -> {
                    MinderExtraNode minderExtraNode = new MinderExtraNode();
                    minderExtraNode.setNodeData(node);
                    minderExtraNode.setParentId(parentId);
                    JSONObject nodeObj = JSONObject.parseObject(node);
                    String id = nodeObj.getString("id");
                    minderExtraNode.setId(id);
                    if (nodeObj.getBoolean("isEdit")) {
                        minderExtraNodeMapper.updateByPrimaryKeySelective(minderExtraNode);
                    } else {
                        minderExtraNode.setGroupId(extraNodeRequest.getGroupId());
                        minderExtraNode.setType(extraNodeRequest.getType());
                        minderExtraNode.setNodeData(nodeObj.toJSONString());
                        minderExtraNodeMapper.insert(minderExtraNode);
                    }
                });
            });
        }
        List<String> ids = request.getIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            MinderExtraNodeExample example = new MinderExtraNodeExample();
            example.createCriteria().andIdIn(ids);
            minderExtraNodeMapper.deleteByExample(example);
        }
    }

    public List<MinderExtraNode> selectByParentId(String parentId, String groupId) {
        MinderExtraNodeExample example = new MinderExtraNodeExample();
        example.createCriteria().andParentIdEqualTo(parentId).andGroupIdEqualTo(groupId);
        return minderExtraNodeMapper.selectByExampleWithBLOBs(example);
    }
}
