package com.evolveum.midpoint.midcredible.comparator.ldap;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapComparatorException extends RuntimeException {

    public LdapComparatorException() {
    }

    public LdapComparatorException(String message) {
        super(message);
    }

    public LdapComparatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public LdapComparatorException(Throwable cause) {
        super(cause);
    }
}
