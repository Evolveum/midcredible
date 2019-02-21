package com.evolveum.midpoint.midcredible.framework.util.structural;

import com.evolveum.midpoint.midcredible.framework.util.Comparator;
import com.evolveum.midpoint.midcredible.framework.util.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Statistics {

    private final FilterTree filterTree;
    private final String filter;
    private final Comparator comparator;
    private long numberOfRecords;
    private long numberOfInconsistenRecords;
    private Map<Attribute, Map<Diff, Long>> countAttributeChanges;
    private File statDump;
    private Boolean isWriteToFile = false;
    private Boolean isWriteToLog = false;

    private static final Logger LOG = LoggerFactory.getLogger(Statistics.class);


    public Statistics(FilterTree filterTree, Comparator comparator) {

        this.comparator = comparator;
        this.filterTree = filterTree;
        filter = null;
    }

    public Statistics(Comparator comparator, String nativeFilter) {

        this.comparator = comparator;
        filter = nativeFilter;
        filterTree = null;
    }

    public Statistics(Comparator comparator) {

        this(comparator, null);
    }

    public Statistics toCsv(String path) throws IOException {
        statDump = new File(path);
        if (!statDump.exists()) {
            try {
                statDump.createNewFile();
            } catch (IOException e) {
                LOG.error("Exception while creating a new file: " + e.getLocalizedMessage());
                throw e;
            }
        } else {
            if (!statDump.canWrite()) {
                LOG.error("Can not write into the file specified");
                throw new IllegalArgumentException("Can not write into the file specified");
            }
        }
        isWriteToFile = true;
        return this;
    }

    public Statistics toLog(String path) throws IOException {
        isWriteToLog = true;
        return this;
    }

//    public String getStats(){
//        StringBuilder stats = new StringBuilder("Comparison statistics: ").append(System.lineSeparator());
//        stats.append("Total number of records: ").append(numberOfRecords).append(System.lineSeparator());
//
//        Float percentil = Float.valueOf((numberOfInconsistenRecords / numberOfRecords) * 100);
//        stats.append("Number of inconsistent records: ").append(numberOfInconsistenRecords).append(System.lineSeparator());
//        stats.append("Percentage of inconsistent records compared to total records: ").append(percentil).append(System.lineSeparator());
//
//        for (Attribute attribute:countAttributeChanges.keySet()){
//            Map numberOfChanges = countAttributeChanges.get(attribute);
//
//            numberOfChanges.keySet().forEach(diff -> {
//               Long number = (Long) numberOfChanges.get(diff);
//               stats.append("Attribtue name: ").append(attribute.getName()).append(System.lineSeparator());
//                stats.append("Action: ").append();
//                stats.append("Percentage of inconsistent records compared to total records: ").append(percentil).append(System.lineSeparator());
//
//            });
//        }
//
//        return stats.toString();
//    }

    protected void write(ResultSet resultSet) {

        if (isWriteToFile) {
            writeToFile(resultSet);
        }

        if (isWriteToLog) {

        }

    }

    private void writeToFile(ResultSet resultSet) {

    }

    private void isWriteToLog(ResultSet resultSet) {

    }

    private void produceStats() {

        countAttributeChanges.forEach((attribute, diffLongMap) -> {

            countAttributeChanges.get(attribute);

        });
    }

    public void execute() {

    }

}
