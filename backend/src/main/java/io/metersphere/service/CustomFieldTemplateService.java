package io.metersphere.service;


import io.metersphere.base.domain.CustomFieldTemplate;
import io.metersphere.base.domain.CustomFieldTemplateExample;
import io.metersphere.base.mapper.CustomFieldTemplateMapper;
import io.metersphere.base.mapper.ext.ExtCustomFieldTemplateMapper;
import io.metersphere.dto.CustomFieldTemplateDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFieldTemplateService {

    @Resource
    CustomFieldTemplateMapper customFieldTemplateMapper;
    @Resource
    ExtCustomFieldTemplateMapper extCustomFieldTemplateMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;

    public List<String> getCustomFieldIds(String templateId) {
        return extCustomFieldTemplateMapper.getCustomFieldIds(templateId);
    }

    public List<CustomFieldTemplateDao> list(CustomFieldTemplate request) {
        return extCustomFieldTemplateMapper.list(request);
    }

    public void deleteByTemplateId(String templateId) {
        if (StringUtils.isNotBlank(templateId)) {
            CustomFieldTemplateExample example = new CustomFieldTemplateExample();
            example.createCriteria().andTemplateIdEqualTo(templateId);
            customFieldTemplateMapper.deleteByExample(example);
        }
    }

    public void create(List<CustomFieldTemplate> customFields, String templateId, String scene) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        CustomFieldTemplateMapper customFieldTemplateMapper =
                sqlSession.getMapper(CustomFieldTemplateMapper.class);
        if (CollectionUtils.isNotEmpty(customFields)) {
            customFields.forEach(item -> {
                item.setId(UUID.randomUUID().toString());
                item.setTemplateId(templateId);
                item.setScene(scene);
                if (item.getRequired() == null) {
                    item.setRequired(false);
                }
                customFieldTemplateMapper.insert(item);
            });
        }
        sqlSession.flushStatements();
        sqlSession.close();
    }
}
