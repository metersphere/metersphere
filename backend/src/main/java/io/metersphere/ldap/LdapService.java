package io.metersphere.ldap;

import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.request.LoginRequest;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class LdapService {

    @Resource
    private LdapTemplate ldapTemplate;

    @Resource
    private PersonRepoImpl personRepo;

    public boolean authenticate(LoginRequest request) {
//        String userDn, String credentials
        String username = request.getUsername();
        String credentials = request.getPassword();

        List user = personRepo.findByName(username);
        if (user.size() > 0) {
            System.out.println(user);
        } else {
            MSException.throwException("no such user");
        }
        try {
            ldapTemplate.authenticate(query()
                    .where("objectclass").is("person")
                    .and("cn").is(username), credentials);
            // Take care here - if a base was specified on the ContextSource
            // that needs to be removed from the user DN for the lookup to succeed.
            // ctx.lookup(userDn);
            return true;
        } catch (Exception e) {
            // Context creation failed - authentication did not succeed
            System.out.println("Login failed: " + e);
            return false;
        } finally {
            // It is imperative that the created DirContext instance is always closed
//            LdapUtils.closeContext((LdapContext) ctx);
        }
    }
}
