package io.metersphere.sdk.ldap;

import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.Context;
import java.util.Hashtable;

public class SSLLdapContextSource extends LdapContextSource {
    public Hashtable<String, Object> getAnonymousEnv() {
        Hashtable<String, Object> anonymousEnv = super.getAnonymousEnv();
        anonymousEnv.put("java.naming.security.protocol", "ssl");
        anonymousEnv.put("java.naming.ldap.factory.socket", CustomSSLSocketFactory.class.getName());
        anonymousEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        return anonymousEnv;
    }
}