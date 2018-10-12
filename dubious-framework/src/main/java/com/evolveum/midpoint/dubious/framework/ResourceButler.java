package com.evolveum.midpoint.dubious.framework;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class ResourceButler<T> {

    private T client;

    public ResourceButler() {
        this(null);
    }

    public ResourceButler(T client) {
        this.client = client;
    }

    public T getClient() {
        return client;
    }

    protected void init() {

    }

    protected void destroy() {

    }
}
