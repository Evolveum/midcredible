package com.evolveum.midpoint.midcredible.comparator.ldap;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.evolveum.midpoint.midcredible.comparator.common.CsvPrinterOptions;

import java.io.File;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "compare-ldap", commandDescriptionKey = "compare-ldap")
public class CompareLdapOptions {

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

    public static final String P_OLD_ASK_PASSWORD = "-ox";
    public static final String P_OLD_ASK_PASSWORD_LONG = "--old-ask-password";

    public static final String P_NEW_HOST = "-nh";
    public static final String P_NEW_HOST_LONG = "--new-host";

    public static final String P_NEW_PORT = "-np";
    public static final String P_NEW_PORT_LONG = "--new-port";

    public static final String P_NEW_SECURED = "-ns";
    public static final String P_NEW_SECURED_LONG = "--new-secured";

    public static final String P_NEW_USERNAME = "-nu";
    public static final String P_NEW_USERNAME_LONG = "--new-username";

    public static final String P_NEW_PASSWORD = "-nw";
    public static final String P_NEW_PASSWORD_LONG = "--new-password";

    public static final String P_NEW_ASK_PASSWORD = "-nx";
    public static final String P_NEW_ASK_PASSWORD_LONG = "--new-ask-password";

    public static final String P_WORKERS = "-w";
    public static final String P_WORKERS_LONG = "--workers";

    public static final String P_DB_PATH = "-d";
    public static final String P_DB_PATH_LONG = "--db-path";

    public static final String P_COMPARE_SCRIPT_PATH = "-p";
    public static final String P_COMPARE_SCRIPT_PATH_LONG = "--compare-script-path";

    public static final String P_COMPRESS_DATA = "-g";
    public static final String P_COMPRESS_DATA_LONG = "--compress-data";

    public static final String P_ATTRIBUTES = "-a";
    public static final String P_ATTRIBUTES_LONG = "--attributes";

    public static final String P_ATTRIBUTES_TO_IGNORE = "-ai";
    public static final String P_ATTRIBUTES_TO_IGNORE_LONG = "--attributes-to-ignore";

    public static final String P_ATTRIBUTES_COMPARE_IGNORE_CASE = "-ac";
    public static final String P_ATTRIBUTES_COMPARE_IGNORE_CASE_LONG = "--attributes-compare-ignore-case";

    public static final String P_BASE_DN = "-b";
    public static final String P_BASE_DN_LONG = "--base-dn";

    public static final String P_FILTER = "-f";
    public static final String P_FILTER_LONG = "--filter";

    public static final String P_SCOPE = "-i";
    public static final String P_SCOPE_LONG = "--scope";

    @ParametersDelegate
    private CsvPrinterOptions csvPrinterOptions = new CsvPrinterOptions();

    @Parameter(names = {P_OLD_HOST, P_OLD_HOST_LONG}, descriptionKey = "compare-ldap.old.host")
    private String oldHost = "localhost";

    @Parameter(names = {P_OLD_PORT, P_OLD_PORT_LONG}, descriptionKey = "compare-ldap.old.port")
    private int oldPort = 389;

    @Parameter(names = {P_OLD_SECURED, P_OLD_SECURED_LONG}, descriptionKey = "compare-ldap.old.secured")
    private boolean oldSecured = false;

    @Parameter(names = {P_OLD_USERNAME, P_OLD_USERNAME_LONG}, descriptionKey = "compare-ldap.old.username")
    private String oldUsername;

    @Parameter(names = {P_OLD_PASSWORD, P_OLD_PASSWORD_LONG}, descriptionKey = "compare-ldap.old.password")
    private String oldPassword;

    @Parameter(names = {P_OLD_ASK_PASSWORD, P_OLD_ASK_PASSWORD_LONG}, password = true,
            descriptionKey = "compare-ldap.old.askPassword")
    private String oldAskPassword;

    @Parameter(names = {P_NEW_HOST, P_NEW_HOST_LONG}, descriptionKey = "compare-ldap.new.host")
    private String newHost = "localhost";

    @Parameter(names = {P_NEW_PORT, P_NEW_PORT_LONG}, descriptionKey = "compare-ldap.new.port")
    private int newPort = 389;

    @Parameter(names = {P_NEW_SECURED, P_NEW_SECURED_LONG}, descriptionKey = "compare-ldap.new.secured")
    private boolean newSecured = false;

    @Parameter(names = {P_NEW_USERNAME, P_NEW_USERNAME_LONG}, descriptionKey = "compare-ldap.new.username")
    private String newUsername;

    @Parameter(names = {P_NEW_PASSWORD, P_NEW_PASSWORD_LONG}, descriptionKey = "compare-ldap.new.password")
    private String newPassword;

    @Parameter(names = {P_NEW_ASK_PASSWORD, P_NEW_ASK_PASSWORD_LONG}, password = true,
            descriptionKey = "compare-ldap.new.askPassword")
    private String newAskPassword;

