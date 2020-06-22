package io.metersphere.ldap;


import java.util.List;

public interface PersonRepo {

    List<String> getAllPersonNames();

    List findByName(String name);
}
