package io.metersphere.ldap.service;

import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.ldap.dao.PersonRepoImpl;
import io.metersphere.ldap.domain.LdapInfo;
import org.springframework.ldap.CommunicationException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LdapService {

    @Resource
    private PersonRepoImpl personRepo;


    public void authenticate(LoginRequest request) {
        String dn = null;
        String username = request.getUsername();
        String credentials = request.getPassword();

        try {
            // select user by sAMAccountName
            List user = personRepo.findByName(username);

            if (user.size() == 1) {
                dn = personRepo.getDnForUser(username);
            } else if (user.size() == 0) {
                MSException.throwException(Translator.get("user_not_exist") + username);
            } else {
                MSException.throwException("Found multiple users");
            }
        } catch (CommunicationException e) {
            MSException.throwException("LDAP Server connection failed!");
        }
        personRepo.authenticate(dn, credentials);
    }

    public void testConnect(LdapInfo ldap) {
        personRepo.authenticate(ldap.getDn(), ldap.getPassword());
    }

}
