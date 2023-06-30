package io.metersphere.sdk.service;


import io.metersphere.sdk.dto.TestResourceDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class NodeResourcePoolService {

    public boolean validate(TestResourceDTO testResourceDTO) {
        if (CollectionUtils.isEmpty(testResourceDTO.getNodesList())) {
            throw new MSException(Translator.get("no_nodes_message"));
        }
        //校验节点
        return true;
    }
}
