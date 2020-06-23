package io.metersphere.ldap;


import io.metersphere.ldap.domain.Person;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.*;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.naming.directory.Attributes;


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
            person.setSuerName(context.getStringAttribute("sn"));
            return person;
        }
    }

}
