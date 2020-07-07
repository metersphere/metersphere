package io.metersphere.ldap.dao;


import io.metersphere.ldap.domain.Person;

public interface PersonRepo {

    Person getDnForUser(String name);
}
