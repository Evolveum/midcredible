package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.util.Comparable;

public interface DatabaseComparable extends Comparable {

    String JDBC_DRIVER = "jdbc.driver";

    String JDBC_URL_OLD_RESOURCE = "jdbc.url.old.resource";
    String JDBC_URL_NEW_RESOURCE = "jdbc.url.new.resource";

    String DATABASE_USERNAME_OLD_RESOURCE = "database.username.old.resource";
    String DATABASE_USERNAME_NEW_RESOURCE = "database.username.new.resource";

    String DATABASE_PASSWORD_OLD_RESOURCE = "database.password.old.resource";
    String DATABASE_PASSWORD_NEW_RESOURCE = "database.password.new.resource";
}
