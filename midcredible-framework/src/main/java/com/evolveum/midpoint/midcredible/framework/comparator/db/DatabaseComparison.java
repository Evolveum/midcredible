package com.evolveum.midpoint.midcredible.framework.comparator.db;

import com.evolveum.midpoint.midcredible.framework.util.ComparisonParent;

public interface DatabaseComparison extends ComparisonParent {

    String JDBC_DRIVER = "jdbc.driver";

    String JDBC_URL_OLD_RESOURCE = "jdbc.url.old.resource";
    String JDBC_URL_NEW_RESOURCE = "jdbc.url.new.resource";

    String DATABASE_USERNAME_OLD_RESOURCE = "database.username.old.resource";
    String DATABASE_USERNAME_NEW_RESOURCE = "database.username.new.resource";

    String DATABASE_PASSWORD_OLD_RESOURCE = "database.password.old.resource";
    String DATABASE_PASSWORD_NEW_RESOURCE = "database.password.new.resource";
}
