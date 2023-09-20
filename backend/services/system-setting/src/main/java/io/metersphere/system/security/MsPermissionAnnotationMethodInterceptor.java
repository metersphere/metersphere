package io.metersphere.system.security;

import io.metersphere.project.domain.Project;
import io.metersphere.system.utils.SessionUtils;
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
        String organizationId = null;
        Object[] arguments = mi.getArguments();
        if (ArrayUtils.isNotEmpty(arguments)) {
            Parameter[] parameters = mi.getMethod().getParameters();
            for (int i = 0; i < arguments.length; i++) {
                Object argument = arguments[i];
                if (argument instanceof String) {
                    if (StringUtils.equals(parameters[i].getName(), "projectId")) {
                        projectId = (String) argument;
                    }
                    if (StringUtils.equals(parameters[i].getName(), "organizationId")) {
                        organizationId = (String) argument;
                    }
                } else {
                    try {
                        if (StringUtils.isEmpty(projectId) && isExistField(argument, "projectId")) {
                            projectId = (String) MethodUtils.invokeMethod(argument, "getProjectId");
                        }
                        if (StringUtils.equals(parameters[i].getName(), "project") && argument instanceof Project) {
                            projectId = ((Project) argument).getId();
                        }
                        if (StringUtils.isEmpty(organizationId) && isExistField(argument, "organizationId")) {
                            organizationId = (String) MethodUtils.invokeMethod(argument, "getOrganizationId");
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        try {
            SessionUtils.setCurrentOrganizationId(organizationId);
            SessionUtils.setCurrentProjectId(projectId);
            super.assertAuthorized(mi);
        } finally {
            SessionUtils.clearCurrentOrganizationId();
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
