package com.evolveum.midpoint.midcredible.framework;

import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Context {

	private RestJaxbService midpoint;
	private Map<Object, Object> properties;

	private Map<String, ResourceButler> butlers = new HashMap<>();

	public Context(RestJaxbService midpoint, Map<Object, Object> properties) {
		this.midpoint = midpoint;
		this.properties = properties;
	}

	public RestJaxbService getMidpoint() {
		return midpoint;
	}

	public Map<Object, Object> getProperties() {
		return properties;
	}

	public Map<String, ResourceButler> getButlers() {
		return butlers;
	}
}
