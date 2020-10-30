package io.metersphere.ldap.service;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.service.SystemParameterService;
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

import javax.annotation.Resource;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class LdapService {

    @Resource
    private SystemParameterService service;

    public DirContextOperations authenticate(LoginRequest request) {
        String username = request.getUsername();
        String credentials = request.getPassword();
        DirContextOperations dirContextOperations = null;

        try {
            // 获取LDAP用户相关信息
            dirContextOperations = getContextMapper(username);
            // 执行登录认证
            authenticate(String.valueOf(dirContextOperations.getDn()), credentials);
        } catch (AuthenticationException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("authentication_failed"));
        }

        // 检查属性是否存在
        getMappingAttr("name", dirContextOperations);
        getMappingAttr("email", dirContextOperations);


        return dirContextOperations;
    }

    public void testConnect() {
        getConnection();
    }

    public boolean authenticate(String dn, String credentials) {
        LdapTemplate ldapTemplate = getConnection();
        return authenticate(dn, credentials, ldapTemplate);
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

    public DirContextOperations getContextMapper(String username) {
        LdapTemplate ldapTemplate = getConnection();

        String filter = getUserFilter();
        String[] arr = getUserOu();

        List<DirContextOperations> result = null;
        // 多OU
        for (String ou : arr) {
            try {
                result = ldapTemplate.search(query().base(ou.trim()).filter(filter, username), new MsContextMapper());
                if (result.size() == 1) {
                    return result.get(0);
                }
            } catch (NameNotFoundException | InvalidNameException e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException(Translator.get("login_fail_ou_error"));
            } catch (InvalidSearchFilterException e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException(Translator.get("login_fail_filter_error"));
            }
        }

        if (result.size() != 1) {
            MSException.throwException(Translator.get("user_not_found_or_not_unique"));
        }

        return result.get(0);
    }

    private String getUserFilter() {
        String filter = service.getValue(ParamConstants.LDAP.FILTER.getValue());

        if (StringUtils.isBlank(filter)) {
            MSException.throwException(Translator.get("ldap_user_filter_is_null"));
        }

        return filter;
    }

    private String[] getUserOu() {
        String ou = service.getValue(ParamConstants.LDAP.OU.getValue());

        if (StringUtils.isBlank(ou)) {
            MSException.throwException(Translator.get("ldap_ou_is_null"));
        }

        return ou.split("\\|");
    }

    private static class MsContextMapper extends AbstractContextMapper<DirContextOperations> {
        @Override
        public DirContextOperations doMapFromContext(DirContextOperations context) {
            return context;
        }
    }

    public LdapTemplate getConnection() {

        String url = service.getValue(ParamConstants.LDAP.URL.getValue());
        String dn = service.getValue(ParamConstants.LDAP.DN.getValue());
        String password = service.getValue(ParamConstants.LDAP.PASSWORD.getValue());

        preConnect(url, dn, password);

        String credentials = EncryptUtils.aesDecrypt(password).toString();
        LdapContextSource sourceLdapCtx;
        if (StringUtils.startsWithIgnoreCase(url, "ldaps://")) {
            sourceLdapCtx = new SSLLdapContextSource();
            // todo 这里加上strategy 会报错
//        DefaultTlsDirContextAuthenticationStrategy strategy = new DefaultTlsDirContextAuthenticationStrategy();
//        strategy.setShutdownTlsGracefully(true);
//        strategy.setHostnameVerifier((hostname, session) -> true);
//        sourceLdapCtx.setAuthenticationStrategy(strategy);
        } else {
            sourceLdapCtx = new LdapContextSource();
        }
        sourceLdapCtx.setUrl(url);
        sourceLdapCtx.setUserDn(dn);
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

        // ldapTemplate 是否可用
        try {
            authenticate(dn, credentials, ldapTemplate);
        } catch (AuthenticationException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("ldap_connect_fail_user"));
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("ldap_connect_fail"));
        }

        return ldapTemplate;
    }

    private void preConnect(String url, String dn, String password) {

        if (StringUtils.isBlank(url)) {
            MSException.throwException(Translator.get("ldap_url_is_null"));
        }

        if (StringUtils.isBlank(dn)) {
            MSException.throwException(Translator.get("ldap_dn_is_null"));
        }

        if (StringUtils.isBlank(password)) {
            MSException.throwException(Translator.get("ldap_password_is_null"));
        }

    }

    private String getLdapMapping() {
        String mapping = service.getValue(ParamConstants.LDAP.MAPPING.getValue());

        if (StringUtils.isBlank(mapping)) {
            MSException.throwException(Translator.get("ldap_user_mapping_is_null"));
        }

        return mapping;
    }

    public String getMappingAttr(String attr, DirContextOperations dirContext) {
        // 检查LDAP映射属性
        String mapping = getLdapMapping();
        JSONObject jsonObject = JSONObject.parseObject(mapping);

        String mapAttr = jsonObject.getString(attr);
        if (StringUtils.isBlank(mapAttr)) {
            MSException.throwException(Translator.get("check_ldap_mapping") + " " + attr);
        }

        String result = dirContext.getStringAttribute(mapAttr);
        if (StringUtils.isBlank(result)) {
            MSException.throwException(Translator.get("ldap_mapping_value_null") + " " + mapAttr);
        }

        return result;
    }

    public String getNotRequiredMappingAttr(String attr, DirContextOperations dirContext) {
        String mapping = getLdapMapping();
        JSONObject jsonObject = JSONObject.parseObject(mapping);

        String mapAttr = jsonObject.getString(attr);

        if (StringUtils.isNotBlank(mapAttr)) {
            return dirContext.getStringAttribute(mapAttr);
        }
        return mapAttr;
    }

    public boolean isOpen() {
        String open = service.getValue(ParamConstants.LDAP.OPEN.getValue());
        if (StringUtils.isBlank(open)) {
            return false;
        }
        return StringUtils.equals(Boolean.TRUE.toString(), open);
    }
}
