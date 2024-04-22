package io.metersphere.api.utils;

import io.metersphere.api.dto.response.ApiScenarioBatchOperationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

//场景批量操作工具类
public class ApiScenarioBatchOperationUtils {
    private static int MAX_OPERATION_SIZE = 20;

    public static <T> ApiScenarioBatchOperationResponse executeWithBatchOperationResponse(List<T> totalList, Function<List<T>, ApiScenarioBatchOperationResponse> subFunc) {
        ApiScenarioBatchOperationResponse response = new ApiScenarioBatchOperationResponse();
        List<T> operationList = new ArrayList<>(totalList);
        while (operationList.size() > MAX_OPERATION_SIZE) {
            List<T> subList = operationList.subList(0, MAX_OPERATION_SIZE);
            response.merge(
                    subFunc.apply(subList));
            subList.clear();
            operationList.removeAll(subList);
        }
        if (!operationList.isEmpty()) {
            response.merge(
                    subFunc.apply(operationList));
        }
        return response;
    }
}
