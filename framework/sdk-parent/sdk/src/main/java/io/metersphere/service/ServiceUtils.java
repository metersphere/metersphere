package io.metersphere.service;

import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.User;
import io.metersphere.base.domain.Workspace;
import io.metersphere.commons.constants.CustomFieldType;
import io.metersphere.commons.constants.OperatorTypeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.ResetOrderRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class ServiceUtils {

    public static final int ORDER_STEP = 5000;

    public static List<OrderRequest> getDefaultOrder(List<OrderRequest> orders) {
        return getDefaultOrder(null, orders);
    }

    public static List<OrderRequest> getDefaultOrder(List<OrderRequest> orders, String field) {
        return getDefaultOrder(null, field, orders);
    }

    public static List<OrderRequest> getDefaultSortOrder(List<OrderRequest> orders) {
        return getDefaultOrderByField(null, orders, "order");
    }

    public static List<OrderRequest> getDefaultSortOrder(String prefix, List<OrderRequest> orders) {
        return getDefaultOrderByField(prefix, orders, "order");
    }

    public static List<OrderRequest> getDefaultOrder(String prefix, List<OrderRequest> orders) {
        return getDefaultOrderByField(prefix, orders, "update_time");
    }

    public static List<OrderRequest> getDefaultOrder(String prefix, String field, List<OrderRequest> orders) {
        return getDefaultOrderByField(prefix, orders, field);
    }

    /**
     * 批量操作
     */
    public static void batchOperate(List data, int batchSize, Class mapperClazz, BiConsumer operateFunc) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        SqlSessionFactory sqlSessionFactory = CommonBeanFactory.getBean(SqlSessionFactory.class);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        Object mapper = sqlSession.getMapper(mapperClazz);
        for (int i = 0; i < data.size(); i++) {
            operateFunc.accept(data.get(i), mapper);
            if (i % batchSize == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    /**
     * 根据当前项目设置是否启用自定义ID
     * 设置按照哪个字段排序
     *
     * @param isCustomNum
     * @param orders
     * @return
     */
    public static List<OrderRequest> replaceCustomNumOrder(Boolean isCustomNum, List<OrderRequest> orders) {
        orders.forEach(item -> {
            if (isCustomNum && StringUtils.equals(item.getName(), "num")) {
                item.setName("custom_num");
            } else if (StringUtils.equals(item.getName(), "custom_num")) {
                item.setName("num");
            }
        });
        return orders;
    }

    private static List<OrderRequest> getDefaultOrderByField(String prefix, List<OrderRequest> orders, String field) {
        if (orders == null || orders.size() < 1) {
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setName(field);
            orderRequest.setType("desc");
            if (StringUtils.isNotBlank(prefix)) {
                orderRequest.setPrefix(prefix);
            }
            orders = new ArrayList<>();
            orders.add(orderRequest);
            return orders;
        }
        return orders;
    }

    public static List<OrderRequest> getDefaultOrderByField(List<OrderRequest> orders, String field) {
        return getDefaultOrderByField(null, orders, field);
    }

    /**
     * 获取前端全选的id列表
     *
     * @param queryRequest 查询条件
     * @param func         查询id列表的数据库查询
     * @return
     */
    public static <T> void getSelectAllIds(T batchRequest, BaseQueryRequest queryRequest, Function<BaseQueryRequest, List<String>> func) {
        if (queryRequest != null && queryRequest.isSelectAll()) {
            List<String> ids = func.apply(queryRequest);
            if (!ids.isEmpty()) {
                ids = ids.stream()
                        .filter(id -> !queryRequest.getUnSelectIds().contains(id))
                        .collect(Collectors.toList());
            }
            queryRequest.setIds(ids);
            try {
                Method setIds = batchRequest.getClass().getDeclaredMethod("setIds", List.class);
                setIds.invoke(batchRequest, ids);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException("请求没有setIds方法");
            }
        }
    }

    public static Map<String, User> getUserMap(List<String> userIds) {
        BaseUserService userService = CommonBeanFactory.getBean(BaseUserService.class);
        if (!CollectionUtils.isEmpty(userIds)) {
            Map<String, User> userMap = userService.queryNameByIds(userIds);
            return userMap;
        }
        return new HashMap<>();
    }

    public static Map<String, String> getUserNameMap(List<String> userIds) {
        Map<String, User> userMap = getUserMap(userIds);
        HashMap<String, String> nameMap = new HashMap<>();
        userMap.forEach((k, v) -> {
            nameMap.put(k, v.getName());
        });
        return nameMap;
    }

    public static Map<String, String> getWorkspaceNameByProjectIds(List<String> projectIds) {
        BaseProjectService projectService = CommonBeanFactory.getBean(BaseProjectService.class);
        HashMap<String, String> nameMap = new HashMap<>();

        if (!CollectionUtils.isEmpty(projectIds)) {
            Map<String, Workspace> workspaceMap = projectService.getWorkspaceNameByProjectIds(projectIds);
            workspaceMap.forEach((k, v) -> {
                nameMap.put(k, v.getName());
            });
            return nameMap;
        }

        return nameMap;
    }

    public static Map<String, Project> getProjectMap(List<String> ids) {
        BaseProjectService projectService = CommonBeanFactory.getBean(BaseProjectService.class);
        if (!CollectionUtils.isEmpty(ids)) {
            Map<String, Project> projectMap = projectService.queryNameByIds(ids);
            return projectMap;
        }
        return new HashMap<>();
    }

    public static Map<String, String> getProjectNameMap(List<String> ids) {
        Map<String, Project> projectMap = getProjectMap(ids);
        HashMap<String, String> nameMap = new HashMap<>();
        projectMap.forEach((k, v) -> {
            nameMap.put(k, v.getName());
        });
        return nameMap;
    }

    /**
     * 初始化 order 列
     *
     * @param clazz
     * @param mapClazz
     * @param selectProjectIdsFunc
     * @param getIdsOrderByUpdateTimeFunc
     * @param <T>
     * @param <M>
     */
    public static <T, M> void initOrderField(Class<T> clazz, Class<M> mapClazz,
                                             Supplier<List<String>> selectProjectIdsFunc,
                                             Function<String, List<String>> getIdsOrderByUpdateTimeFunc) {

        try {

            SqlSessionFactory sqlSessionFactory = CommonBeanFactory.getBean(SqlSessionFactory.class);
            Method setId = clazz.getMethod("setId", String.class);
            Method setOrder = clazz.getMethod("setOrder", Long.class);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            Object mapper = sqlSession.getMapper(mapClazz);

            List<String> projectIds = selectProjectIdsFunc.get();
            for (String projectId : projectIds) {
                Long order = 0L;
                List<String> ids = getIdsOrderByUpdateTimeFunc.apply(projectId);
                for (String id : ids) {
                    T item = clazz.getDeclaredConstructor().newInstance();
                    setId.invoke(item, id);
                    setOrder.invoke(item, order);
                    order += ServiceUtils.ORDER_STEP;
                    Method updateByPrimaryKeySelectiveFunc = mapper.getClass().getMethod("updateByPrimaryKeySelective", clazz);
                    updateByPrimaryKeySelectiveFunc.invoke(mapper, item);
                }
                sqlSession.flushStatements();
            }
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("初始化 order 字段失败");
        }
    }

    /**
     * @param request
     * @param clazz
     * @param selectByPrimaryKeyFunc
     * @param getPreOrderFunc
     * @param getLastOrderFunc
     * @param updateByPrimaryKeySelectiveFuc
     * @param <T>
     */
    public static <T> void updateOrderField(ResetOrderRequest request, Class<T> clazz,
                                            Function<String, T> selectByPrimaryKeyFunc,
                                            BiFunction<String, Long, Long> getPreOrderFunc,
                                            BiFunction<String, Long, Long> getLastOrderFunc,
                                            Consumer<T> updateByPrimaryKeySelectiveFuc) {
        Long order;
        Long lastOrPreOrder;
        try {
            Method getOrder = clazz.getMethod("getOrder");
            Method setId = clazz.getMethod("setId", String.class);
            Method setOrder = clazz.getMethod("setOrder", Long.class);

            // 获取移动的参考对象
            T target = selectByPrimaryKeyFunc.apply(request.getTargetId());

            if (target == null) {
                // 如果参考对象被删除，则不处理
                return;
            }

            Long targetOrder = (Long) getOrder.invoke(target);

            if (request.getMoveMode().equals(ResetOrderRequest.MoveMode.AFTER.name())) {
                // 追加到参考对象的之后
                order = targetOrder - ServiceUtils.ORDER_STEP;
                // ，因为是降序排，则查找比目标 order 小的一个order
                lastOrPreOrder = getPreOrderFunc.apply(request.getGroupId(), targetOrder);
            } else {
                // 追加到前面
                order = targetOrder + ServiceUtils.ORDER_STEP;
                // 因为是降序排，则查找比目标 order 更大的一个order
                lastOrPreOrder = getLastOrderFunc.apply(request.getGroupId(), targetOrder);
            }
            if (lastOrPreOrder != null) {
                // 如果不是第一个或最后一个则取中间值
                order = (targetOrder + lastOrPreOrder) / 2;
            }

            // 更新order值
            T updateObj = (T) clazz.getDeclaredConstructor().newInstance();
            setId.invoke(updateObj, request.getMoveId());
            setOrder.invoke(updateObj, order);
            updateByPrimaryKeySelectiveFuc.accept(updateObj);
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("更新 order 字段失败");
        }
    }

    /**
     * 创建时获取下一个 order 值
     *
     * @param groupId
     * @param getLastOrderFunc
     * @return
     */
    public static Long getNextOrder(String groupId, BiFunction<String, Long, Long> getLastOrderFunc) {
        Long lastOrder = getLastOrderFunc.apply(groupId, null);
        return (lastOrder == null ? 0 : lastOrder) + ServiceUtils.ORDER_STEP;
    }

    public static <T> int getNextNum(String projectId, Class<T> clazz, Function<String, T> getNextNumFunc) {
        T data = getNextNumFunc.apply(projectId);
        try {
            Method getNum = clazz.getMethod("getNum");
            if (data == null || getNum.invoke(data) == null) {
                return 100001;
            } else {
                return Optional.ofNullable((Integer) getNum.invoke(data) + 1).orElse(100001);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return 100001;
    }

    public static SqlSession getBatchSqlSession() {
        SqlSessionFactory sqlSessionFactory = CommonBeanFactory.getBean(SqlSessionFactory.class);
        return sqlSessionFactory.openSession(ExecutorType.BATCH);
    }

    public static String getCopyName(String name) {
        return "copy_" + name + "_" + UUID.randomUUID().toString().substring(0, 4);
    }

    public static void buildVersionInfo(List<? extends Object> list) {
        // todo
//        ProjectVersionService projectVersionService = CommonBeanFactory.getBean(ProjectVersionService.class);
//        if (projectVersionService == null) {
//            return;
//        }
//
//        List<String> versionIds = list.stream()
//                .map(i -> {
//                    Class<?> clazz = i.getClass();
//                    try {
//                        Method getVersionId = clazz.getMethod("getVersionId");
//                        return getVersionId.invoke(i).toString();
//                    } catch (Exception e) {
//                        LogUtil.error(e);
//                        return i.toString();
//                    }
//                })
//                .distinct()
//                .collect(Collectors.toList());
//
//        Map<String, String> versionNameMap = projectVersionService.getProjectVersionByIds(versionIds).
//                stream()
//                .collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));
//
//        list.forEach(i -> {
//            Class<?> clazz = i.getClass();
//            try {
//                Method setVersionName = clazz.getMethod("setVersionName", String.class);
//                Method getVersionId = clazz.getMethod("getVersionId");
//                Object versionId = getVersionId.invoke(i);
//                setVersionName.invoke(i, versionNameMap.get(versionId));
//            } catch (Exception e) {
//                LogUtil.error(e);
//            }
//        });
    }

    @SuppressWarnings("unchecked")
    public static void setBaseQueryRequestCustomMultipleFields(BaseQueryRequest request) {
        // filter中自定义多选字段
        if (MapUtils.isNotEmpty(request.getFilters())) {
            request.getFilters().entrySet().forEach(entry -> {
                if (entry.getKey().startsWith("custom_multiple") && CollectionUtils.isNotEmpty(entry.getValue())) {
                    List<String> customValues = JSON.parseArray(entry.getValue().get(0), String.class);
                    List<String> jsonValues = customValues.stream().map(item -> "[\"".concat(item).concat("\"]")).collect(Collectors.toList());
                    entry.setValue(jsonValues);
                }
            });
        }
        // 高级搜索中自定义多选字段
        if (MapUtils.isNotEmpty(request.getCombine()) && ObjectUtils.isNotEmpty((request.getCombine().get("customs")))) {
            List<Map<String, Object>> customs = (List<Map<String, Object>>) request.getCombine().get("customs");
            customs.forEach(custom -> {
                if(StringUtils.equalsIgnoreCase(custom.get("operator").toString(), "current user")){
                    String userId = SessionUtils.getUserId();
                    custom.put("value", userId);
                }
                if (StringUtils.equalsAny(custom.get("type").toString(), CustomFieldType.MULTIPLE_MEMBER.getValue(),
                        CustomFieldType.CHECKBOX.getValue(), CustomFieldType.MULTIPLE_SELECT.getValue())
                        && StringUtils.isNotEmpty(custom.get("value").toString())) {
                    List<String> customValues = JSON.parseArray(custom.get("value").toString(), String.class);
                    List<String> jsonValues = customValues.stream().map(item -> "JSON_CONTAINS(`value`, '[\"".concat(item).concat("\"]')")).collect(Collectors.toList());
                    custom.put("value", "(".concat(StringUtils.join(jsonValues, " OR ")).concat(")"));

                }
            });
        }
    }

    public static Boolean checkConfigEnable(Map config, String key, String subKey) {
        if (config == null || config.get(key) == null) {
            return true;
        } else {
            Map configItem = (Map) config.get(key);
            Boolean enable = (Boolean) configItem.get("enable");
            if (enable) {
                Map subConfig = (Map) ((Map) configItem.get("children")).get(subKey);
                if (subConfig == null) {
                    return true;
                }
                return (Boolean) subConfig.get("enable");
            } else {
                return false;
            }
        }
    }

    public static Boolean checkConfigEnable(Map config, String key) {
        if (config == null) {
            return true;
        } else {
            Map configItem = (Map) config.get(key);
            if (configItem == null) {
                return true;
            }
            return (Boolean) configItem.get("enable");
        }
    }

    private static List<String> getFieldListByMethod(List<?> list, String field) {
        return list.stream()
                .map(i -> {
                    Class<?> clazz = i.getClass();
                    try {
                        Method getField = clazz.getMethod(field);
                        return getField.invoke(i).toString();
                    } catch (Exception e) {
                        LogUtil.error(e);
                        return i.toString();
                    }
                })
                .distinct()
                .collect(Collectors.toList());
    }

    public static void buildProjectInfo(List<? extends Object> list) {
        List<String> projectIds = getFieldListByMethod(list, "getProjectId");

        Map<String, String> projectNameMap = getProjectNameMap(projectIds);

        list.forEach(i -> {
            Class<?> clazz = i.getClass();
            try {
                Method setProjectName = clazz.getMethod("setProjectName", String.class);
                Method getProjectId = clazz.getMethod("getProjectId");
                Object projectId = getProjectId.invoke(i);
                setProjectName.invoke(i, projectNameMap.get(projectId));
            } catch (Exception e) {
                LogUtil.error(e);
            }
        });
    }

    public static void buildCustomNumInfo(List<? extends Object> list) {
        List<String> projectIds = getFieldListByMethod(list, "getProjectId");
        BaseProjectApplicationService projectApplicationService = CommonBeanFactory.getBean(BaseProjectApplicationService.class);
        Map<String, String> customNumMap = projectApplicationService.getCustomNumMapByProjectIds(projectIds);
        list.forEach(i -> {
            buildCustomNumInfo(customNumMap, i);
        });
    }

    public static void buildCustomNumInfo(Object data) {
        try {
            Method getProjectId = data.getClass().getMethod("getProjectId");
            String projectId = getProjectId.invoke(data).toString();
            BaseProjectApplicationService projectApplicationService = CommonBeanFactory.getBean(BaseProjectApplicationService.class);
            Map<String, String> customNumMap = projectApplicationService.getCustomNumMapByProjectIds(Arrays.asList(projectId));
            buildCustomNumInfo(customNumMap, data);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public static void buildCombineTagsToSupportMultiple(BaseQueryRequest request) {
        if (request.getCombine() != null && request.getCombine().containsKey("tags")) {
            Map<String, Object> tagsMap = (Map<String, Object>) request.getCombine().get("tags");
            Object tagVal = tagsMap.get("value");
            if (tagVal != null) {
                List<String> tagList = Arrays.asList(tagVal.toString().split(StringUtils.SPACE));
                tagsMap.put("value", tagList);
            }
        }
    }

    private static void buildCustomNumInfo(Map<String, String> customNumMap, Object data) {
        Class<?> clazz = data.getClass();
        try {
            Method setIsCustomNum = clazz.getMethod("setCustomNum", String.class);
            Method getNum = clazz.getMethod("getNum");
            Method getProjectId = clazz.getMethod("getProjectId");
            Object projectId = getProjectId.invoke(data);
            String isCustomNum = customNumMap.get(projectId);
            if (isCustomNum == null) {
                setIsCustomNum.invoke(data, String.valueOf(getNum.invoke(data)));
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }
}
