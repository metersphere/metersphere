package io.metersphere.commons.utils;

import io.metersphere.api.dto.definition.BatchDataCopyRequest;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 批量处理工具
 */
public class BatchProcessingUtil {

    private static final int BATCH_PROCESS_QUANTITY = 1000;

    public static void batchProcessingByDataCopy(BatchDataCopyRequest paramRequest, Consumer<BatchDataCopyRequest> func) {
        List<String> paramList = paramRequest.getIds();
        if (CollectionUtils.isNotEmpty(paramList)) {
            BatchDataCopyRequest queryRequest = new BatchDataCopyRequest();
            BeanUtils.copyBean(queryRequest, paramRequest);
            int unProcessingCount = paramList.size();
            while (paramList.size() > BATCH_PROCESS_QUANTITY) {
                List<String> processingList = new ArrayList<>();
                for (int i = 0; i < BATCH_PROCESS_QUANTITY; i++) {
                    processingList.add(paramList.get(i));
                }
                //函数处理
                queryRequest.setIds(processingList);
                func.accept(queryRequest);

                paramList.removeAll(processingList);
                if (paramList.size() == unProcessingCount) {
                    //如果剩余数量没有发生变化，则跳出循环。防止出现死循环的情况
                    break;
                } else {
                    unProcessingCount = paramList.size();
                }
            }

            if (CollectionUtils.isNotEmpty(paramList)) {
                //剩余待处理数据进行处理
                queryRequest.setIds(paramList);
                func.accept(queryRequest);
            }
        }
    }
}
