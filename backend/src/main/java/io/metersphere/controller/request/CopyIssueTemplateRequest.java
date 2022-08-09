package io.metersphere.controller.request;

import io.metersphere.base.domain.IssueTemplate;
import lombok.Data;

import java.util.List;

@Data
public class CopyIssueTemplateRequest extends IssueTemplate {

    /**
     * targetProjectIds 缺陷模板复制的目标项目ID
     */
    private List<String> targetProjectIds;

    /**
     * copyModel 复制模式 0: 追加, 1: 覆盖
     */
    private String copyModel;
}
