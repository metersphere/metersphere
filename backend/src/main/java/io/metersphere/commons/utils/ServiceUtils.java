package io.metersphere.commons.utils;

import io.metersphere.base.domain.User;
import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.service.UserService;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServiceUtils {

    public static List<OrderRequest> getDefaultOrder(List<OrderRequest> orders) {
        if (orders == null || orders.size() < 1) {
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setName("update_time");
            orderRequest.setType("desc");
            orders = new ArrayList<>();
            orders.add(orderRequest);
            return orders;
        }
        return orders;
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
        UserService userService = CommonBeanFactory.getBean(UserService.class);
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
}
