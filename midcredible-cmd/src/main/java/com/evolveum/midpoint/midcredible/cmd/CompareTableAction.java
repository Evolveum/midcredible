package com.evolveum.midpoint.midcredible.cmd;


import com.evolveum.midpoint.midcredible.comparator.table.CompareTableOptions;
import com.evolveum.midpoint.midcredible.comparator.table.TableComparator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CompareTableAction implements Action<CompareTableOptions> {

    private CompareTableOptions options;

    @Override
    public void init(CompareTableOptions opts) throws Exception {
        this.options = opts;
    }

    @Override
    public void execute() throws Exception {
        new TableComparator(options).execute();
    }
}
