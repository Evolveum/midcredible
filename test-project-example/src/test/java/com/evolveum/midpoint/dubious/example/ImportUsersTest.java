package com.evolveum.midpoint.dubious.example;

import com.evolveum.midpoint.client.api.ObjectReference;
import com.evolveum.midpoint.client.api.SearchResult;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbService;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbServiceUtil;
import com.evolveum.midpoint.dubious.framework.Context;
import com.evolveum.midpoint.dubious.framework.TableButler;
import com.evolveum.midpoint.dubious.framework.test.ButlerBaseTest;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.ContextConfiguration;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
@ContextConfiguration(classes = com.evolveum.midpoint.dubious.example.ContextConfiguration.class)
public class ImportUsersTest extends ButlerBaseTest {

	private TableButler tableButler;

	@BeforeClass
	@Override
	public void beforeClass() throws Exception {
		super.beforeClass();

		tableButler = new TableButler("sometable", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff430056");
		tableButler.init();
		tableButler = new TableButler("asdf", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff430056");
		tableButler.init();
		tableButler = new TableButler("df", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff430056");
		tableButler.init();
		tableButler = new TableButler("df", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff430056");
		tableButler.init();

	}

	@Test
	public void importUser() throws Exception {
		JdbcTemplate tableResource = tableButler.getClient();

		final String GIVEN_NAME = "John";
		final String FAMILY_NAME = "Doe";
		final boolean ACTIVE = true;

		assertUsersCount(0L);

		tableResource.update("insert into users (givenName, familyName, active) values (?,?,?)",
				GIVEN_NAME, FAMILY_NAME, ACTIVE);

		assertUsersCount(1L);
		// add user to DB table

		// import one user using MidPoint REST api
		Context ctx = getContext();
		RestJaxbService service = ctx.getMidpoint();

		ItemPathType path = new ItemPathType();
		path.setValue("attributes/username");
		SearchResult<ShadowType> result = service.shadows().search().queryFor(ShadowType.class)
				.item(path).eq("administrator").and()
				.item(new QName("kind")).eq(ShadowKindType.ACCOUNT).and()
				.item(new QName("resourceRef")).ref("04afeda6-394b-11e6-8cbe-abf7ff430056").get();

		AssertJUnit.assertNotNull(result);
		AssertJUnit.assertFalse(result.isEmpty());
		ShadowType shadow = result.get(0);

		OperationResultType importResult = service.shadows().oid(shadow.getOid()).importShadow();

		AssertJUnit.assertEquals(OperationResultStatusType.SUCCESS, importResult.getStatus());

		service.shadows().oid(shadow.getOid());

//        OperationResultType result = service.resources().oid("ef2bc95b-76e0-59e2-86d6-9999cccccccc").test();
//        System.out.println(result);
//
//        RestJaxbServiceUtil util = new RestJaxbServiceUtil(service.getJaxbContext());
////
//        UserType user = new UserType();
//        user.setGivenName(util.createPoly("Jack"));
//        user.setFamilyName(util.createPoly("Black"));
////
//        ObjectReference result = service.users().add(user).post();
//
//        // validate MP user
//
//        UserType createdUser = service.users().oid(result.getOid()).get();
//        AssertJUnit.assertEquals("jblack", util.getOrig(createdUser.getName()));
//        AssertJUnit.assertEquals("Jack", util.getOrig(createdUser.getGivenName()));
//        AssertJUnit.assertEquals("Black", util.getOrig(createdUser.getFamilyName()));
//        AssertJUnit.assertEquals("Jack Black", util.getOrig(createdUser.getFullName()));
//
//        // validate target resources
//
		// cleanup
		tableResource.execute("delete from users");

//        service.users().oid(result.getOid()).delete();

	}

	private void assertUsersCount(long expectedCount) {
		Number count = tableButler.getClient().queryForObject("select count(*) from users", Number.class);
		long lCount = count.longValue();

		AssertJUnit.assertEquals(expectedCount, lCount);
	}

}
