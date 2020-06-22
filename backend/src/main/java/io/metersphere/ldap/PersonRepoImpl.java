package io.metersphere.ldap;


import io.metersphere.ldap.domain.Person;
import org.apache.shiro.realm.ldap.LdapUtils;
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

    @Override
    public List findByName(String name) {
        ldapTemplate.setIgnorePartialResultException(true);
        LdapQuery query = query()
                .where("objectclass").is("person")
                .and("cn").is(name);
        return ldapTemplate.search(query, getContextMapper());
    }

    protected ContextMapper getContextMapper() {
        return new PersonContextMapper();
    }


    private static class PersonContextMapper extends AbstractContextMapper<Person> {
        @Override
        public Person doMapFromContext(DirContextOperations context) {
            Person person = new Person();
            person.setCommonName(context.getStringAttribute("cn"));
            person.setSuerName(context.getStringAttribute("sn"));
            return person;
        }
    }

//    public boolean authenticate(String userDn, String credentials) {
//        DirContext ctx = null;
//        try {
//            ctx = ldapTemplate.getContextSource().getContext(userDn, credentials);
//            // Take care here - if a base was specified on the ContextSource
//            // that needs to be removed from the user DN for the lookup to succeed.
////            ctx.lookup(userDn);
//            return true;
//        } catch (Exception e) {
//            // Context creation failed - authentication did not succeed
//            System.out.println("Login failed: " + e);
//            return false;
//        } finally {
//            // It is imperative that the created DirContext instance is always closed
//            LdapUtils.closeContext((LdapContext) ctx);
//        }
//    }
}
