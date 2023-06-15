package io.metersphere.system.service;

import io.metersphere.system.dto.TestResourceDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class NodeResourcePoolService {

    public boolean validate(TestResourceDTO testResourceDTO) {
        /*TestResourcePoolBlob testResourcePoolBlob = testResourcePool.getConfiguration();
        if (testResourcePoolBlob == null || testResourcePoolBlob.getConfiguration()==null){
            throw new MSException(Translator.get("no_nodes_message"));
        }*/
        //校验节点
        return true;
    }
}
