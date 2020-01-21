package com.evolveum.midpoint.midcredible.comparator.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Row {

    private String uid;

    private Map<String, List<Object>> attributes = new HashMap<>();

    public Row(String uid, Map<String, List<Object>> attributes) {
        this.uid = uid;
        this.attributes = attributes;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, List<Object>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<Object>> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Row row = (Row) o;

        if (uid != null ? !uid.equals(row.uid) : row.uid != null) return false;
        return attributes != null ? attributes.equals(row.attributes) : row.attributes == null;
    }

    @Override
    public int hashCode() {
        int result = uid != null ? uid.hashCode() : 0;
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }
}
