package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.util.structural.Entity;
import com.evolveum.midpoint.midcredible.framework.util.structural.Label;

import java.util.Map;

public interface Comparator {

    String query();

    String query(Integer resultSet);

    String buildIdentifier(Map<Label, Object> oldRow);

    State compareEntity(String oldEntityId, String newEntityId);

    Entity compareData(Entity oldEntity, Entity newEntity);

}
// Abstract comparatorBase