package io.metersphere.commons.utils;

import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.OrderRequest;

import java.util.ArrayList;
import java.util.List;
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
     *  获取前端全选的id列表
     * @param queryRequest 查询条件
     * @param func 查询id列表的数据库查询
     * @return
     */
    public static void getSelectAllIds(BaseQueryRequest queryRequest, Function<BaseQueryRequest, List<String>> func) {
        if (queryRequest.isSelectAll()) {
            List<String> ids = func.apply(queryRequest);
            if (!ids.isEmpty()) {
                ids = ids.stream()
                        .filter(id -> !queryRequest.getUnSelectIds().contains(id))
                        .collect(Collectors.toList());
            }
            queryRequest.setIds(ids);
        }
    }
}
