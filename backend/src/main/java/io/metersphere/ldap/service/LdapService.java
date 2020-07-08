package io.metersphere.ldap.service;

import io.metersphere.commons.exception.MSException;
import io.metersphere.controller.request.LoginRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.ldap.dao.PersonRepoImpl;
import io.metersphere.ldap.domain.Person;
import org.springframework.ldap.AuthenticationException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LdapService {

    @Resource
    private PersonRepoImpl personRepo;


    public Person authenticate(LoginRequest request) {
        String username = request.getUsername();
        String credentials = request.getPassword();
        Person person = null;

        try {
            person = personRepo.getDnForUser(username);
            personRepo.authenticate(person.getDn(), credentials);
        } catch (AuthenticationException e) {
            MSException.throwException(Translator.get("authentication_failed"));
        }

        return person;
    }

    public void testConnect() {
        personRepo.getConnection();
    }

}
