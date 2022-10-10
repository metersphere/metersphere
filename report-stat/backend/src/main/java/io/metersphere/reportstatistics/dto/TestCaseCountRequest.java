package io.metersphere.reportstatistics.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestCaseCountRequest {
    //x轴字段
    private String xaxis;
    //y轴字段
    private List<String> yaxis;

    //搜索条件
    private String projectId;
    private String timeType;
    private TimeFilter timeFilter;
    private List<Long> times;
    private String order;

    //起始时间
    private long startTime = 0;
    //结束时间
    private long endTime = 0;

    //其余条件
    private String filterType;
    private List<Map<String,Object>> filters;

    /**
     * 功能用例、接口用例、场景用例、性能用例的分组字段
     */
    private String testCaseGroupColumn;
    private String apiCaseGroupColumn;
    private String scenarioCaseGroupColumn;
    private String loadCaseGroupColumn;

    /**
     * filter整理后的查询数据
     * @return
     */
    private Map<String,List<String>> filterSearchList;
    private Map<String,List<String>> apiFilterSearchList;
    private Map<String,List<String>> loadFilterSearchList;

    public int getTimeRange(){
        if(timeFilter != null){
            return timeFilter.getTimeRange();
        }else {
            return 0;
        }
    }

    public String getTimeRangeUnit(){
        if(timeFilter != null){
            return timeFilter.getTimeRangeUnit();
        }else {
            return null;
        }
    }

    public void setFilterSearchList(String key,List<String> values){
        if(this.filterSearchList == null){
            this.filterSearchList = new HashMap<>();
        }
        filterSearchList.put(key,values);
    }
}

@Getter
@Setter
class TimeFilter{
    private int timeRange;
    private String timeRangeUnit;
}
