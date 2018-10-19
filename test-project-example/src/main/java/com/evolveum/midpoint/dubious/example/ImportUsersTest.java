package com.evolveum.midpoint.dubious.example;

import com.evolveum.midpoint.client.api.SearchResult;
import com.evolveum.midpoint.client.impl.restjaxb.AuthenticationType;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbService;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbServiceBuilder;
import com.evolveum.midpoint.dubious.framework.test.ButlerBaseTest;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowKindType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

/**
 * Created by Viliam Repan (lazyman).
 */
@ContextConfiguration(classes = EnvConfig.class)
@TestPropertySource("classpath:configuration.properties")
public class ImportUsersTest extends ButlerBaseTest {

	@Autowired
	private JdbcTemplate tableResource;

	@Value("${midpoint.url}")
	private String midpointUrl;
	@Value("${midpoint.rest.username}")
	private String midpointUsername;
	@Value("${midpoint.rest.password}")
	private String midpointPassword;

	@Test
	public void importUser() throws Exception {
		final String GIVEN_NAME = "John";
		final String FAMILY_NAME = "Doe";
		final boolean ACTIVE = true;

		assertUsersCount(0L);

		tableResource.update("insert into users (givenName, familyName, active) values (?,?,?)",
				GIVEN_NAME, FAMILY_NAME, ACTIVE);

		assertUsersCount(1L);
		// add user to DB table

		// import one user using MidPoint REST api
		RestJaxbServiceBuilder builder = new RestJaxbServiceBuilder();
		builder.url(midpointUrl)
				.username(midpointUsername)
				.password(midpointPassword)
				.authentication(AuthenticationType.BASIC);
		RestJaxbService service = builder.build();

		ItemPathType path = new ItemPathType();
		path.setValue("attributes/username");
		SearchResult<ShadowType> result = service.shadows().search().queryFor(ShadowType.class)
				.item(path).eq("administrator").and()
				.item(new QName("kind")).eq(ShadowKindType.ACCOUNT).and()
				.item(new QName("resourceRef")).ref("ef2bc95b-76e0-59e2-86d6-9999cccccccc").get();

		AssertJUnit.assertFalse(result.isEmpty());
		ShadowType shadow = result.get(0);

		OperationResultType importResult = service.shadows().oid(shadow.getOid()).importShadow();

		AssertJUnit.assertEquals(OperationResultStatusType.SUCCESS, importResult.getStatus());

		service.shadows().oid(shadow.getOid());

//        OperationResultType result = service.resources().oid("ef2bc95b-76e0-59e2-86d6-9999cccccccc").test();
//        System.out.println(result);
//
//        RestJaxbServiceUtil util = new RestJaxbServiceUtil(service.getJaxbContext());
//
//        UserType user = new UserType();
//        user.setGivenName(util.createPoly("Jack"));
//        user.setFamilyName(util.createPoly("Black"));
//
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
		Number count = tableResource.queryForObject("select count(*) from users", Number.class);
		long lCount = count.longValue();

		AssertJUnit.assertEquals(expectedCount, lCount);
	}

}
