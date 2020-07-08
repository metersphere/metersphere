package io.metersphere.ldap.dao;


import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.ldap.domain.Person;
import io.metersphere.service.SystemParameterService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.InvalidNameException;
import org.springframework.ldap.InvalidSearchFilterException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.*;
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
public class PersonRepoImpl implements PersonRepo {

    @Resource
    private SystemParameterService service;

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


    @Override
    public Person getDnForUser(String username) {
        LdapTemplate ldapTemplate = getConnection();

        String filter = getUserFilter();
        String ou = getUserOu();

        List<Person> result = null;
        try {
            result = ldapTemplate.search(query().base(ou).filter(filter, username), getContextMapper());
        } catch (NameNotFoundException e) {
            MSException.throwException(Translator.get("login_fail_ou_error"));
        } catch (InvalidNameException e) {
            MSException.throwException(Translator.get("login_fail_ou_error"));
        } catch (InvalidSearchFilterException e) {
            MSException.throwException(Translator.get("login_fail_filter_error"));
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

    private String getUserOu() {
        String ou = service.getValue(ParamConstants.LDAP.OU.getValue());

        if (StringUtils.isBlank(ou)) {
            MSException.throwException(Translator.get("ldap_ou_is_null"));
        }

        return ou;
    }

    protected ContextMapper getContextMapper() {
        return new PersonContextMapper();
    }

    private static class PersonContextMapper extends AbstractContextMapper<Person> {
        @Override
        public Person doMapFromContext(DirContextOperations context) {
            Person person = new Person();
            person.setDn(context.getNameInNamespace());
            person.setUid(context.getStringAttribute("uid"));
            person.setCommonName(context.getStringAttribute("cn"));
            person.setSurName(context.getStringAttribute("sn"));
            person.setUsername(context.getStringAttribute("sAMAccountName"));
            person.setEmail(context.getStringAttribute("mail"));
            return person;
        }
    }

    public LdapTemplate getConnection() {

        String url = service.getValue(ParamConstants.LDAP.URL.getValue());
        String dn = service.getValue(ParamConstants.LDAP.DN.getValue());
        String password = service.getValue(ParamConstants.LDAP.PASSWORD.getValue());

        preConnect(url, dn, password);

        String credentials = EncryptUtils.aesDecrypt(password).toString();

        LdapContextSource sourceLdapCtx = new LdapContextSource();
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
            MSException.throwException(Translator.get("ldap_connect_fail_user"));
        } catch (Exception e) {
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

}
