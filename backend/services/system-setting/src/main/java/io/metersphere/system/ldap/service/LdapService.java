package io.metersphere.system.ldap.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.ldap.SSLLdapContextSource;
import io.metersphere.system.ldap.vo.LdapLoginRequest;
import io.metersphere.system.ldap.vo.LdapRequest;
import io.metersphere.sdk.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.InvalidNameException;
import org.springframework.ldap.InvalidSearchFilterException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
@Transactional(rollbackFor = Exception.class)
public class LdapService {


    public void testConnect(LdapRequest request) {
        getConnect(request);
    }

    private LdapTemplate getConnect(LdapRequest request) {
        String credentials = EncryptUtils.aesDecrypt(request.getLdapPassword()).toString();
        LdapContextSource sourceLdapCtx;
        if (StringUtils.startsWithIgnoreCase(request.getLdapUrl(), "ldaps://")) {
            sourceLdapCtx = new SSLLdapContextSource();
            // todo 这里加上strategy 会报错
        } else {
            sourceLdapCtx = new LdapContextSource();
        }
        sourceLdapCtx.setUrl(request.getLdapUrl());
        sourceLdapCtx.setUserDn(request.getLdapDn());
        sourceLdapCtx.setPassword(credentials);
        sourceLdapCtx.setDirObjectFactory(DefaultDirObjectFactory.class);
        sourceLdapCtx.afterPropertiesSet();
        LdapTemplate ldapTemplate = new LdapTemplate(sourceLdapCtx);
        ldapTemplate.setIgnorePartialResultException(true);
        Map<String, Object> baseEnv = new Hashtable<>();
        baseEnv.put("com.sun.jndi.ldap.connect.timeout", "3000");
        baseEnv.put("com.sun.jndi.ldap.read.timeout", "3000");
        sourceLdapCtx.setBaseEnvironmentProperties(baseEnv);
        ldapTemplate.setDefaultSearchScope(SearchScope.SUBTREE.getId());
        try {
            authenticate(request.getLdapDn(), credentials, ldapTemplate);
        } catch (AuthenticationException e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(Translator.get("ldap_connect_fail_user"));
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(Translator.get("ldap_connect_fail"));
        }
        return ldapTemplate;
    }


    private boolean authenticate(String dn, String credentials, LdapTemplate ldapTemplate) throws AuthenticationException {
        DirContext ctx = null;
        try {
            ctx = ldapTemplate.getContextSource().getContext(dn, credentials);
            return true;
        } finally {
            // It is imperative that the created DirContext instance is always closed
            LdapUtils.closeContext((LdapContext) ctx);
        }
    }

    /**
     * 测试登录
     *
     * @param request
     * @return
     */
    public DirContextOperations testLogin(LdapLoginRequest request) {
        String credentials = request.getPassword();
        DirContextOperations dirContextOperations = null;

        try {
            LdapTemplate ldapTemplate = getLdapTemplate(request);
            // 获取LDAP用户相关信息
            dirContextOperations = getContextMapper(request, ldapTemplate);
            // 执行登录认证
            authenticate(String.valueOf(dirContextOperations.getDn()), credentials, ldapTemplate);
        } catch (AuthenticationException e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(Translator.get("authentication_failed"));
        }

        // 检查属性是否存在
        getMappingAttr("name", dirContextOperations, request);
        return dirContextOperations;
    }

    private LdapTemplate getLdapTemplate(LdapLoginRequest request) {
        LdapRequest ldapRequest = new LdapRequest();
        BeanUtils.copyBean(ldapRequest, request);
        LdapTemplate ldapTemplate = getConnect(ldapRequest);
        return ldapTemplate;
    }


    public String getMappingAttr(String attr, DirContextOperations dirContext, LdapLoginRequest request) {
        // 检查LDAP映射属性
        String mapping = request.getLdapUserMapping();
        Map jsonObject = JSON.parseObject(mapping, Map.class);
        String mapAttr = (String) jsonObject.get(attr);
        String result = dirContext.getStringAttribute(mapAttr);
        return result;
    }

    public DirContextOperations getContextMapper(LdapLoginRequest request, LdapTemplate ldapTemplate) {
        String filter = request.getLdapUserFilter();
        String[] arr = request.getLdapUserOu().split("|");

        List<DirContextOperations> result = null;
        // 多OU
        for (String ou : arr) {
            try {
                result = ldapTemplate.search(query().base(ou.trim()).filter(filter, request.getUsername()), new MsContextMapper());
                if (result.size() == 1) {
                    return result.get(0);
                }
            } catch (NameNotFoundException | InvalidNameException e) {
                LogUtils.error(e.getMessage(), e);
                throw new MSException(Translator.get("login_fail_ou_error"));
            } catch (InvalidSearchFilterException e) {
                LogUtils.error(e.getMessage(), e);
                throw new MSException(Translator.get("login_fail_filter_error"));
            }
        }

        if (result.size() != 1) {
            throw new MSException(Translator.get("user_not_found_or_not_unique"));
        }

        return result.get(0);
    }

    private static class MsContextMapper extends AbstractContextMapper<DirContextOperations> {
        @Override
        public DirContextOperations doMapFromContext(DirContextOperations context) {
            return context;
        }
    }
}
