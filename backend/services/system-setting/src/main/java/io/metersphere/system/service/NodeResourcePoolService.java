package io.metersphere.system.service;


import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.TestResourcePoolDTO;
import org.apache.commons.collections4.CollectionUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class NodeResourcePoolService {

    public boolean validate(TestResourcePoolDTO testResourcePool) {
        if (CollectionUtils.isEmpty(testResourcePool.getTestResources())) {
           throw new MSException(Translator.get("no_nodes_message"));
        }
        //校验节点
        return true;
    }
}
