package io.metersphere.performance.base;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChartsData {

    /**
     * X 轴
     */
    private String xAxis;

    /**
     * Y 轴
     */
    private BigDecimal yAxis = BigDecimal.ZERO;

    /**
     * Y 轴右侧
     */
    private BigDecimal yAxis2 = BigDecimal.ZERO;

    /**
     * series 名称
     */
    private String groupName;

    /**
     * 描述
     */
    private String description;

    public String getxAxis() {
        return xAxis;
    }

    public void setxAxis(String xAxis) {
        this.xAxis = xAxis;
    }

    public BigDecimal getyAxis() {
        return yAxis;
    }

    public void setyAxis(BigDecimal yAxis) {
        this.yAxis = yAxis;
    }

    public BigDecimal getyAxis2() {
        return yAxis2;
    }

    public void setyAxis2(BigDecimal yAxis2) {
        this.yAxis2 = yAxis2;
    }
}
