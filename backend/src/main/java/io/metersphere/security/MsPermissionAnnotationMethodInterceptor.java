package io.metersphere.security;

import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.aop.PermissionAnnotationMethodInterceptor;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public class MsPermissionAnnotationMethodInterceptor extends PermissionAnnotationMethodInterceptor {


    public MsPermissionAnnotationMethodInterceptor(AnnotationResolver resolver) {
        super(resolver);
    }

    @Override
    public void assertAuthorized(MethodInvocation mi) throws AuthorizationException {
        String projectId = null;
        String workspaceId = null;
        Object[] arguments = mi.getArguments();
        if (ArrayUtils.isNotEmpty(arguments)) {
            Parameter[] parameters = mi.getMethod().getParameters();
            for (int i = 0; i < arguments.length; i++) {
                Object argument = arguments[i];
                if (argument instanceof String) {
                    if (StringUtils.equals(parameters[i].getName(), "projectId")) {
                        projectId = (String) argument;
                    }
                    if (StringUtils.equals(parameters[i].getName(), "workspaceId")) {
                        workspaceId = (String) argument;
                    }
                } else {
                    try {
                        if (StringUtils.isEmpty(projectId) && isExistField(argument, "projectId")) {
                            projectId = (String) MethodUtils.invokeMethod(argument, "getProjectId");
                        }
                        if (StringUtils.isEmpty(workspaceId) && isExistField(argument, "workspaceId")) {
                            workspaceId = (String) MethodUtils.invokeMethod(argument, "getWorkspaceId");
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        try {
            SessionUtils.setCurrentWorkspaceId(workspaceId);
            SessionUtils.setCurrentProjectId(projectId);
            super.assertAuthorized(mi);
        } finally {
            SessionUtils.clearCurrentWorkspaceId();
            SessionUtils.clearCurrentProjectId();
        }
    }


    public static boolean isExistField(Object obj, String fieldName) {
        if (obj == null || StringUtils.isEmpty(fieldName)) {
            return false;
        }
        //获取这个类的所有属性
        Field[] fields = FieldUtils.getAllFields(obj.getClass());
        boolean flag = false;
        //循环遍历所有的fields
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                flag = true;
                break;
            }
        }

        return flag;
    }
}
