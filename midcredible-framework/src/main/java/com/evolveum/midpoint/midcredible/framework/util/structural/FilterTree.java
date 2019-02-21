package com.evolveum.midpoint.midcredible.framework.util.structural;

public class FilterTree<T> {

    private FilterNode<T> root;

    public FilterTree(T rootContent) {
        root = new FilterNode<>();
        root.setContent(rootContent);
    }

    public FilterNode<T> getRoot() {
        return root;
    }

    public void setRoot(FilterNode<T> root) {
        this.root = root;
    }

}
