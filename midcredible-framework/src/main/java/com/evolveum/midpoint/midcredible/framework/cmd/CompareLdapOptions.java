package com.evolveum.midpoint.midcredible.framework.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages", commandDescriptionKey = "compare-ldap")
public class CompareLdapOptions extends CompareCommonOptions {

    public static final String P_OLD_HOST = "-oh";
    public static final String P_OLD_HOST_LONG = "--old-host";

    public static final String P_OLD_PORT = "-op";
    public static final String P_OLD_PORT_LONG = "--old-port";

    public static final String P_OLD_SECURED = "-os";
    public static final String P_OLD_SECURED_LONG = "--old-secured";

    public static final String P_OLD_USERNAME = "-ou";
    public static final String P_OLD_USERNAME_LONG = "--old-username";

    public static final String P_OLD_PASSWORD = "-ow";
    public static final String P_OLD_PASSWORD_LONG = "--old-password";

    public static final String P_OLD_PASSWORD_ASK = "-ox";
    public static final String P_OLD_PASSWORD_ASK_LONG = "--old-password-ask";

    @Parameter(names = {P_OLD_HOST, P_OLD_HOST_LONG}, descriptionKey = "compare-ldap.old.host")
    private String oldHost;

    @Parameter(names = {P_OLD_PORT, P_OLD_PORT_LONG}, descriptionKey = "compare-ldap.old.port")
    private String oldPort;

    @Parameter(names = {P_OLD_SECURED, P_OLD_SECURED_LONG}, descriptionKey = "compare-ldap.old.secured")
    private String oldSecured;

    @Parameter(names = {P_OLD_USERNAME, P_OLD_USERNAME_LONG}, descriptionKey = "compare-ldap.old.username")
    private String oldUsername;

    private String oldPassword;

    private String oldAskPassword;

    public String getOldHost() {
        return oldHost;
    }

    public String getOldPort() {
        return oldPort;
    }

    public String getOldSecured() {
        return oldSecured;
    }

    public String getOldUsername() {
        return oldUsername;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getOldAskPassword() {
        return oldAskPassword;
    }
}
