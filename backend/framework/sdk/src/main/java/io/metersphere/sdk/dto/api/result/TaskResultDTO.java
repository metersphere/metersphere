package io.metersphere.sdk.dto.api.result;

import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class TaskResultDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务执行结果数据结构
     * {@link RequestResult}
     */
    private List<RequestResult> requestResults;
    /**
     * 控制台信息
     */
    private String console;
    /**
     * debug 信息
     */
    private String runningDebugSampler;
    /**
     * 线程组执行完成标识
     */
    private Boolean hasEnded;
    /**
     * 执行过程状态
     * {@link ProcessResultDTO}
     */
    private ProcessResultDTO processResultDTO;

    /**
     * 请求参数
     * {@link TaskRequestDTO}
     */
    private TaskRequestDTO request;
}
