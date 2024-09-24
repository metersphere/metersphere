package io.metersphere.system.utils;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import io.metersphere.system.dto.sdk.enums.MoveTypeEnum;
import io.metersphere.system.dto.sdk.request.PosRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;

public class ServiceUtils {

    //用于排序的pos
    public static final int POS_STEP = 4096;

    /**
     * 保存资源名称，在处理 NOT_FOUND 异常时，拼接资源名称
     */
    private static final ThreadLocal<String> resourceName = new ThreadLocal<>();

    /**
     * 校验资源是否存在，不存在则抛出 NOT_FOUND 异常
     * @param resource 资源
     * @param name 资源名称，用户拼接异常信息
     * @return
     * @param <T>
     */
    public static <T> T checkResourceExist(T resource, String name) {
        if (resource == null) {
            resourceName.set(name);
            throw new MSException(NOT_FOUND);
        }
        return resource;
    }

    public static String getResourceName() {
        return resourceName.get();
    }

    public static void clearResourceName() {
        resourceName.remove();
    }

    public static <T> void updatePosField(PosRequest request, Class<T> clazz,
                                          Function<String, T> selectByPrimaryKeyFunc,
                                          BiFunction<String, Long, Long> getPrePosFunc,
                                          BiFunction<String, Long, Long> getLastPosFunc,
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

            Long targetPos = (Long) getPos.invoke(target);

            if (request.getMoveMode().equals(MoveTypeEnum.AFTER.name())) {
                // 追加到参考对象的之后
                pos = targetPos - POS_STEP;
                // ，因为是降序排，则查找比目标 order 小的一个order
                lastOrPrePos = getPrePosFunc.apply(request.getProjectId(), targetPos);
            } else {
                // 追加到前面
                pos = targetPos + POS_STEP;
                // 因为是降序排，则查找比目标 order 更大的一个order
                lastOrPrePos = getLastPosFunc.apply(request.getProjectId(), targetPos);
            }
            if (lastOrPrePos != null) {
                // 如果不是第一个或最后一个则取中间值
                pos = (targetPos + lastOrPrePos) / 2;
            }

            // 更新order值
            T updateObj = (T) clazz.getDeclaredConstructor().newInstance();
            setId.invoke(updateObj, request.getMoveId());
            setPos.invoke(updateObj, pos);
            updateByPrimaryKeySelectiveFuc.accept(updateObj);
        } catch (Exception e) {
            throw new MSException("更新 pos 字段失败");
        }
    }

    /**
     * 校验对象的参数校验注解
     * @param param
     */
    public static <T> void validateParam(T param) {
        Validator validator = (Validator) CommonBeanFactory.getBean("validator");
        Set<ConstraintViolation<T>> errors = validator.validate(param);
        if (CollectionUtils.isEmpty(errors)) {
            return;
        }
        StringBuilder tips = new StringBuilder();
        for (ConstraintViolation<T> error : errors) {
            String fieldName = error.getPropertyPath().toString();
            String message = error.getMessage();
            tips.append(fieldName + message).append("; ");
        }
        throw new MSException(MsHttpResultCode.VALIDATE_FAILED, tips.toString());
    }

    public static String compressName(String name, int maxSize) {
        if (StringUtils.isBlank(name)) {
            return StringUtils.EMPTY;
        }
        String newName = name;
        // 限制名称长度 （数据库里最大的长度是255，这里判断超过250时截取到200附近）
        if (newName.length() > maxSize) {
            newName = newName.substring(0, maxSize - 3) + "...";
        }
        return newName;
    }


    /**
     * 解析Tag，只保留默认长度
     *
     * @param tags 标签集合
     */
    private static final int MAX_TAG_SIZE = 10;

    public static List<String> parseTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return tags;
        } else if (tags.size() > MAX_TAG_SIZE) {
            List<String> returnTags = new ArrayList<>(tags.stream().distinct().toList());
            return returnTags.subList(0, MAX_TAG_SIZE);
        } else {
            return new ArrayList<>(tags.stream().distinct().toList());
        }
    }
}
