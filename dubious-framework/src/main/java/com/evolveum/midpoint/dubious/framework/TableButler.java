package com.evolveum.midpoint.dubious.framework;

import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3.ConfigurationPropertiesType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TableButler extends JdbcButler {

	private String resourceOid;

	public TableButler(String id, Context context, String resourceOid) {
		super(id, context);

		this.resourceOid = resourceOid;
	}

	public TableButler(String id, Context context, JdbcTemplate client) {
		super(id, context, client);
	}

	@Override
	public void init() throws Exception {
		if (getClient() != null) {
			// client already initialized
			return;
		}

		if (resourceOid == null) {
			throw new IllegalStateException("Resource oid must be defined");
		}

		Context ctx = getContext();
		RestJaxbService midpoint = ctx.getMidpoint();
		ResourceType resource = midpoint.resources().oid(resourceOid).get();

		ConfigurationPropertiesType configuration = getConfigurationProperties(resource);

		String datasource = getValue(configuration, "datasource"); // todo
		if (StringUtils.isNotBlank(datasource)) {
			throw new IllegalStateException("Resource contains datasource configuration, we're unable to obtain datasource connection this way");
		}

		String driver = getValue(configuration, "jdbcDriver");
		String url = getValue(configuration, "jdbcUrlTemplate");
		String host = getValue(configuration, "host");
		String port = getValue(configuration, "port");
		String database = getValue(configuration, "database");

		String username = getValue(configuration, "user");
		String password = getValue(configuration, "password");

		String jdbcUrl = formatUrlTemplate(url, host, port, database);
		SingleConnectionDataSource ds = new SingleConnectionDataSource(jdbcUrl, username, password, true);
		ds.setDriverClassName(driver);

		new JdbcTemplate(ds);
	}

	private String formatUrlTemplate(String url, String host, String port, String database) {
		StringBuilder sb = new StringBuilder();
		int len = url.length();

		for (int i = 0; i < len; ++i) {
			char ch = url.charAt(i);
			if (ch != '%') {
				sb.append(ch);
			} else if (i + 1 < len) {
				++i;
				ch = url.charAt(i);
				if (ch == '%') {
					sb.append(ch);
				} else if (ch == 'h') {
					sb.append(host);
				} else if (ch == 'p') {
					sb.append(port);
				} else if (ch == 'd') {
					sb.append(database);
				}
			}
		}

		return sb.toString();
	}

	private ConfigurationPropertiesType getConfigurationProperties(ResourceType resource) {
		ConnectorConfigurationType configuration = resource.getConnectorConfiguration();
		for (Object object : configuration.getAny()) {
			if (object instanceof ConfigurationPropertiesType) {
				return (ConfigurationPropertiesType) object;
			}

			if (!(object instanceof JAXBElement)) {
				continue;
			}

			JAXBElement jaxb = (JAXBElement) object;
			if (!(jaxb.getValue() instanceof ConfigurationPropertiesType)) {
				continue;
			}

			return (ConfigurationPropertiesType) jaxb.getValue();
		}

		return null;
	}

	private String getValue(ConfigurationPropertiesType config, String name) {
		return getValue(config, new QName(name));
	}

	private String getValue(ConfigurationPropertiesType config, QName name) {
		if (config == null) {
			return null;
		}

		for (Object obj : config.getAny()) {
			if (!(obj instanceof Element)) {
				continue;
			}

			Element e = (Element) obj;
			if (!name.getLocalPart().equals(e.getLocalName())) {
				continue;
			}

			if (StringUtils.isNotEmpty(name.getNamespaceURI()) && !name.getNamespaceURI().equals(e.getNamespaceURI())) {
				continue;
			}

			return e.getTextContent();
		}

		return null;
	}
}
