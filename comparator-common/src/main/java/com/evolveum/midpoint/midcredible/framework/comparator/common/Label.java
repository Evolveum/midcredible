package com.evolveum.midpoint.midcredible.framework.comparator.common;

public class Label {

    private String name;
    private int index;

    public Label(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Label label = (Label) o;

        if (index != label.index) return false;
        return name != null ? name.equals(label.name) : label.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + index;
        return result;
    }
}