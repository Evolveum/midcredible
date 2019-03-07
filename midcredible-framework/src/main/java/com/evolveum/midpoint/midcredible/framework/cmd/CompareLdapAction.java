package com.evolveum.midpoint.midcredible.framework.cmd;

import com.evolveum.midpoint.midcredible.framework.comparator.ldap.LdapDbComparator;

import java.util.Properties;

import static com.evolveum.midpoint.midcredible.framework.comparator.ldap.LdapDbComparator.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CompareLdapAction implements Action<CompareLdapOptions> {

    private Properties properties;

    @Override
    public void init(CompareLdapOptions opts) throws Exception {
        // todo transalte opts to properties, or second option is to load properties from file (if there is one defined)
        // and transform properties to option bean and pass that bean to comparator

        properties = new Properties();

        // todo remove this
        properties.setProperty(PROP_CSV_PATH, "./target/out.csv");

        properties.setProperty(PROP_OLD_HOST, "localhost");
        properties.setProperty(PROP_OLD_PORT, "1389");
        properties.setProperty(PROP_OLD_SECURED, "false");
        properties.setProperty(PROP_OLD_USERNAME, "cn=admin,dc=example,dc=com");
        properties.setProperty(PROP_OLD_PASSWORD, "admin");

        properties.setProperty(PROP_NEW_HOST, "localhost");
        properties.setProperty(PROP_NEW_PORT, "2389");
        properties.setProperty(PROP_NEW_SECURED, "false");
        properties.setProperty(PROP_NEW_USERNAME, "cn=admin,dc=example,dc=com");
        properties.setProperty(PROP_NEW_PASSWORD, "admin");

        properties.setProperty(PROP_COMPARATOR_SCRIPT, "./src/test/resources/SimpleLdapComparator.groovy");

        properties.setProperty(PROP_WORKER_COUNT, "2");

        properties.setProperty(PROP_CSV_PRINT_EQUAL, "false");
    }

    @Override
    public void execute() {
        new LdapDbComparator(properties).execute();
    }
}
