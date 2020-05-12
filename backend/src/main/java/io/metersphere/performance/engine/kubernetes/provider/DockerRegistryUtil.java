package io.metersphere.performance.engine.kubernetes.provider;

import com.alibaba.fastjson.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class DockerRegistryUtil {


    public static String getDockerConfig(DockerRegistry registry) {
        Map<String, String> config = new HashMap<>();
        config.put("username", registry.getUsername());
        config.put("password", registry.getPassword());
        config.put("auth", Base64.getEncoder().encodeToString((registry.getUsername() + ":" + registry.getPassword()).getBytes()));

        JSONObject jb = new JSONObject();
        jb.put(registry.getUrl(), config);
        JSONObject result = new JSONObject();
        result.put("auths", jb);
        return Base64.getEncoder().encodeToString(result.toJSONString().getBytes());
    }
}
