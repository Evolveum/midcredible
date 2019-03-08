package com.evolveum.midpoint.midcredible.comparator.common;

import java.util.Map;

public interface Comparator {

    String query();

    String query(Integer resultSet);

    String buildIdentifier(Map<Label, Object> oldRow);

    State compareEntity(String oldEntityId, String newEntityId);

    Entity compareData(Entity oldEntity, Entity newEntity);

}
// Abstract comparatorBase