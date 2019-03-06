package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.util.structural.Attribute;
import com.evolveum.midpoint.midcredible.framework.util.structural.Entity;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CsvReportPrinter implements Closeable {

    private static final char DEFAULT_MV_SEPARATOR = ';';
    private static final Logger LOG = LoggerFactory.getLogger(CsvReportPrinter.class);
    private Boolean isFirst = true;
    private CSVPrinter printer;

    // todo who fill close writer? CsvReportPrinter should be closeable [matus]

    public CsvReportPrinter() {
    }

    @Override
    public void close() throws IOException {
        if (printer != null) {
            printer.close();
        }
    }

    public void setupCsvPrinter(String path) {
        Writer writer;
        try {
            if (path != null && !path.isEmpty()) {
                File file = new File(path);
                file.createNewFile();

                writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            } else {
                writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
            }

            CSVFormat csvFormat = setupCsvFormat();
            csvFormat = csvFormat.withQuoteMode(QuoteMode.ALL);

            this.printer = csvFormat.print(writer);
        } catch (IOException e) {
            LOG.error("Exception while creating a new file: " + e.getLocalizedMessage());

            e.printStackTrace();
        }
    }

    private CSVFormat setupCsvFormat() {
        return CSVFormat.DEFAULT;
    }

    private void printCsvHeader(CSVPrinter printer, List<String> columnNames) throws IOException {
        LOG.debug("Printing header based on the following column names+ " + columnNames.toString());
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

            Attribute attr = entity.getAttrs().get(name);
            if (attr == null || attr.getValues() == null) {
                LOG.debug("Attribute value not present in attribute list for attribute: ", name);
            } else {
                Map<Diff, Collection<Object>> attrValues = attr.getValues();
                attrValues.forEach((diff, objects) -> {
                    int count = 1;
                    objects.forEach(object -> {

                        if (entity.getChange() == State.MODIFIED) {
                            valueString.append(diff.getCharacter());
                        }

                        valueString.append(object != null ? object.toString() : "[null]");
                        if (count < objects.size()) {
                            valueString.append(DEFAULT_MV_SEPARATOR);
                        }
                    });
                });
            }
            
            row[i.get() + 1] = valueString != null ? valueString.toString() : "[null]";
            i.getAndIncrement();
        });

        LOG.trace("Row produced: " + row.toString());
        printer.printRecord(row);
    }
}
