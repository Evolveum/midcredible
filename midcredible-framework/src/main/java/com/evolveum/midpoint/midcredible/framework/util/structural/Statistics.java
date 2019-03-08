package com.evolveum.midpoint.midcredible.framework.util.structural;

import com.evolveum.midpoint.midcredible.framework.ResourceButler;
import com.evolveum.midpoint.midcredible.framework.comparator.common.Attribute;
import com.evolveum.midpoint.midcredible.framework.comparator.common.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class Statistics {

    private final ResourceButler butler;

    private long numberOfRecords;
    private long numberOfInconsistenRecords;
    private Map<Attribute, Map<Diff, Long>> countAttributeChanges;
    private File objectStatDump;
    private Boolean isWriteToFile = false;
    private Boolean isWriteToLog = false;

    private static final Logger LOG = LoggerFactory.getLogger(Statistics.class);


//    public Statistics(FilterTree filterTree, ComparatorImpl comparatorImpl) {
//
//        this.comparatorImpl = comparatorImpl;
//        this.filterTree = filterTree;
//        filter = null;
//    }
//
//    public Statistics(ComparatorImpl comparatorImpl, String nativeFilter) {
//
//        this.comparatorImpl = comparatorImpl;
//        filter = nativeFilter;
//        filterTree = null;
//    }

    public Statistics(ResourceButler butler) {
        this.butler = butler;
    }

    public Statistics toCsv(String path) throws IOException {
        objectStatDump = new File(path);
        if (!objectStatDump.exists()) {
            try {
                objectStatDump.createNewFile();
            } catch (IOException e) {
                LOG.error("Exception while creating a new file: " + e.getLocalizedMessage());
                throw e;
            }
        } else {
            if (!objectStatDump.canWrite()) {
                LOG.error("Can not write into the file specified");
                throw new IllegalArgumentException("Can not write into the file specified");
            }
        }
        isWriteToFile = true;
        return this;
    }

    public Statistics toLog(){
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

    protected void write(Outcome outcome) {

        if (isWriteToFile) {
            writeToFile(outcome);
        }

        if (isWriteToLog) {

        }

    }

    private void writeToFile(Outcome outcome) {


        try (PrintWriter writer = new PrintWriter(objectStatDump)){

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void isWriteToLog(Outcome outcome) {

    }

    private void produceStats() {

        countAttributeChanges.forEach((attribute, diffLongMap) -> {

            countAttributeChanges.get(attribute);

        });
    }

   public ResourceButler confirm(){

        return butler;
   }

}
