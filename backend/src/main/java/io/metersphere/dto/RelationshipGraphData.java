package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RelationshipGraphData {

    private List<Node> data;
    private List<Edge> links;

    @Getter
    @Setter
    public static class Node {
        private String id;
        private Integer index;
        private String name;
        private Integer x;
        private Integer y;
    }

    @Getter
    @Setter
    public static class Edge {
        private Integer source;
        private Integer target;
    }
}
