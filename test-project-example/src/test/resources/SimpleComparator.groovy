import com.evolveum.midpoint.midcredible.comparator.common.Attribute
import com.evolveum.midpoint.midcredible.comparator.common.Comparator
import com.evolveum.midpoint.midcredible.comparator.common.Diff
import com.evolveum.midpoint.midcredible.comparator.common.Entity
import com.evolveum.midpoint.midcredible.comparator.common.Label
import com.evolveum.midpoint.midcredible.comparator.common.State

class SimpleComparator implements Comparator {

    String ID = "ID"

    @Override
    String query() {
        // TODO dummy select
        return "SELECT * FROM TEST ORDER BY ID"
    }

    @Override
    String query(Integer resultSet) {
       return query()
    }

    @Override
    String buildIdentifier(Map<Label, Object> atomicEntity) {

        for (Label l : atomicEntity.keySet()) {
            if (ID == l.getName()) {
                return atomicEntity.get(l)
            }
        }

        return null
    }
    @Override
    State compareEntity(String oldEntityId, String newEntityId) {

        Integer id = oldEntityId <=> newEntityId

        if (id == 0) {

            return State.EQUAL
        } else if (id > 0) {

            return State.OLD_AFTER_NEW
        } else {

            return State.OLD_BEFORE_NEW
        }

    }

    @Override
    Entity compareData(Entity oldEntity, Entity newEntity) {

        boolean hasChange = false;

        Entity diffIdentity = new Entity(oldEntity.getId(), new HashMap<String, Attribute>())
        Map<String, Attribute> oldSet = oldEntity.getAttrs()
        Map<String, Attribute> newSet = newEntity.getAttrs()

        for (String attrName : oldSet.keySet()) {
            Attribute newAttr = newSet.get(attrName)
            Attribute oldAttr = oldSet.get(attrName)

            Collection newValues = newAttr.getValues().get(Diff.NONE)
            Collection oldValues = oldAttr.getValues().get(Diff.NONE)
            Map<Diff, Collection<Object>> diffValues = new HashMap<>()

            for (Object o : oldValues) {

                if (newValues.contains(o)) {

                    Integer amouthO = Collections.frequency(oldValues, o)
                    Integer amouthN = Collections.frequency(oldValues, o)

                    if (amouthO == amouthN) {

                        diffValues = checkAndAddCollection(diffValues, Diff.EQUALS, o)
                    } else if (amouthO > amouthN) {

                        hasChange = true;
                        diffValues = checkAndAddCollection(diffValues, Diff.REMOVE, o)
                    } else {

                        hasChange = true;
                        diffValues = checkAndAddCollection(diffValues, Diff.ADD, o)
                    }

                } else {

                    hasChange = true;
                    diffValues = checkAndAddCollection(diffValues, Diff.REMOVE, o)
                }

            }

            Attribute diffAttr = new Attribute(attrName)
            diffAttr.setValues(diffValues)
            Map<String, Attribute> map = diffIdentity.getAttrs()
            map.put(attrName, diffAttr)
            diffIdentity.setAttrs(map)
        }

        if (hasChange) {
            diffIdentity.setChanged(State.MODIFIED)
        } else {
            diffIdentity.setChanged(State.EQUAL)
        }

        return diffIdentity
    }

    public Map<Diff, Collection<Object>> checkAndAddCollection(Map<Diff, Collection<Object>> original, Diff diff, Object o) {

        if (original.containsKey(diff)) {
            original.get(diff).add(o)
        } else {
            Collection re = new ArrayList()
            re.add(o)
            original.put(diff, re)
        }

        return original
    }

}