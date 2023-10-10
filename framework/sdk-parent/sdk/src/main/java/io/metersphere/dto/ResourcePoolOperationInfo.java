package io.metersphere.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ResourcePoolOperationInfo {
    private String id;
    private String cpuUsage;
    private int runningTask = 0;
    Map<String, NodeOperationInfo> nodeOperationInfos = new HashMap<>();

    public void addNodeOperationInfo(String taskResourceId, String ip, String port, String cpuUsage, int runningTask) {
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
            this.cpuUsage = null;
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