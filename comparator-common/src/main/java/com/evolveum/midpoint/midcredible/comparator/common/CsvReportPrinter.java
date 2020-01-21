package com.evolveum.midpoint.midcredible.comparator.common;

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

/**
 * Merge to {@link CsvReportPrinter2}
 */
@Deprecated
public class CsvReportPrinter implements Closeable {

    private static final char DEFAULT_MV_SEPARATOR = ';';
    private static final Logger LOG = LoggerFactory.getLogger(CsvReportPrinter.class);
    private Boolean isFirst = true;

    private String outPath;
    private Boolean printEqual = false;
    private CSVPrinter printer;

    public CsvReportPrinter() {
    }

    @Override
    public void close() throws IOException {
        if (printer != null) {
            printer.close();
        }
    }

    public void init() {
        Writer writer;
        try {
            if (outPath != null && !outPath.isEmpty()) {
                LOG.debug("Path property provided to the printer," +
                        " the output will be directed into the corresponding file: " + outPath);

                File file = new File(outPath);
                file.createNewFile();

                writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), StandardCharsets.UTF_8);
            } else {
                LOG.debug("No path property provided to the printer," +
                        " directing output to STDOUT");

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

    public synchronized void printCsvRow(List<String> attrNames, Entity entity) throws IOException {
        if (isFirst) {
            printCsvHeader(printer, attrNames);
            isFirst = false;
        }
        printCsvRow(printer, attrNames, entity);
        printer.flush();
    }

    private void printCsvRow(CSVPrinter printer, List<String> attrNames, Entity entity) throws IOException {

        State entityState = entity.getChange();

        if (entityState != null) {

        } else {
            LOG.warn("Unexpected state, state of entity not set. The processed entity id: " + entity.getId());
            return;
        }

        if (printEqual) {
        } else {

            if (entityState == State.EQUAL) {
                LOG.debug("Entity is equal and omitted. The processed entity id: " + entity.getId());
                return;
            }

        }
        String[] row = new String[attrNames.size() + 1];
        attrNames.sort(String::compareTo);

        row[0] = entityState.getCharacter();
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


    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }

    public void setPrintEqual(Boolean printEqual) {
        this.printEqual = printEqual;
    }
}