    @Parameter(names = {P_WORKERS, P_WORKERS_LONG}, descriptionKey = "compare-ldap.workers")
    private int workers = 1;

    @Parameter(names = {P_DB_PATH, P_DB_PATH_LONG}, descriptionKey = "compare-ldap.dbPath")
    private File dbPath = new File("./data");

    @Parameter(names = {P_COMPARE_SCRIPT_PATH, P_COMPARE_SCRIPT_PATH_LONG}, descriptionKey = "compare-ldap.scriptPath")
    private File compareScriptPath;

    @Parameter(names = {P_COMPRESS_DATA, P_COMPRESS_DATA_LONG}, descriptionKey = "compare-ldap.compressData")
    private boolean compressData;

    @Parameter(names = {P_ATTRIBUTES, P_ATTRIBUTES_LONG}, descriptionKey = "compare-ldap.attributes")
    private String attributes = "*";

    @Parameter(names = {P_ATTRIBUTES_TO_IGNORE, P_ATTRIBUTES_TO_IGNORE_LONG}, descriptionKey = "compare-ldap.attributesToIgnore")
    private String attributesToIgnore = "";

    @Parameter(names = {P_ATTRIBUTES_COMPARE_IGNORE_CASE, P_ATTRIBUTES_COMPARE_IGNORE_CASE_LONG}, descriptionKey = "compare-ldap.attributesCompareIgnoreCase")
    private String attributesCompareIgnoreCase = "";


    @Parameter(names = {P_BASE_DN, P_BASE_DN_LONG}, descriptionKey = "compare-ldap.baseDn")
    private String baseDn;

    @Parameter(names = {P_FILTER, P_FILTER_LONG}, descriptionKey = "compare-ldap.filter")
    private String filter = "(objectclass=*)";

    @Parameter(names = {P_SCOPE, P_SCOPE_LONG}, descriptionKey = "compare-ldap.scope", validateValueWith = ScopeValueValidator.class)
    private String scope = "sub";

    public String getOldHost() {
        return oldHost;
    }

    public void setOldHost(String oldHost) {
        this.oldHost = oldHost;
    }

    public int getOldPort() {
        return oldPort;
    }

    public void setOldPort(int oldPort) {
        this.oldPort = oldPort;
    }

    public boolean isOldSecured() {
        return oldSecured;
    }

    public void setOldSecured(boolean oldSecured) {
        this.oldSecured = oldSecured;
    }

    public String getOldUsername() {
        return oldUsername;
    }

    public void setOldUsername(String oldUsername) {
        this.oldUsername = oldUsername;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getOldAskPassword() {
        return oldAskPassword;
    }

    public void setOldAskPassword(String oldAskPassword) {
        this.oldAskPassword = oldAskPassword;
    }

    public String getNewHost() {
        return newHost;
    }

    public void setNewHost(String newHost) {
        this.newHost = newHost;
    }

    public int getNewPort() {
        return newPort;
    }

    public void setNewPort(int newPort) {
        this.newPort = newPort;
    }

    public boolean isNewSecured() {
        return newSecured;
    }

    public void setNewSecured(boolean newSecured) {
        this.newSecured = newSecured;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewAskPassword() {
        return newAskPassword;
    }

    public void setNewAskPassword(String newAskPassword) {
        this.newAskPassword = newAskPassword;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public File getDbPath() {
        return dbPath;
    }

    public void setDbPath(File dbPath) {
        this.dbPath = dbPath;
    }

    public File getCompareScriptPath() {
        return compareScriptPath;
    }

    public void setCompareScriptPath(File compareScriptPath) {
        this.compareScriptPath = compareScriptPath;
    }

    public CsvPrinterOptions getCsvPrinterOptions() {
        return csvPrinterOptions;
    }

    public boolean isCompressData() {
        return compressData;
    }

    public void setCompressData(boolean compressData) {
        this.compressData = compressData;
    }

    public void setCsvPrinterOptions(CsvPrinterOptions csvPrinterOptions) {
        this.csvPrinterOptions = csvPrinterOptions;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getAttributesToIgnore() {
        return attributesToIgnore;
    }

    public String getAttributesCompareIgnoreCase() {
        return attributesCompareIgnoreCase;
    }

    public void setAttributesToIgnore(String attributesToIgnore) {
        this.attributesToIgnore = attributesToIgnore;
    }

    public void setAttributesIgnoreCase(String attributesIgnoreCase) {
        this.attributesToIgnore = attributesToIgnore;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getOldPasswordOrAskPassword() {
        String password = getOldPassword();
        if (password == null) {
            password = getOldAskPassword();
        }

        return password;
    }

    public String getNewPasswordOrAskPassword() {
        String password = getNewPassword();
        if (password == null) {
            password = getNewAskPassword();
        }

        return password;
    }
}
