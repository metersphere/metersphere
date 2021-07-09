package io.metersphere.performance.controller.request;

import java.util.HashMap;
import java.util.Map;

public class MetricQuery {

    public static Map<String, String> getMetricQueryMap() {
        return new HashMap<String, String>(16) {{
            // 指标名:promQL
            put("cpu", "100 - (avg by (instance) (irate(node_cpu_seconds_total{instance='%1$s', mode='idle'}[1m])) * 100)");
            put("disk", "100 - node_filesystem_free_bytes{instance='%1$s'} / node_filesystem_size_bytes{instance='%1$s'} * 100");
            put("memory", "(node_memory_MemTotal_bytes{instance='%1$s'} - node_memory_MemFree_bytes{instance='%1$s'}) / node_memory_MemTotal_bytes{instance='%1$s'} * 100");
            put("netIn", "sum by (instance) (irate(node_network_receive_bytes_total{instance='%1$s',device!~'bond.*?|lo'}[1m])/1024)");
            put("netOut", "sum by (instance) (irate(node_network_transmit_bytes_total{instance='%1$s',device!~'bond.*?|lo'}[1m])/1024)");
        }};

    }
}
