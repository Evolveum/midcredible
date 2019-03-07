package com.evolveum.midpoint.midcredible.framework.cmd;

import com.evolveum.midpoint.midcredible.framework.util.TableComparator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CompareTableAction implements Action<CompareTableOptions> {

    @Override
    public void init(CompareTableOptions opts) throws Exception {

    }

    @Override
    public void execute() throws Exception {
        TableComparator comparison = new TableComparator();
        comparison.compare(false);
    }
}
