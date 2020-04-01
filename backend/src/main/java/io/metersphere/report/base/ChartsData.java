package io.metersphere.report.base;

import java.util.List;

public class ChartsData {

    private String xAxis;
    private String yAxis;
    private String yAxis1;
    private String serices;

    ////
    private List<String> time;
    private List<String> users;
    private List<String> hits;
    private List<String> errors;

    public String getxAxis() {
        return xAxis;
    }

    public void setxAxis(String xAxis) {
        this.xAxis = xAxis;
    }

    public String getyAxis() {
        return yAxis;
    }

    public void setyAxis(String yAxis) {
        this.yAxis = yAxis;
    }

    public String getyAxis1() {
        return yAxis1;
    }

    public void setyAxis1(String yAxis1) {
        this.yAxis1 = yAxis1;
    }

    public String getSerices() {
        return serices;
    }

    public void setSerices(String serices) {
        this.serices = serices;
    }

    public List<String> getTime() {
        return time;
    }

    public void setTime(List<String> time) {
        this.time=time;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users=users;
    }

    public List<String> getHits() {
        return hits;
    }

    public void setHits(List<String> hits) {
        this.hits=hits;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors=errors;
    }
}
