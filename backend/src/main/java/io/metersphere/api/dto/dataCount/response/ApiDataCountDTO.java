package io.metersphere.api.dto.dataCount.response;

import io.metersphere.api.dto.dataCount.ApiDataCountResult;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.base.domain.ApiDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Locale;

/**
 * 接口数据统计返回
 */
@Getter
@Setter
public class ApiDataCountDTO {

    //接口统计
    private long allApiDataCountNumber = 0;
    //http接口统计
    private long httpApiDataCountNumber = 0;
    //rpc接口统计
    private long rpcApiDataCountNumber = 0;
    //tcp接口统计
    private long tcpApiDataCountNumber = 0;
    //sql接口统计
    private long sqlApiDataCountNumber = 0;

    //本周新增数量
    private long thisWeekAddedCount = 0;
    //本周执行数量
    private long thisWeekExecutedCount = 0;
    //历史总执行数量
    private long executedCount = 0;

    public ApiDataCountDTO(){}

    /**
     * 通过ApiDefinitionCountResult统计查询结果进行数据合计
     * @param countResultList
     */
    public void countByApiDefinitionCountResult(List<ApiDataCountResult> countResultList){
        for (ApiDataCountResult countResult :
                countResultList) {
            switch (countResult.getGroupField().toUpperCase()){
                case RequestType.DUBBO:
                    this.rpcApiDataCountNumber += countResult.getCountNumber();
                    break;
                case RequestType.HTTP:
                    this.httpApiDataCountNumber += countResult.getCountNumber();
                    break;
                case RequestType.SQL:
                    this.sqlApiDataCountNumber += countResult.getCountNumber();
                    break;
                case RequestType.TCP:
                    this.tcpApiDataCountNumber += countResult.getCountNumber();
                    break;
                default:
                    break;
            }
            allApiDataCountNumber += countResult.getCountNumber();
        }
    }
}
