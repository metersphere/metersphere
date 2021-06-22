package io.metersphere.performance.base;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Statistics {

    private String label;

    private BigDecimal samples;

    private BigDecimal fail;

    private BigDecimal error;

    private BigDecimal average;

    private BigDecimal min;

    private BigDecimal max;

    private BigDecimal median;

    private BigDecimal tp90;

    private BigDecimal tp95;

    private BigDecimal tp99;

    private BigDecimal transactions;

    private BigDecimal received;

    private BigDecimal sent;

}
