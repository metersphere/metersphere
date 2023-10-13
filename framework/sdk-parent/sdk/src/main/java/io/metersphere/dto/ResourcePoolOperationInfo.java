package io.metersphere.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
public class ResourcePoolOperationInfo {
    Map<String, NodeOperationInfo> nodeOperationInfos = new HashMap<>();
    private String id;
    private String cpuUsage;
    private int runningTask = 0;

    public void addNodeOperationInfo(String taskResourceId, String ip, String port, String cpuUsage, int runningTask) {
        if (StringUtils.isBlank(cpuUsage) && runningTask < 0) {
            //节点下如果获取不到cpu使用率，且没有查到runningTask数据，判断为没有查询到该节点的数据。
            return;
        } else if (StringUtils.isBlank(cpuUsage)) {
            cpuUsage = "NONE";
        }
        NodeOperationInfo nodeOperationInfo = new NodeOperationInfo();
        nodeOperationInfo.setIp(ip);
        nodeOperationInfo.setPort(port);
        nodeOperationInfo.setCpuUsage(cpuUsage);
        nodeOperationInfo.setRunningTask(runningTask);
        nodeOperationInfos.put(taskResourceId, nodeOperationInfo);

        this.cpuUsage = cpuUsage;
        this.runningTask += runningTask;

        if (nodeOperationInfos.size() > 1) {
            //多节点的情况下暂不处理CPU使用率
            this.cpuUsage = "NONE";
        }
    }
}

@Data
class NodeOperationInfo {
    private String ip;
    private String port;
    private String cpuUsage;
    private int runningTask;
}