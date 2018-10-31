package com.evolveum.midpoint.dubious.framework;

import org.apache.commons.lang3.Validate;

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

		this.id = id.replaceAll("^\\w", "");
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
		return client;
	}

	protected void init() throws Exception {

	}

	protected void destroy() throws Exception {

	}
}
