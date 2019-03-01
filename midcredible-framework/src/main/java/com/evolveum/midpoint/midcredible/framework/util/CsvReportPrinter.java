package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.util.structural.Attribute;
import com.evolveum.midpoint.midcredible.framework.util.structural.Entity;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CsvReportPrinter {

    private static final char DEFAULT_MV_SEPARATOR = ';';
    private static final Logger LOG = LoggerFactory.getLogger(CsvReportPrinter.class);
    private Boolean isFirst = true;
    private CSVPrinter printer;

    public CsvReportPrinter(String path) {
        try {
            printer = setupCsvPrinter(path);
        } catch (IOException e) {
            LOG.error("Exception while creating a new file: " + e.getLocalizedMessage());
            //TODO handle
        }
    }


    public CSVPrinter setupCsvPrinter(String path) throws IOException {
        Writer writer = null;
        if (path != null && !path.isEmpty()) {

            File file = new File(path);
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);

        } else {
            writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
        }

        CSVFormat csvFormat = setupCsvFormat();

        return csvFormat.print(writer);
    }

    private CSVFormat setupCsvFormat() {
        return CSVFormat.DEFAULT;
    }

    private void printCsvHeader(CSVPrinter printer, List<String> columnNames) throws IOException {
      //TODO set to debug
        LOG.info("Printing header based on the following column names+ "+ columnNames.toString());
        columnNames.sort(String::compareTo);
        int columns = columnNames.size();
        String[] header = new String[columns + 1];

        header[0] = "state";

        for (int i = 0; i < columns; i++) {
            header[i + 1] = columnNames.get(i);
        }

        printer.printRecord(header);
    }

    public void printCsvRow(List<String> attrNames, Entity entity) throws IOException {
        if (isFirst) {
            printCsvHeader(printer, attrNames);
            isFirst = false;
        }
        printCsvRow(printer, attrNames, entity);
       printer.flush();
    }

    private void printCsvRow(CSVPrinter printer, List<String> attrNames, Entity entity) throws IOException {

        if (entity.getChange() == null && entity.getChange() == State.EQUAL) {
            return;
        }
        String[] row = new String[attrNames.size() + 1];
        attrNames.sort(String::compareTo);

        row[0] = entity.getChange().getCharacter();
        AtomicInteger i = new AtomicInteger();
        attrNames.forEach(name -> {
            StringBuilder valueString = new StringBuilder();
            // TODO
           // LOG.info("Attribute to be printed: "+ name);
            Attribute attr = entity.getAttrs().get(name);
            Map<Diff, Collection<Object>> attrValues = attr.getValues();
            if (attrValues==null){

                LOG.error("Attribute not present in attribute list");
            }


            attrValues.forEach((diff, objects) -> {
                int count = 1;
                objects.forEach(object -> {

                    if (entity.getChange() == State.MODIFIED) {
                        valueString.append(diff.getCharacter());
                    }

                    valueString.append(object != null ? object.toString() : "[null]");
                    LOG.info("The value string: "+valueString.toString());
                    if (count < objects.size()) {
                        valueString.append(DEFAULT_MV_SEPARATOR);
                    }
                });
            });

            row[i.get() + 1] = valueString != null ? valueString.toString() : "[null]";
            i.getAndIncrement();
        });

        for(int j = 0; j <row.length; j++){
            LOG.info("The values in row: "+row[j]);

        }
        printer.printRecord(row);
    }
}
