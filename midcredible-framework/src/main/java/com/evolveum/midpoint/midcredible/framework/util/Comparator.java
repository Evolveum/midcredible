package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.util.structural.Identity;
import com.evolveum.midpoint.midcredible.framework.util.structural.Jdbc.Column;

import java.util.List;
import java.util.Map;

public interface Comparator {

    String query();

    State compareIdentity(Identity oldIdentity, Identity newIdentity);

    Identity compareData(Identity oldIdentity, Identity newIdentity);
}
