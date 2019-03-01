package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.util.structural.Entity;
import com.evolveum.midpoint.midcredible.framework.util.structural.Label;

import java.util.Map;

public interface Comparator {

    String query();

    String buildIdentifier(Map<Label, Object> oldRow);

    State compareEntity(Entity oldEntity, Entity newEntity);

    Entity compareData(Entity oldEntity, Entity newEntity);

}
