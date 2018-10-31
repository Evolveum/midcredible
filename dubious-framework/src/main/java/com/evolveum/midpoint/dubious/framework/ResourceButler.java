package com.evolveum.midpoint.dubious.framework;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ConnectorConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3.ConfigurationPropertiesType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class ResourceButler<T> {

	private String id;
	private Context context;
	private T client;

	public ResourceButler(String id, Context context) {
		this(id, context, null);
	}

	public ResourceButler(String id, Context context, T client) {
		Validate.notNull(id);
		Validate.notNull(context);

		this.id = id.replaceAll("[^\\w]", "");
		this.context = context;
		this.client = client;

		context.getButlers().put(this.id, this);
	}

	public String getId() {
		return id;
	}

	public Context getContext() {
		return context;
	}

	public T getClient() {
		if (client != null) {
			return client;
		}

		try {
			client = init();
		} catch (Exception ex) {
			throw new IllegalStateException("Couldn't initialize client", ex);
		}

		return client;
	}

	protected T init() throws Exception {
		return null;
	}

	protected void destroy() throws Exception {

	}

	protected ConfigurationPropertiesType getConfigurationProperties(ResourceType resource) {
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

	protected String getValue(ConfigurationPropertiesType config, String name) {
		return getValue(config, name, getId() + "." + name);
	}

	protected String getValue(ConfigurationPropertiesType config, String name, String propertyName) {
		return getValue(config, new QName(name), propertyName);
	}

	protected String getValue(ConfigurationPropertiesType config, QName name, String propertyName) {
		Context ctx = getContext();
		String value = (String) ctx.getProperties().get(propertyName);
		if (value != null) {
			return value;
		}

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
