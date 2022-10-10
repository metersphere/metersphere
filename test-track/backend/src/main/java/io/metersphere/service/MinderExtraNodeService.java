package io.metersphere.service;

import io.metersphere.base.domain.MinderExtraNode;
import io.metersphere.base.domain.MinderExtraNodeExample;
import io.metersphere.base.mapper.MinderExtraNodeMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.request.testcase.TestCaseMinderEditRequest;
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
                    Map nodeObj = JSON.parseMap(node);
                    String id = nodeObj.get("id").toString();
                    minderExtraNode.setId(id);
                    if ((Boolean) nodeObj.get("isEdit")) {
                        minderExtraNodeMapper.updateByPrimaryKeySelective(minderExtraNode);
                    } else {
                        minderExtraNode.setGroupId(extraNodeRequest.getGroupId());
                        minderExtraNode.setType(extraNodeRequest.getType());
                        minderExtraNode.setNodeData(node);
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
