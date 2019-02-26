import com.evolveum.midpoint.midcredible.framework.util.Comparator
import com.evolveum.midpoint.midcredible.framework.util.structural.Outcome

class SimpleComparator implements Comparator {

    @Override
    String query() {
        return "select userid, firstname, lastname from table order by userid"
    }

    @Override
    Outcome compare(Map<?, Object> oldRow, Map<?, Object> newRow) {
        return null
    }

}