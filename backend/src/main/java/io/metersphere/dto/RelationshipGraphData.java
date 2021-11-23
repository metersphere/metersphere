package io.metersphere.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RelationshipGraphData {

    private List<Node> data;
    private List<Edge> links;

    // x 轴占用了多少单位
    private int xUnitCount;
    // y 轴占用了多少单位
    private int yUnitCount;

    public RelationshipGraphData() {
        this.data = new ArrayList<>();
        this.links = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Node {
        private String id;
        private Integer index;
        private String name;
        private Integer category; // 分组
        private Integer x;
        private Integer y;
        private Boolean visited = false;
    }

    @Getter
    @Setter
    public static class Edge {
        private Integer source;
        private Integer target;
        private float curveness;
    }

    /**
     * 记录当前遍历时，已经占用的 x 坐标的最大最小值
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class XAxisMark {
        private Integer min;
        private Integer max;
    }
}
