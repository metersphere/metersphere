package io.metersphere.track.request.testplan;

/**
 * @author song.tianyang
 * @Date 2021/4/6 4:16 下午
 * @Description
 */

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 性能案例--批量处理的request
 */
@Getter
@Setter
public class LoadCaseReportBatchRequest {

    List<String> ids;

    private  String projectId;

    private BatchCondition condition;
}
