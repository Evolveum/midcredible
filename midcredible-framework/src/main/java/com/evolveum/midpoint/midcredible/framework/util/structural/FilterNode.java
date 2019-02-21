package com.evolveum.midpoint.midcredible.framework.util.structural;

import java.util.List;

public class FilterNode<T> {
    private T content;
    private FilterNode<T> parent;
    private List<FilterNode<T>> children;


    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public FilterNode<T> getParent() {
        return parent;
    }

    public void setParent(FilterNode<T> parent) {
        this.parent = parent;
    }

    public List<FilterNode<T>> getChildren() {
        return children;
    }

    public void setChildren(List<FilterNode<T>> children) {
        this.children = children;
    }
}
