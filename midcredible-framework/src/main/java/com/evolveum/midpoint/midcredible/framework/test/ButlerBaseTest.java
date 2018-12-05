package com.evolveum.midpoint.midcredible.framework.test;

import com.evolveum.midpoint.client.impl.restjaxb.AuthenticationType;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbService;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbServiceBuilder;
import com.evolveum.midpoint.midcredible.framework.Context;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger LOG = LoggerFactory.getLogger(ButlerBaseTest.class);

	private Context context;

	@BeforeClass
	public void beforeClass() throws Exception {
		LOG.info("Initializing butler context");

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

		LOG.debug("Using {} as midpoint rest url, authenticating as {} using {} authentication", new Object[]{url, username, authType});

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

		LOG.info("Butler context initialized");
	}

	public Context getContext() {
		return context;
	}
}
