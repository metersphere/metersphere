package io.metersphere.consul;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.controller.handler.annotation.NoResultHolder;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("v1")
public class CatalogApiController {
    @Resource
    private ConsulService consulService;

    @GetMapping("agent/self")
    @NoResultHolder
    public ResponseEntity<JSONObject> self() {

        JSONObject result = JSONObject.parseObject("{\n" +
                "    \"Config\": {\n" +
                "        \"Datacenter\": \"dc1\",\n" +
                "        \"NodeName\": \"dade5a85b216\",\n" +
                "        \"NodeID\": \"6d1e33c5-c18b-8a5a-cbf2-c2314031cc25\",\n" +
                "        \"Revision\": \"10bb6cb3b\",\n" +
                "        \"Server\": true,\n" +
                "        \"Version\": \"1.9.4\"\n" +
                "    },\n" +
                "}");
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Consul-Default-Acl-Policy", "allow");

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(result);
    }

    @GetMapping("catalog/services")
    @NoResultHolder
    public ResponseEntity<Map<String, List<String>>> catalog() {
        Map<String, List<String>> activeNodes = consulService.getActiveNodes();
        HttpHeaders responseHeaders = new HttpHeaders();
        int index = RandomUtils.nextInt(1000, 20000);
        responseHeaders.set("X-Consul-Default-Acl-Policy", "allow");
        responseHeaders.set("X-Consul-Effective-Consistency", "leader");
        responseHeaders.set("X-Consul-Index", index + "");
        responseHeaders.set("X-Consul-Knownleader", "true");
        responseHeaders.set("X-Consul-Lastcontact", "0");

        // 刷新缓存
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(activeNodes);

    }

    @GetMapping("health/service/{service}")
    @NoResultHolder
    public ResponseEntity<JSONArray> health(@PathVariable String service) {
        Map<String, List<String>> activeNodes = consulService.getActiveNodes();
        int index = RandomUtils.nextInt(1000, 20000);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Consul-Default-Acl-Policy", "allow");
        responseHeaders.set("X-Consul-Index", index + "");
        responseHeaders.set("X-Consul-Knownleader", "true");
        responseHeaders.set("X-Consul-Lastcontact", "0");

        if (!activeNodes.containsKey(service)) {
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(JSON.parseArray("[]"));
        }

        int i = service.lastIndexOf("-");
        String address = service.substring(0, i);
        String port = service.substring(i + 1);
        String result = "[\n" +
                "    {\n" +
                "        \"Node\": {\n" +
                "            \"ID\": \"6d1e33c5-c18b-8a5a-cbf2-c2314031cc25\",\n" +
                "            \"Node\": \"dade5a85b216\",\n" +
                "            \"Address\": \"127.0.0.1\",\n" +
                "            \"Datacenter\": \"dc1\",\n" +
                "            \"TaggedAddresses\": {\n" +
                "                \"lan\": \"127.0.0.1\",\n" +
                "                \"lan_ipv4\": \"127.0.0.1\",\n" +
                "                \"wan\": \"127.0.0.1\",\n" +
                "                \"wan_ipv4\": \"127.0.0.1\"\n" +
                "            },\n" +
                "            \"Meta\": {\n" +
                "                \"consul-network-segment\": \"\"\n" +
                "            },\n" +
                "            \"CreateIndex\": 11,\n" +
                "            \"ModifyIndex\": 13\n" +
                "        },\n" +
                "        \"Service\": {\n" +
                "            \"ID\": \"" + service + "\",\n" +
                "            \"Service\": \"" + service + "\",\n" +
                "            \"Tags\": [\n" +
                "                \"test\"\n" +
                "            ],\n" +
                "            \"Address\": \"" + address + "\",\n" +
                "            \"Meta\": null,\n" +
                "            \"Port\": " + port + ",\n" +
                "            \"Weights\": {\n" +
                "                \"Passing\": 1,\n" +
                "                \"Warning\": 1\n" +
                "            },\n" +
                "            \"EnableTagOverride\": false,\n" +
                "            \"Proxy\": {\n" +
                "                \"MeshGateway\": {},\n" +
                "                \"Expose\": {}\n" +
                "            },\n" +
                "            \"Connect\": {},\n" +
                "            \"CreateIndex\": " + index + ",\n" +
                "            \"ModifyIndex\": " + index + "\n" +
                "        },\n" +
                "        \"Checks\": [\n" +
                "            {\n" +
                "                \"Node\": \"dade5a85b216\",\n" +
                "                \"CheckID\": \"serfHealth\",\n" +
                "                \"Name\": \"Serf Health Status\",\n" +
                "                \"Status\": \"passing\",\n" +
                "                \"Notes\": \"\",\n" +
                "                \"Output\": \"Agent alive and reachable\",\n" +
                "                \"ServiceID\": \"\",\n" +
                "                \"ServiceName\": \"\",\n" +
                "                \"ServiceTags\": [],\n" +
                "                \"Type\": \"\",\n" +
                "                \"Definition\": {},\n" +
                "                \"CreateIndex\": 11,\n" +
                "                \"ModifyIndex\": 11\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "]";


        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(JSON.parseArray(result));
    }
}
