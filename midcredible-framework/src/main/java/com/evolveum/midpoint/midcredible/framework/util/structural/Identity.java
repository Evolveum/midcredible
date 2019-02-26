package com.evolveum.midpoint.midcredible.framework.util.structural;

import com.evolveum.midpoint.midcredible.framework.util.State;

import java.util.List;

public class Identity {

    private String uid;
    private State change;
    private List<Attribute> attrs;

    public Identity(String uid) {
        this(uid, null);
    }

    public Identity(String uid, List<Attribute> attrs) {
        this.uid = uid;
        this.attrs = attrs;
    }

    public List<Attribute> setAttributes(Attribute attr) {

        return null;
    }

    public void setHasChanged(State changed) {
        this.change = changed;
    }

    public State getChange() {

        return change;
    }

}
