import com.evolveum.midpoint.midcredible.framework.util.Comparator
import com.evolveum.midpoint.midcredible.framework.util.Diff
import com.evolveum.midpoint.midcredible.framework.util.State
import com.evolveum.midpoint.midcredible.framework.util.structural.Attribute
import com.evolveum.midpoint.midcredible.framework.util.structural.Identity

class SimpleComparator implements Comparator {

    @Override
    String query() {
        // TODO dummy select
        return "SELECT * FROM test.test ORDER BY UID"
    }

    @Override
    State compareIdentity(Identity oldIdentity, Identity newIdentity) {

        String oldUid = oldIdentity.getId()
        String newUid = newIdentity.getId()

        Integer id = oldUid <=> newUid

        if (id == 0) {

            return State.EQUAL
        } else if (id > 0) {

            return State.OLD_AFTER_NEW
        } else {

            return State.OLD_BEFORE_NEW
        }

    }

    @Override
    Identity compareData(Identity oldIdentity, Identity newIdentity) {

        Identity diffIdentity = new Identity(oldIdentity.getId(), new HashMap<String, Attribute>())
        Map<String, Attribute> oldSet = oldIdentity.getAttrs()
        Map<String, Attribute> newSet = newIdentity.getAttrs()

        for (String attrName : oldSet) {
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

                        diffValues = checkAndAddCollection(diffValues, Diff.REMOVE, o)
                    } else {

                        diffValues = checkAndAddCollection(diffValues, Diff.ADD, o)
                    }

                } else {

                    diffValues = checkAndAddCollection(diffValues, Diff.REMOVE, o)
                }

            }

            Attribute diffAttr = new Attribute(attrName)
            diffAttr.setValues(diffValues)
            map = diffIdentity.getAttrs()
            map.put(attrName, diffAttr)
            diffIdentity.setAttrs(map)
            // TODO implement compare directly in attr class ???
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