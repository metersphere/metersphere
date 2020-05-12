package io.metersphere.performance.base;

import lombok.Data;

@Data
public class Statistics {

    private String label;

    private String samples;

    private String ko;

    private String error;

    private String average;

    private String min;

    private String max;

    private String tp90;

    private String tp95;

    private String tp99;

    private String transactions;

    private String received;

    private String sent;

}
