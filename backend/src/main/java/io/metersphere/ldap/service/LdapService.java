package io.metersphere.ldap.service;

import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.ldap.dao.PersonRepoImpl;
import io.metersphere.ldap.domain.LdapInfo;
import io.metersphere.ldap.domain.Person;
import org.springframework.ldap.CommunicationException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LdapService {

    @Resource
    private PersonRepoImpl personRepo;


    public Person authenticate(LoginRequest request) {
        String dn = null;
        String username = request.getUsername();
        String credentials = request.getPassword();

        List<Person> personList = null;
        try {
            // select user by sAMAccountName
            personList = personRepo.findByName(username);

            if (personList.size() == 1) {
                dn = personRepo.getDnForUser(username);
            } else if (personList.size() == 0) {
                MSException.throwException(Translator.get("user_not_exist") + username);
            } else {
                MSException.throwException(Translator.get("find_more_user"));
            }
        } catch (CommunicationException e) {
            MSException.throwException(Translator.get("ldap_connect_fail"));
        }
        personRepo.authenticate(dn, credentials);

        return personList.get(0);
    }

    public void testConnect(LdapInfo ldap) {
        personRepo.authenticate(ldap.getDn(), ldap.getPassword());
    }

}
