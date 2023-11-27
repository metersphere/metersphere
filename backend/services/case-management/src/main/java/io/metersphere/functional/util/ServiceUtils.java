package io.metersphere.functional.util;

import io.metersphere.functional.constants.MoveTypeEnum;
import io.metersphere.functional.request.PosRequest;
import io.metersphere.sdk.exception.MSException;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ServiceUtils {

    public static final int POS_STEP = 5000;

    public static <T> void updateOrderField(PosRequest request, Class<T> clazz,
                                            Function<String, T> selectByPrimaryKeyFunc,
                                            BiFunction<String, Long, Long> getPreOrderFunc,
                                            BiFunction<String, Long, Long> getLastOrderFunc,
                                            Consumer<T> updateByPrimaryKeySelectiveFuc) {
        Long pos;
        Long lastOrPrePos;
        try {
            Method getPos = clazz.getMethod("getPos");
            Method setId = clazz.getMethod("setId", String.class);
            Method setPos = clazz.getMethod("setPos", Long.class);

            // 获取移动的参考对象
            T target = selectByPrimaryKeyFunc.apply(request.getTargetId());

            if (target == null) {
                // 如果参考对象被删除，则不处理
                return;
            }

            Long targetOrder = (Long) getPos.invoke(target);

            if (request.getMoveMode().equals(MoveTypeEnum.AFTER.name())) {
                // 追加到参考对象的之后
                pos = targetOrder - ServiceUtils.POS_STEP;
                // ，因为是降序排，则查找比目标 order 小的一个order
                lastOrPrePos = getPreOrderFunc.apply(request.getProjectId(), targetOrder);
            } else {
                // 追加到前面
                pos = targetOrder + ServiceUtils.POS_STEP;
                // 因为是降序排，则查找比目标 order 更大的一个order
                lastOrPrePos = getLastOrderFunc.apply(request.getProjectId(), targetOrder);
            }
            if (lastOrPrePos != null) {
                // 如果不是第一个或最后一个则取中间值
                pos = (targetOrder + lastOrPrePos) / 2;
            }

            // 更新order值
            T updateObj = (T) clazz.getDeclaredConstructor().newInstance();
            setId.invoke(updateObj, request.getMoveId());
            setPos.invoke(updateObj, pos);
            updateByPrimaryKeySelectiveFuc.accept(updateObj);
        } catch (Exception e) {
            throw new MSException("更新 order 字段失败");
        }
    }
}
