package com.evolveum.midpoint.midcredible.framework.util.structural;

import com.evolveum.midpoint.midcredible.framework.util.Diff;

import java.util.Collection;
import java.util.Map;

public class Attribute {

    // TODO

//    private boolean changed;
//    private String originName;
//    private String originOid;
    private String name;
    private Map<Diff, Collection<Object>> values;

    public Attribute(String originName, String originOid, String name) {
//        this.originName = originName;
//        this.originOid = originOid;
        this.name = name;
    }


//    public boolean isChanged() {
//        return changed;
//    }
//
//    public void setChanged(boolean changed) {
//        this.changed = changed;
//    }
//
//    public String getOriginName() {
//        return originName;
//    }
//
//    public void setOriginName(String originName) {
//        this.originName = originName;
//    }
//
//    public String getOriginOid() {
//        return originOid;
//    }
//
//    public void setOriginOid(String originOid) {
//        this.originOid = originOid;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Diff, Collection<Object>> getValues() {
        return values;
    }

    public void setValues(Map<Diff, Collection<Object>> values) {
        this.values = values;
    }


}
