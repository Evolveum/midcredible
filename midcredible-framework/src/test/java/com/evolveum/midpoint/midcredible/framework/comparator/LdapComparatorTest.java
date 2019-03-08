package com.evolveum.midpoint.midcredible.framework.comparator;

import com.evolveum.midpoint.midcredible.framework.cmd.CompareLdapOptions;
import com.evolveum.midpoint.midcredible.framework.comparator.ldap.LdapDbComparator;
import org.testng.annotations.Test;

import java.util.Properties;

import static com.evolveum.midpoint.midcredible.framework.comparator.ldap.LdapDbComparator.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapComparatorTest {

    @Test
    public void simple() throws Exception {
        CompareLdapOptions opts = new CompareLdapOptions();

        Properties props = new Properties();

        props.setProperty(PROP_CSV_PATH, "./target/out.csv");

        props.setProperty(PROP_OLD_HOST, "localhost");
        props.setProperty(PROP_OLD_PORT, "1389");
        props.setProperty(PROP_OLD_SECURED, "false");
        props.setProperty(PROP_OLD_USERNAME, "cn=admin,dc=example,dc=com");
        props.setProperty(PROP_OLD_PASSWORD, "admin");

        props.setProperty(PROP_NEW_HOST, "localhost");
        props.setProperty(PROP_NEW_PORT, "2389");
        props.setProperty(PROP_NEW_SECURED, "false");
        props.setProperty(PROP_NEW_USERNAME, "cn=admin,dc=example,dc=com");
        props.setProperty(PROP_NEW_PASSWORD, "admin");

        props.setProperty(PROP_COMPARATOR_SCRIPT, "./src/test/resources/SimpleLdapComparator.groovy");

        props.setProperty(PROP_WORKER_COUNT, "2");

        props.setProperty(PROP_CSV_PRINT_EQUAL, "false");

        new LdapDbComparator(opts).execute();
    }
}
