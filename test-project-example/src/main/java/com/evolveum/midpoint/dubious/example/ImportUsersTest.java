package com.evolveum.midpoint.dubious.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Created by Viliam Repan (lazyman).
 */
@ContextConfiguration(classes = EnvConfig.class)
@TestPropertySource("classpath:configuration.properties")
public class ImportUsersTest extends BaseTest {

    @Autowired
    private JdbcTemplate tableResource;

    @Test
    public void importUser() throws Exception {
        assertUsersCount(0L);

        tableResource.update("insert into users (givenName, familyName, active) values (?,?,?)", "john", "doe", true);

        assertUsersCount(1L);
        // add user to DB table

        // import one user using MidPoint REST api

        // validate MP user

        // validate target resources
    }

    private void assertUsersCount(long expectedCount) {
        Number count = tableResource.queryForObject("select count(*) from users", Number.class);
        long lCount = count.longValue();

        AssertJUnit.assertEquals(expectedCount, lCount);
    }

}
