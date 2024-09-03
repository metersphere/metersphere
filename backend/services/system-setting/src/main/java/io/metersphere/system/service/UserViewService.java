package io.metersphere.system.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.constants.InternalUserView;
import io.metersphere.system.constants.UserViewConditionValueType;
import io.metersphere.system.constants.UserViewType;
import io.metersphere.system.domain.UserView;
import io.metersphere.system.domain.UserViewCondition;
import io.metersphere.system.domain.UserViewConditionExample;
import io.metersphere.system.domain.UserViewExample;
import io.metersphere.system.dto.UserViewDTO;
import io.metersphere.system.dto.UserViewListDTO;
import io.metersphere.system.dto.UserViewListGroupedDTO;
import io.metersphere.system.dto.request.UserViewAddRequest;
import io.metersphere.system.dto.request.UserViewUpdateRequest;
import io.metersphere.system.dto.sdk.CombineCondition;
import io.metersphere.system.mapper.ExtUserViewMapper;
import io.metersphere.system.mapper.UserViewConditionMapper;
import io.metersphere.system.mapper.UserViewMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-30  14:30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserViewService {
    @Resource
    private UserViewMapper userViewMapper;
    @Resource
    private UserViewConditionMapper userViewConditionMapper;
    @Resource
    private ExtUserViewMapper extUserViewMapper;

    public static final Long POS_STEP = 5000L;

    public List<UserViewListDTO> list(String scopeId, UserViewType viewType, String userId) {
        UserViewListGroupedDTO userViews = groupedList(scopeId, viewType, userId);
        userViews.getCustomViews().addAll(userViews.getInternalViews());
        return userViews.getCustomViews();
    }

    public UserViewDTO get(String id, UserViewType viewType, String userId) {
        // 先尝试获取系统内置视图
        InternalUserView[] values = InternalUserView.values();
        for (InternalUserView value : values) {
            if (StringUtils.equalsIgnoreCase(id, value.name())) {
                UserViewDTO userView = value.getUserView();
                UserViewDTO userViewDTO = BeanUtils.copyBean(new UserViewDTO(), userView);
                userViewDTO.setName(translateInternalView(userView.getName()));
                userViewDTO.setViewType(viewType.name());
                userViewDTO.setUserId(userId);
                return userViewDTO;
            }
        }

        // 查询用户自定义视图
        UserView userView = userViewMapper.selectByPrimaryKey(id);
        checkOwner(userId, userView);
        UserViewDTO userViewDTO = BeanUtils.copyBean(new UserViewDTO(), userView);
        List<CombineCondition> conditions = getUserViewConditionsByViewId(id).stream().map(condition -> {
            CombineCondition combineCondition = BeanUtils.copyBean(new CombineCondition(), condition);
            Object value = getConditionValueByType(condition.getValueType(), condition.getValue());
            combineCondition.setValue(value);
            return combineCondition;
        }).toList();
        userViewDTO.setConditions(conditions);
        return userViewDTO;
    }

    private List<UserViewCondition> getUserViewConditionsByViewId(String userViewId) {
        UserViewConditionExample example = new UserViewConditionExample();
        example.createCriteria()
                .andUserViewIdEqualTo(userViewId);
        List<UserViewCondition> conditions = userViewConditionMapper.selectByExample(example);
        return conditions;
    }

    public UserViewDTO add(UserViewAddRequest request, String viewType, String userId) {
        Long nextPos = getNextPos(request.getScopeId(), userId, viewType);
        UserView userView = BeanUtils.copyBean(new UserView(), request);
        userView.setCreateTime(System.currentTimeMillis());
        userView.setUpdateTime(System.currentTimeMillis());
        userView.setViewType(viewType);
        userView.setUserId(userId);
        userView.setId(IDGenerator.nextStr());
        userView.setPos(nextPos);
        userViewMapper.insertSelective(userView);

        addUserViewConditions(request.getConditions(), userView);

        UserViewDTO userViewDTO = BeanUtils.copyBean(new UserViewDTO(), request);
        userViewDTO.setId(userView.getId());
        userViewDTO.setCreateTime(userView.getCreateTime());
        userViewDTO.setUpdateTime(userView.getUpdateTime());
        userViewDTO.setUserId(userId);
        userViewDTO.setViewType(viewType);
        userViewDTO.setScopeId(request.getScopeId());
        userViewDTO.setPos(nextPos);
        return userViewDTO;
    }

    public Long getNextPos(String scopeId, String userId, String viewType) {
        Long pos = extUserViewMapper.getLastPos(scopeId, userId, viewType, null);
        return (pos == null ? 0 : pos) + POS_STEP;
    }

    public void delete(String id, String userId) {
        UserView originUserView = userViewMapper.selectByPrimaryKey(id);
        // 校验权限，只能修改自己的视图
        checkOwner(userId, originUserView);
        userViewMapper.deleteByPrimaryKey(id);
    }

    private void deleteConditionsByViewId(String userViewId) {
        UserViewConditionExample example = new UserViewConditionExample();
        example.createCriteria()
                .andUserViewIdEqualTo(userViewId);
        userViewConditionMapper.deleteByExample(example);
    }

    private void addUserViewConditions(List<CombineCondition> conditions, UserView userView) {
        List<UserViewCondition> insertConditions = conditions.stream().map(condition -> {
            UserViewCondition userViewCondition = BeanUtils.copyBean(new UserViewCondition(), condition);
            userViewCondition.setId(IDGenerator.nextStr());
            userViewCondition.setOperator(condition.getOperator());
            Object value = condition.getValue();
            String conditionValueType = getConditionValueType(value);
            userViewCondition.setValueType(conditionValueType);
            if (condition.getValue() != null) {
                if (value instanceof List<?>) {
                    userViewCondition.setValue(JSON.toJSONString(value));
                } else {
                    userViewCondition.setValue(condition.getValue().toString());
                }
            }
            userViewCondition.setUserViewId(userView.getId());
            return userViewCondition;
        }).toList();

        userViewConditionMapper.batchInsert(insertConditions);
    }

    private String getConditionValueType(Object value) {
        if (value instanceof List<?>) {
            return UserViewConditionValueType.ARRAY.name();
        } else if (value instanceof Integer || value instanceof Long) {
            return UserViewConditionValueType.INT.name();
        } else if (value instanceof Float || value instanceof Double) {
            return UserViewConditionValueType.FLOAT.name();
        } else {
            return UserViewConditionValueType.STRING.name();
        }
    }

    private Object getConditionValueByType(String conditionValueTypeStr, String valueStr) {
        UserViewConditionValueType conditionValueType = EnumValidator.validateEnum(UserViewConditionValueType.class, conditionValueTypeStr);
        if (StringUtils.isBlank(valueStr)) {
            return null;
        }
        switch (conditionValueType) {
            case ARRAY:
                return JSON.parseObject(valueStr);
            case INT:
                return Long.valueOf(valueStr);
            case FLOAT:
                return Double.valueOf(valueStr);
            default:
                return valueStr;
        }
    }

    public UserViewDTO update(UserViewUpdateRequest request, String viewType, String userId) {
        UserView originUserView = userViewMapper.selectByPrimaryKey(request.getId());
        // 校验权限，只能修改自己的视图
        checkOwner(userId, originUserView);

        UserView userView = BeanUtils.copyBean(new UserView(), request);
        userView.setViewType(viewType);
        userView.setUpdateTime(System.currentTimeMillis());
        userViewMapper.updateByPrimaryKeySelective(userView);

        // 先删除
        deleteConditionsByViewId(originUserView.getId());
        // 再新增
        addUserViewConditions(request.getConditions(), userView);

        UserViewDTO userViewDTO = BeanUtils.copyBean(new UserViewDTO(), request);
        userViewDTO.setId(originUserView.getId());
        userViewDTO.setCreateTime(originUserView.getCreateTime());
        userViewDTO.setUpdateTime(userView.getUpdateTime());
        userViewDTO.setUserId(userId);
        userViewDTO.setViewType(viewType);
        userViewDTO.setScopeId(originUserView.getScopeId());
        userViewDTO.setPos(originUserView.getPos());
        return userViewDTO;
    }

    /**
     * 校验权限，只能修改自己的视图
     *
     * @param userId
     * @param originView
     */
    private void checkOwner(String userId, UserView originView) {
        if (!StringUtils.equals(userId, originView.getUserId())) {
            throw new MSException(Translator.get("check_owner_case"));
        }
    }

    public UserViewListGroupedDTO groupedList(String scopeId, UserViewType viewType, String userId) {
        // 查询系统内置视图
        List<UserViewListDTO> internalViews = viewType.getInternalViews().stream().map(userViewEnum -> {
            UserViewDTO userView = userViewEnum.getUserView();
            UserViewListDTO userViewListDTO = BeanUtils.copyBean(new UserViewListDTO(), userView);
            userViewListDTO.setName(translateInternalView(userView.getName()));
            userViewListDTO.setViewType(viewType.name());
            userViewListDTO.setScopeId(scopeId);
            userViewListDTO.setUserId(userId);
            return userViewListDTO;
        }).collect(Collectors.toList());

        // 查询用户自定义视图
        UserViewExample example = new UserViewExample();
        example.createCriteria()
                .andUserIdEqualTo(userId)
                .andScopeIdEqualTo(scopeId)
                .andViewTypeEqualTo(viewType.name());
        List<UserViewListDTO> customUserViews = userViewMapper.selectByExample(example).stream()
                .sorted(Comparator.comparing(UserView::getPos))
                .map(userView -> BeanUtils.copyBean(new UserViewListDTO(), userView))
                .collect(Collectors.toList());

        UserViewListGroupedDTO groupedDTO = new UserViewListGroupedDTO();
        groupedDTO.setInternalViews(internalViews);
        groupedDTO.setCustomViews(customUserViews);
        return groupedDTO;
    }

    /**
     * 翻译内置视图
     *
     * @param viewName
     * @return
     */
    public String translateInternalView(String viewName) {
        return Translator.get("user_view." + viewName);
    }
}
