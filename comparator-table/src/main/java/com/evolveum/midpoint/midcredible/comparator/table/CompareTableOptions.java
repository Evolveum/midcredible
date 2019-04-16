package com.evolveum.midpoint.midcredible.comparator.table;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.evolveum.midpoint.midcredible.comparator.common.CsvPrinterOptions;

import java.io.File;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages", commandDescriptionKey = "compare-table")
public class CompareTableOptions {

    public static final String P_OLD_JDBC_URL = "-ol";
    public static final String P_OLD_JDBC_URL_LONG = "--old-jdbc-url";

    public static final String P_OLD_USERNAME = "-ou";
    public static final String P_OLD_USERNAME_LONG = "--old-username";

    public static final String P_OLD_PASSWORD = "-ow";
    public static final String P_OLD_PASSWORD_LONG = "--old-password";

    public static final String P_OLD_ASK_PASSWORD = "-ox";
    public static final String P_OLD_ASK_PASSWORD_LONG = "--old-ask-password";

    public static final String P_NEW_JDBC_URL = "-nl";
    public static final String P_NEW_JDBC_URL_LONG = "--new-jdbc-url";

    public static final String P_NEW_USERNAME = "-nu";
    public static final String P_NEW_USERNAME_LONG = "--new-username";

    public static final String P_NEW_PASSWORD = "-nw";
    public static final String P_NEW_PASSWORD_LONG = "--new-password";

    public static final String P_NEW_ASK_PASSWORD = "-nx";
    public static final String P_NEW_ASK_PASSWORD_LONG = "--new-ask-password";

    public static final String P_COMPARE_SCRIPT_PATH = "-p";
    public static final String P_COMPARE_SCRIPT_PATH_LONG = "--compare-script-path";

    public static final String P_COMPARE_ATTRIBUTE_VALUES = "-av";
    public static final String P_COMPARE_ATTRIBUTE_VALUES_LONG = "--compare-attribute-values";

    public static final String P_JDBC_DRIVER = "-jd";
    public static final String P_JDBC_DRIVER_LONG = "--compare-jdbc-driver";

    @ParametersDelegate
    private CsvPrinterOptions csvPrinterOptions = new CsvPrinterOptions();

    @Parameter(names = {P_OLD_JDBC_URL, P_OLD_JDBC_URL_LONG}, descriptionKey = "compare-table.old.url")
    private String oldJdbcUrl;

    @Parameter(names = {P_OLD_USERNAME, P_OLD_USERNAME_LONG}, descriptionKey = "compare-table.old.username")
    private String oldUsername;

    @Parameter(names = {P_OLD_PASSWORD, P_OLD_PASSWORD_LONG}, descriptionKey = "compare-table.old.password")
    private String oldPassword;

    @Parameter(names = {P_OLD_ASK_PASSWORD, P_OLD_ASK_PASSWORD_LONG}, password = true,
            descriptionKey = "compare-table.old.askPassword")
    private String oldAskPassword;

    @Parameter(names = {P_NEW_JDBC_URL, P_NEW_JDBC_URL_LONG}, descriptionKey = "compare-table.new.url")
    private String newJdbcUrl;

    @Parameter(names = {P_NEW_USERNAME, P_NEW_USERNAME_LONG}, descriptionKey = "compare-table.new.username")
    private String newUsername;

    @Parameter(names = {P_NEW_PASSWORD, P_NEW_PASSWORD_LONG}, descriptionKey = "compare-table.new.password")
    private String newPassword;

    @Parameter(names = {P_NEW_ASK_PASSWORD, P_NEW_ASK_PASSWORD_LONG}, password = true,
            descriptionKey = "compare-table.new.askPassword")
    private String newAskPassword;

    @Parameter(names = {P_COMPARE_SCRIPT_PATH, P_COMPARE_SCRIPT_PATH_LONG}, descriptionKey = "compare-table.scriptPath")
    private File compareScriptPath;

    @Parameter(names = {P_COMPARE_ATTRIBUTE_VALUES, P_COMPARE_ATTRIBUTE_VALUES_LONG}, descriptionKey = "compare-table.attribute.values")
    private boolean compareAttributeValues =true;

    @Parameter(names = {P_JDBC_DRIVER, P_JDBC_DRIVER_LONG}, descriptionKey = "compare-table.jdbc.driver")
    private String jdbcDriver;

    public CsvPrinterOptions getCsvPrinterOptions() {
        return csvPrinterOptions;
    }

    public void setCsvPrinterOptions(CsvPrinterOptions csvPrinterOptions) {
        this.csvPrinterOptions = csvPrinterOptions;
    }

    public String getOldJdbcUrl() {
        return oldJdbcUrl;
    }

    public void setOldJdbcUrl(String oldJdbcUrl) {
        this.oldJdbcUrl = oldJdbcUrl;
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

    public String getNewJdbcUrl() {
        return newJdbcUrl;
    }

    public void setNewJdbcUrl(String newJdbcUrl) {
        this.newJdbcUrl = newJdbcUrl;
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

    public File getCompareScriptPath() {
        return compareScriptPath;
    }

    public void setCompareScriptPath(File compareScriptPath) {
        this.compareScriptPath = compareScriptPath;
    }

    public Boolean getCompareAttributeValues() {
        return compareAttributeValues;
    }

    public void setCompareAttributeValues(Boolean compareAttributeValues) {
        this.compareAttributeValues = compareAttributeValues;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
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
