package io.metersphere.service;

import io.metersphere.base.domain.IssueTemplate;
import io.metersphere.base.mapper.IssueTemplateMapper;
import io.metersphere.base.mapper.ext.ExtIssueTemplateMapper;
import io.metersphere.commons.constants.TemplateConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.UpdateIssueTemplateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssueTemplateService {

    @Resource
    ExtIssueTemplateMapper extIssueTemplateMapper;

    @Resource
    IssueTemplateMapper issueTemplateMapper;

    @Resource
    CustomFieldTemplateService customFieldTemplateService;

    public void add(UpdateIssueTemplateRequest request) {
        IssueTemplate template = new IssueTemplate();
        BeanUtils.copyBean(template, request);
        template.setId(UUID.randomUUID().toString());
        template.setCreateTime(System.currentTimeMillis());
        template.setUpdateTime(System.currentTimeMillis());
        issueTemplateMapper.insert(template);
        customFieldTemplateService.create(request.getCustomFields(), template.getId(),
                TemplateConstants.FieldTemplateScene.ISSUE.name());
    }

    public List<IssueTemplate> list(BaseQueryRequest request) {
        return extIssueTemplateMapper.list(request);
    }

    public void delete(String id) {
        issueTemplateMapper.deleteByPrimaryKey(id);
    }

    public void update(UpdateIssueTemplateRequest request) {
        customFieldTemplateService.deleteByTemplateId(request.getId());
        IssueTemplate template = new IssueTemplate();
        BeanUtils.copyBean(template, request);
        template.setUpdateTime(System.currentTimeMillis());
        issueTemplateMapper.updateByPrimaryKeySelective(template);
        customFieldTemplateService.create(request.getCustomFields(), request.getId(),
                TemplateConstants.FieldTemplateScene.ISSUE.name());
    }
}
