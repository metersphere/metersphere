package io.metersphere.request;

import lombok.Data;

import java.util.List;

@Data
public class NodeOperationSelectRequest {
    private List<String> nodeIds;
}
