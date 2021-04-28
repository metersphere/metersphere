package io.metersphere.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EnvironmentRequest {

    private String name;    //查询时的环境名称条件
    private List<String> projectIds;
}
