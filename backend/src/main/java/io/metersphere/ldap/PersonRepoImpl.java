package io.metersphere.ldap;


import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.ldap.domain.Person;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.*;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import java.util.List;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class PersonRepoImpl implements PersonRepo {

    @Resource
    private LdapTemplate ldapTemplate;


    @Override
    public List<String> getAllPersonNames() {
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate.search(
                query().where("objectclass").is("person"),
                new AttributesMapper<String>() {
                    @Override
                    public String mapFromAttributes(Attributes attrs)
                            throws NamingException, javax.naming.NamingException {
                        return attrs.toString();
                    }
                });
    }

    public boolean authenticate(String dn, String credentials) {
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
            MSException.throwException("用户认证失败！");
            return false;
        } catch (Exception e) {
            // Context creation failed - authentication did not succeed
            LogUtil.error("ldap authenticate failed..." + e);
            System.out.println("Login failed: " + e);
            MSException.throwException("login failed...");
            return false;
        } finally {
            // It is imperative that the created DirContext instance is always closed
            LdapUtils.closeContext((LdapContext) ctx);
        }
    }

    public List<Person> getAllPersons() {
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate.search(query()
                .where("objectclass").is("person"), getContextMapper());
    }

    @Override
    public List findByName(String name) {
        ldapTemplate.setIgnorePartialResultException(true);
        LdapQuery query = query()
//                .where("objectclass").is("person")
//                .and("cn").is(name);
                .where("cn").is(name);
        return ldapTemplate.search(query, getContextMapper());
    }

    @Override
    public String getDnForUser(String uid) {
        List<String> result = ldapTemplate.search(
                query().where("cn").is(uid),
                new AbstractContextMapper() {
                    @Override
                    protected String doMapFromContext(DirContextOperations ctx) {
                        return ctx.getNameInNamespace();
                    }
                });

        if(result.size() != 1) {
            throw new RuntimeException("User not found or not unique");
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

}
