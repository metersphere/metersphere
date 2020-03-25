package io.metersphere;


import io.metersphere.base.domain.SystemParameter;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.utils.CompressUtils;
import io.metersphere.engine.kubernetes.registry.RegistryService;
import io.metersphere.service.RegistryParamService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
    @Resource
    UserMapper userMapper;
    @Resource
    private RegistryParamService registryParamService;
    @Resource
    private RegistryService registryService;

    @Test
    public void test1() {
        final Object test = CompressUtils.zip("test".getBytes());
        final Object unzip = CompressUtils.unzip(test);
        System.out.println(new String((byte[]) unzip));
    }

    @Test
    public void test2() {
        List<SystemParameter> registry = registryParamService.getRegistry(ParamConstants.Classify.REGISTRY.getValue());
        System.out.println(registry);
        for (SystemParameter sp : registry) {
            if ("registry.password".equals(sp.getParamKey())) {
                sp.setParamValue("Calong@2015");
            }
            if ("registry.url".equals(sp.getParamKey())) {
                sp.setParamValue("registry.fit2cloud.com");
            }
            if ("registry.repo".equals(sp.getParamKey())) {
                sp.setParamValue("metersphere");
            }
            if ("registry.username".equals(sp.getParamKey())) {
                sp.setParamValue("developer");
            }
        }
        registryParamService.updateRegistry(registry);
    }


    @Test
    public void test3() {
        String registry = registryService.getRegistry();
        System.out.println(registry);
    }

}
