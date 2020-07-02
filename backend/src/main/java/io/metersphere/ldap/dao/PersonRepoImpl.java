package io.metersphere.ldap.dao;


import io.metersphere.commons.constants.ParamConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Translator;
import io.metersphere.ldap.domain.Person;
import io.metersphere.service.SystemParameterService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.*;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class PersonRepoImpl implements PersonRepo {

    @Resource
    private SystemParameterService service;

    public boolean authenticate(String dn, String credentials) {
        LdapTemplate ldapTemplate = getConnection();
        DirContext ctx = null;
        try {
            ctx = ldapTemplate.getContextSource().getContext(dn, credentials);
//            ldapTemplate.authenticate(dn, credentials);
            // Take care here - if a base was specified on the ContextSource
            // that needs to be removed from the user DN for the lookup to succeed.
            // ctx.lookup(userDn);
            return true;
        } catch (AuthenticationException e) {
            LogUtil.error("ldap authenticate failed..." + e);
            System.out.println("Login failed: " + e);
            MSException.throwException(Translator.get("authentication_failed"));
            return false;
        } catch (Exception e) {
            // Context creation failed - authentication did not succeed
            LogUtil.error("ldap authenticate failed..." + e);
            System.out.println("Login failed: " + e);
            MSException.throwException(Translator.get("ldap_connect_fail"));
            return false;
        } finally {
            // It is imperative that the created DirContext instance is always closed
            LdapUtils.closeContext((LdapContext) ctx);
        }
    }

    @Override
    public List findByName(String name) {
        LdapTemplate ldapTemplate = getConnection();
        ldapTemplate.setIgnorePartialResultException(true);
        LdapQuery query = query().where("cn").is(name);
        return ldapTemplate.search(query, getContextMapper());
    }

    @Override
    public String getDnForUser(String uid) {
        LdapTemplate ldapTemplate = getConnection();
        ldapTemplate.setIgnorePartialResultException(true);
        List<String> result = ldapTemplate.search(
                query().where("cn").is(uid),
                new AbstractContextMapper() {
                    @Override
                    protected String doMapFromContext(DirContextOperations ctx) {
                        return ctx.getNameInNamespace();
                    }
                });

        if (result.size() != 1) {
            throw new RuntimeException(Translator.get("user_not_found_or_not_unique"));
        }

        return result.get(0);
    }

    protected ContextMapper getContextMapper() {
        return new PersonContextMapper();
    }

    private static class PersonContextMapper extends AbstractContextMapper<Person> {
        @Override
        public Person doMapFromContext(DirContextOperations context) {
            Person person = new Person();
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
        String ou = service.getValue(ParamConstants.LDAP.OU.getValue());
        String password = service.getValue(ParamConstants.LDAP.PASSWORD.getValue());

        preConnect(url, dn, ou, password);

        String credentials = EncryptUtils.aesDecrypt(password).toString();


        LdapContextSource sourceLdapCtx = new LdapContextSource();
        sourceLdapCtx.setUrl(url);
        sourceLdapCtx.setUserDn(dn);
        sourceLdapCtx.setPassword(credentials);
        sourceLdapCtx.setBase(ou);
        sourceLdapCtx.setDirObjectFactory(DefaultDirObjectFactory.class);
        sourceLdapCtx.afterPropertiesSet();

        return new LdapTemplate(sourceLdapCtx);
    }

    private void preConnect(String url, String dn, String ou, String password) {

        if (StringUtils.isBlank(url)) {
            MSException.throwException(Translator.get("ldap_url_is_null"));
        }

        if (StringUtils.isBlank(dn)) {
            MSException.throwException(Translator.get("ldap_dn_is_null"));
        }

        if (StringUtils.isBlank(ou)) {
            MSException.throwException(Translator.get("ldap_ou_is_null"));
        }

        if (StringUtils.isBlank(password)) {
            MSException.throwException(Translator.get("ldap_password_is_null"));
        }

    }

}
