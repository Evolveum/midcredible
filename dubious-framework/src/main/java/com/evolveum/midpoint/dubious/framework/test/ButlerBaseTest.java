package com.evolveum.midpoint.dubious.framework.test;

import com.evolveum.midpoint.client.impl.restjaxb.AuthenticationType;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbService;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbServiceBuilder;
import com.evolveum.midpoint.dubious.framework.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ButlerBaseTest extends AbstractTestNGSpringContextTests {

	public static final String P_MIDPOINT_URL = "midpoint.url";
	public static final String P_MIDPOINT_USERNAME = "midpoint.rest.username";
	public static final String P_MIDPOINT_PASSWORD = "midpoint.rest.password";
	public static final String P_MIDPOINT_AUTH_TYPE = "midpoint.rest.authentication";

	public static final String REST_URL_PREFIX = "/ws/rest";

	private Context context;

	@BeforeClass
	public void beforeClass() throws Exception {
		System.out.println("bb");

		Properties props = new Properties();

		try (InputStream is = ButlerBaseTest.class.getResourceAsStream("/configuration.properties")) {
			props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
		} catch (IOException ex) {
			throw new IllegalStateException("Couldn't load configuration.properties", ex);
		}

		String url = props.getProperty(P_MIDPOINT_URL);
		if (!url.endsWith(REST_URL_PREFIX)) {
			url += REST_URL_PREFIX;
		}
		String username = props.getProperty(P_MIDPOINT_USERNAME);
		String password = props.getProperty(P_MIDPOINT_PASSWORD);
		String auth = props.getProperty(P_MIDPOINT_AUTH_TYPE);

		AuthenticationType authType = AuthenticationType.BASIC;
		if (StringUtils.isNotEmpty(auth)) {
			authType = AuthenticationType.valueOf(auth);
		}

		try {
			RestJaxbServiceBuilder builder = new RestJaxbServiceBuilder();
			builder.url(url)
					.username(username)
					.password(password)
					.authentication(authType);
			RestJaxbService service = builder.build();

			Map<Object, Object> map = new HashMap<>();
			map.putAll(props);

			context = new Context(service, props);
		} catch (IOException ex) {
			throw new IllegalStateException("Couldn't create butler context", ex);
		}
	}

	public Context getContext() {
		return context;
	}
}
