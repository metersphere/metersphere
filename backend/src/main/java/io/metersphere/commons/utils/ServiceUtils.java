package io.metersphere.commons.utils;

import io.metersphere.controller.request.OrderRequest;

import java.util.ArrayList;
import java.util.List;

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
}
