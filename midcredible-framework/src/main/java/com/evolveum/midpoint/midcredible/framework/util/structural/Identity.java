package com.evolveum.midpoint.midcredible.framework.util.structural;

import com.evolveum.midpoint.midcredible.framework.util.State;

import java.util.List;
import java.util.Map;

public class Identity {

    private String uid;
    private State change;
    private Map<String, Attribute> attrs;

    public Identity(String uid) {
        this(uid, null);
    }

    public Identity(String uid, Map<String, Attribute> attrs) {
        this.uid = uid;
        this.attrs = attrs;
    }

    public List<Attribute> setAttributes(Attribute attr) {

        return null;
    }

    public Map<String, Attribute> getAttrs() {
        return attrs;
    }

    public void setChanged(State changed) {
        this.change = changed;
    }

    public State getChange() {

        return change;
    }

    public String getUid() {
        return uid;
    }

}
