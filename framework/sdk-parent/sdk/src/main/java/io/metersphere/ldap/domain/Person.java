package io.metersphere.ldap.domain;

import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

@Data
@Entry(objectClasses = {"person", "top"})
public class Person {

    @Id
    private Name id;
    @DnAttribute(value = "uid", index = 0)
    private String uid;
    @Attribute(name = "cn")
    private String commonName;
    @Attribute(name = "sn")
    private String surName;
    @Attribute(name = "sAMAccountName")
    private String username;
    @Attribute(name = "mail")
    private String email;
    private String dn;

}