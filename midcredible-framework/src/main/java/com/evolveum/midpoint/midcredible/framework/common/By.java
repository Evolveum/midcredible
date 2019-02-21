package com.evolveum.midpoint.midcredible.framework.common;

import java.util.List;

public abstract class By<T> extends Component<T> {

    public By(T parent) {
        super(parent);
    }

    public Searchable name() {
//TODO implement
        return null;
    }

    public Searchable oid() {
//TODO implement
        return null;
    }

    public List<Searchable> displayName() {
//TODO implement
        return null;
    }

    public Broker confirm() {
        //TODO implement
        return null;
    }

}
