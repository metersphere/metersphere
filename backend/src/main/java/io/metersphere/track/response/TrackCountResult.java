package io.metersphere.track.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackCountResult {

    /**
     * 分组统计字段
     */
    private String groupField;
    /**
     * 数据统计
     */
    private long countNumber;
}
