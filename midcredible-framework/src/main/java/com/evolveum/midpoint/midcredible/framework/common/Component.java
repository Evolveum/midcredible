package com.evolveum.midpoint.midcredible.framework.common;

public class Component<T> {

    private T parent;

    public Component(T parent) {
        this.parent = parent;
    }

    public T and() {
        return parent;
    }

    public T getParent() {
        return parent;
    }
}
