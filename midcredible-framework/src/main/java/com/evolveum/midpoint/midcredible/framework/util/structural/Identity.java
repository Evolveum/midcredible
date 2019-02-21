package com.evolveum.midpoint.midcredible.framework.util.structural;

import java.util.List;

public class Identity {

    private String uid;
    private String name;
    private String origin;
    private Boolean changed;
    private List<Attribute> attrs;

    public Identity(String uid, String origin) {
        this(uid, uid, origin, null);
    }

    public Identity(String uid, String name, String origin) {
        this(uid, name, origin, null);
    }

    public Identity(String uid, String name, String origin, List<Attribute> attrs) {
        this.uid = uid;
        this.name = name;
        this.attrs = attrs;
        this.origin = origin;
    }

    public List<Attribute> setAttributes(Attribute attr) {

        return null;
    }

    public void setHasChanged(Boolean changed) {
        this.changed = changed;
    }

    public Boolean hasChanged() {

        return changed;
    }

}
