package io.metersphere.system.notice;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class NoticeModel implements Serializable {
    /**
     * 保存 测试id
     */
    private String testId;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 保存状态
     */
    private String status;
    /**
     * Event
     */
    private String event;
    /**
     * 消息主题
     */
    private String subject;
    /**
     * 消息内容
     */
    private String context;

    /**
     * 保存特殊的用户
     */
    private List<String> relatedUsers;

    /**
     * 模版里的参数信息
     */
    private Map<String, Object> paramMap;
    /**
     * 接收人
     */
    private List<Receiver> receivers;
    /**
     * 抄送人
     */
    private List<Receiver> recipients;
    /**
     * 排除自己
     */
    private boolean excludeSelf;
}
