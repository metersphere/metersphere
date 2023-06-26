package io.metersphere.service;

import io.metersphere.base.mapper.ext.BaseCustomFieldResourceMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SubListUtil;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.dto.CustomFieldResourceDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomFieldResourceService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    BaseCustomFieldResourceMapper baseCustomFieldResourceMapper;

    @Resource
    SystemParameterService systemParameterService;

    protected void addFields(String tableName, String resourceId, List<CustomFieldResourceDTO> addFields) {
        if (CollectionUtils.isNotEmpty(addFields)) {
            this.checkInit();
            addFields.forEach(field -> {
                createOrUpdateFields(tableName, resourceId, field);
            });
        }
    }

    protected void editFields(String tableName, String resourceId, List<CustomFieldResourceDTO> editFields) {
        if (CollectionUtils.isNotEmpty(editFields)) {
            this.checkInit();
            editFields.forEach(field -> {
                createOrUpdateFields(tableName, resourceId, field);
            });
        }
    }

    protected void batchEditFields(String tableName, String resourceId, List<CustomFieldResourceDTO> fields) {
        if (CollectionUtils.isNotEmpty(fields)) {
            this.checkInit();
            SqlSession sqlSession = ServiceUtils.getBatchSqlSession();
            BaseCustomFieldResourceMapper batchMapper = sqlSession.getMapper(BaseCustomFieldResourceMapper.class);
            for (CustomFieldResourceDTO field : fields) {
                long count = baseCustomFieldResourceMapper.countFieldResource(tableName, resourceId, field.getFieldId());
                if (count > 0) {
                    batchMapper.updateByPrimaryKeySelective(tableName, field);
                } else {
                    batchMapper.insert(tableName, field);
                }
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    private void createOrUpdateFields(String tableName, String resourceId, CustomFieldResourceDTO field) {
        long count = baseCustomFieldResourceMapper.countFieldResource(tableName, resourceId, field.getFieldId());
        field.setResourceId(resourceId);
        if (count > 0) {
            baseCustomFieldResourceMapper.updateByPrimaryKeySelective(tableName, field);
        } else {
            baseCustomFieldResourceMapper.insert(tableName, field);
        }
    }

    protected int updateByPrimaryKeySelective(String tableName, CustomFieldResourceDTO field) {
        return baseCustomFieldResourceMapper.updateByPrimaryKeySelective(tableName, field);
    }

    protected int insert(String tableName, CustomFieldResourceDTO field) {
        return baseCustomFieldResourceMapper.insert(tableName, field);
    }

    protected List<CustomFieldResourceDTO> getByResourceId(String tableName, String resourceId) {
        return baseCustomFieldResourceMapper.getByResourceId(tableName, resourceId);
    }

    public void batchUpdateByResourceIds(String tableName, List<String> resourceIds, CustomFieldResourceDTO customField) {
        if (CollectionUtils.isEmpty(resourceIds)) {
             return;
        }
        SubListUtil.dealForSubList(resourceIds, 5000, (subIds) ->
                baseCustomFieldResourceMapper.batchUpdateByResourceIds(tableName, subIds, customField));
    }

    public void batchInsertIfNotExists(String tableName, List<String> resourceIds, CustomFieldResourceDTO customField) {
        ServiceUtils.batchOperate(resourceIds, 5000, BaseCustomFieldResourceMapper.class, (resourceId, batchMapper) -> {
            customField.setResourceId((String) resourceId);
            ((BaseCustomFieldResourceMapper) batchMapper).batchInsertIfNotExists(tableName, customField);
        });
    }

    protected List<CustomFieldResourceDTO>  getByResourceIds(String tableName, List<String> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return new ArrayList<>();
        }
        return baseCustomFieldResourceMapper.getByResourceIds(tableName, resourceIds);
    }

    protected List<CustomFieldResourceDTO>  getByResourceIdsForList(String tableName, List<String> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return new ArrayList<>();
        }
        return baseCustomFieldResourceMapper.getByResourceIdsForList(tableName, resourceIds);
    }

    protected Map<String, List<CustomFieldDao>> getMapByResourceIds(String tableName, List<String> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return new HashMap<>();
        }
        List<CustomFieldResourceDTO> CustomFieldResourceDTOs = getByResourceIds(tableName, resourceIds);
        return getFieldMap(CustomFieldResourceDTOs);
    }

    protected Map<String, List<CustomFieldDao>> getMapByResourceIdsForList(String tableName, List<String> resourceIds) {
        if (CollectionUtils.isEmpty(resourceIds)) {
            return new HashMap<>();
        }
        List<CustomFieldResourceDTO> CustomFieldResourceDTOs = getByResourceIdsForList(tableName, resourceIds);
        return getFieldMap(CustomFieldResourceDTOs);
    }

    @NotNull
    private static Map<String, List<CustomFieldDao>> getFieldMap(List<CustomFieldResourceDTO> CustomFieldResourceDTOs) {
        Map<String, List<CustomFieldDao>> fieldMap = new HashMap<>();
        CustomFieldResourceDTOs.forEach(i -> {
            List<CustomFieldDao> fields = fieldMap.get(i.getResourceId());
            if (fields == null) {
                fields = new ArrayList<>();
            }
            CustomFieldDao customFieldDao = new CustomFieldDao();
            customFieldDao.setId(i.getFieldId());
            customFieldDao.setValue(i.getValue());
            customFieldDao.setTextValue(i.getTextValue());
            fields.add(customFieldDao);
            fieldMap.put(i.getResourceId(), fields);
        });
        return fieldMap;
    }

    protected void deleteByResourceId(String tableName, String resourceId) {
        baseCustomFieldResourceMapper.deleteByResourceId(tableName, resourceId);
    }

    protected void deleteByResourceIds(String tableName, List<String> resourceIds) {
        baseCustomFieldResourceMapper.deleteByResourceIds(tableName, resourceIds);
    }

    protected void checkInit() {
        String value = systemParameterService.getValue("init.custom.field.resource");
        if (StringUtils.isNotBlank(value) && value.equals("over")) {
            return;
        }
        MSException.throwException("数据升级处理中，请稍后重试！");
    }
}
