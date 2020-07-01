package io.metersphere.ldap.dao;


import java.util.List;

public interface PersonRepo {

    List findByName(String name);

    String getDnForUser(String name);
}
