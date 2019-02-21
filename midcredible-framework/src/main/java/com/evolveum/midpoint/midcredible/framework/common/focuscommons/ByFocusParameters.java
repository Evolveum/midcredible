package com.evolveum.midpoint.midcredible.framework.common.focuscommons;

import com.evolveum.midpoint.midcredible.framework.ResourceButler;
import com.evolveum.midpoint.midcredible.framework.common.By;

public class ByFocusParameters<T> extends By<T> {
    public ByFocusParameters(T parent) {
        super(parent);
    }

    public ByShadowParameters projection(ResourceButler targetResourceButler) {

        return new ByShadowParameters(this, targetResourceButler);
    }

}
