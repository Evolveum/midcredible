package com.evolveum.midpoint.midcredible.comparator.common;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CsvReportPrinter2 implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(CsvReportPrinter2.class);

    private static final String MULTIVALUE_SEPARATOR = ";";

    private CsvPrinterOptions options;

    private CSVPrinter printer;

    private List<String> columns;

    public CsvReportPrinter2(CsvPrinterOptions options) {
        this.options = options;
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
            File file = options.getPath();

            if (file != null) {
                LOG.debug("Path property provided to the printer," +
                        " the output will be directed into the corresponding file: " + file);

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

    public void printCsvHeader(List<String> columnNames) throws IOException {
        LOG.debug("Printing header based on the following column names: {}" + columnNames.toString());

        Collections.sort(columnNames);

        columns = new ArrayList<>();
        columns.add("state");
        columns.addAll(columnNames);

        printer.printRecord(columns.toArray(new String[columns.size()]));
    }

    public synchronized void printCsvRow(Row oldRow, Row newRow) throws IOException {
        if (!options.isPrintEqual() && Objects.deepEquals(oldRow, newRow)) {
            return;
        }

        printRow("old", oldRow);
        printRow("new", newRow);

        printer.flush();
    }

    private void printRow(String state, Row row) throws IOException {
        if (row == null) {
            return;
        }

        String[] result = new String[columns.size()];
        result[0] = state;

        Map<String, List<Object>> attrs = row.getAttributes();
        for (int i = 1; i < columns.size(); i++) {
            List<Object> values = attrs.get(columns.get(i));
            if (values == null || values.isEmpty()) {
                continue;
            }

            result[i] = join(values, MULTIVALUE_SEPARATOR);
        }

        printer.printRecord(result);
    }

    public static String join(final List<?> list, final String separator) {
        if (list == null) {
            return null;
        }

        if (list.isEmpty()) {
            return null;
        }

        Iterator<?> iterator = list.iterator();

        final Object first = iterator.next();
        if (!iterator.hasNext()) {
            return Objects.toString(first, "");
        }

        // two or more elements
        final StringBuilder buf = new StringBuilder(256); // Java default is 16, probably too small
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            final Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }
}
