package io.metersphere.service;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.MinderExtraNode;
import io.metersphere.base.domain.MinderExtraNodeExample;
import io.metersphere.base.mapper.MinderExtraNodeMapper;
import io.metersphere.track.request.MinderExtraNodeEditRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class MinderExtraNodeService {

    @Resource
    MinderExtraNodeMapper minderExtraNodeMapper;

    public void batchEdit(MinderExtraNodeEditRequest request) {
        Map<String, List<String>> data = request.getData();
        if (data != null) {
            data.forEach((parentId, nodes) -> {
                nodes.forEach(node -> {
                    MinderExtraNode minderExtraNode = new MinderExtraNode();
                    minderExtraNode.setNodeData(node);
                    minderExtraNode.setParentId(parentId);
                    JSONObject nodeObj = JSONObject.parseObject(node);
                    String id = nodeObj.getString("id");
                    if (StringUtils.isBlank(id) || id.length() < 20) {
                        minderExtraNode.setId(UUID.randomUUID().toString());
                        minderExtraNode.setGroupId(request.getGroupId());
                        minderExtraNode.setType(request.getType());
                        nodeObj.put("id", minderExtraNode.getId());
                        minderExtraNode.setNodeData(nodeObj.toJSONString());
                        minderExtraNodeMapper.insert(minderExtraNode);
                    } else {
                        minderExtraNode.setId(id);
                        minderExtraNodeMapper.updateByPrimaryKeySelective(minderExtraNode);
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