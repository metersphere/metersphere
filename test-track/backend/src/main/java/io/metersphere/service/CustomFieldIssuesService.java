package io.metersphere.service;

import com.alibaba.nacos.common.utils.CollectionUtils;
import io.metersphere.base.domain.CustomField;
import io.metersphere.base.domain.CustomFieldIssues;
import io.metersphere.base.domain.CustomFieldIssuesExample;
import io.metersphere.base.mapper.CustomFieldIssuesMapper;
import io.metersphere.base.mapper.ext.BaseCustomFieldResourceMapper;
import io.metersphere.constants.SystemCustomField;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldResourceDTO;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFieldIssuesService extends CustomFieldResourceService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private CustomFieldIssuesMapper customFieldIssuesMapper;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;

    private static final String TABLE_NAME = "custom_field_issues";

    public void addFields(String resourceId, List<CustomFieldResourceDTO> addFields) {
        super.addFields(TABLE_NAME, resourceId, addFields);
    }

    public void editFields(String resourceId, List<CustomFieldResourceDTO> editFields) {
        super.editFields(TABLE_NAME, resourceId, editFields);
    }

    public void batchEditFields(String resourceId, List<CustomFieldResourceDTO> fields) {
        super.batchEditFields(TABLE_NAME, resourceId, fields);
    }

    public int updateByPrimaryKeySelective(CustomFieldResourceDTO field) {
        return super.updateByPrimaryKeySelective(TABLE_NAME, field);
    }

    public int insert(CustomFieldResourceDTO field) {
        return super.insert(TABLE_NAME, field);
    }

    public void deleteByResourceId(String resourceId) {
        super.deleteByResourceId(TABLE_NAME, resourceId);
    }

    public void deleteByResourceIds(List<String> resourceIds) {
        super.deleteByResourceIds(TABLE_NAME, resourceIds);
    }

    public Map<String, List<CustomFieldDao>> getMapByResourceIds(List<String> resourceIds) {
        return super.getMapByResourceIds(TABLE_NAME, resourceIds);
    }

    public List<CustomFieldResourceDTO> getByResourceId(String resourceId) {
        return super.getByResourceId(TABLE_NAME, resourceId);
    }

    public void batchEditFields(Map<String, List<CustomFieldResourceDTO>> customFieldMap) {
        if (customFieldMap == null || customFieldMap.size() == 0) {
            return;
        }
        this.checkInit();
        SqlSession sqlSession = ServiceUtils.getBatchSqlSession();
        BaseCustomFieldResourceMapper batchMapper = sqlSession.getMapper(BaseCustomFieldResourceMapper.class);
        List<CustomFieldResourceDTO> addList = new ArrayList<>();
        List<CustomFieldResourceDTO> updateList = new ArrayList<>();

        Set<String> set = customFieldMap.keySet();
        if (CollectionUtils.isEmpty(set)) {
            return;
        }
        CustomFieldIssuesExample example = new CustomFieldIssuesExample();
        example.createCriteria().andResourceIdIn(new ArrayList<>(set));
        List<CustomFieldIssues> customFieldIssues = customFieldIssuesMapper.selectByExample(example);
        Map<String, List<String>> resourceFieldMap = customFieldIssues.stream()
                .collect(Collectors.groupingBy(CustomFieldIssues::getResourceId, Collectors.mapping(CustomFieldIssues::getFieldId, Collectors.toList())));

        for (String resourceId : customFieldMap.keySet()) {
            List<CustomFieldResourceDTO> list = customFieldMap.get(resourceId);
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }
            List<String> fieldIds = resourceFieldMap.get(resourceId);
            for (CustomFieldResourceDTO CustomFieldResourceDTO : list) {
                CustomFieldResourceDTO.setResourceId(resourceId);
                if (CollectionUtils.isEmpty(fieldIds) || !fieldIds.contains(CustomFieldResourceDTO.getFieldId())) {
                    addList.add(CustomFieldResourceDTO);
                } else {
                    updateList.add(CustomFieldResourceDTO);
                }
            }
        }
        addList.forEach(l -> batchMapper.insert(TABLE_NAME, l));
        updateList.forEach(l -> batchMapper.updateByPrimaryKeySelective(TABLE_NAME, l));
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public Map<String, String> getIssueStatusMap(List<String> issueIds, String projectId) {
        if (CollectionUtils.isEmpty(issueIds)) {
            return new HashMap<>(0);
        }
        CustomField customField = baseCustomFieldService.getCustomFieldByName(projectId, SystemCustomField.ISSUE_STATUS);
        CustomFieldIssuesExample example = new CustomFieldIssuesExample();
        example.createCriteria().andFieldIdEqualTo(customField.getId()).andResourceIdIn(issueIds);
        List<CustomFieldIssues> customFieldIssues = customFieldIssuesMapper.selectByExample(example);
        Map<String, String> statusMap = customFieldIssues.stream().collect(Collectors.toMap(CustomFieldIssues::getResourceId, CustomFieldIssues::getValue));
        return statusMap;
    }

    public Map<String, String> getIssueDegreeMap(List<String> issueIds, String projectId) {
        if (CollectionUtils.isEmpty(issueIds)) {
            return new HashMap<>(0);
        }
        CustomField customField = baseCustomFieldService.getCustomFieldByName(projectId, SystemCustomField.ISSUE_DEGREE);
        CustomFieldIssuesExample example = new CustomFieldIssuesExample();
        example.createCriteria().andFieldIdEqualTo(customField.getId()).andResourceIdIn(issueIds);
        List<CustomFieldIssues> customFieldIssues = customFieldIssuesMapper.selectByExample(example);
        Map<String, String> statusMap = customFieldIssues.stream().collect(Collectors.toMap(CustomFieldIssues::getResourceId, CustomFieldIssues::getValue));
        return statusMap;
    }
}
