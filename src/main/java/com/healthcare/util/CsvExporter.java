package com.healthcare.util;

import com.healthcare.model.Reportable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Utility for exporting domain entities and reportable data to CSV.
 */
public final class CsvExporter {

    private CsvExporter() {}

    /**
     * Converts a list of Reportable entities into CSV format with custom headers.
     * Demonstrates bounded wildcards <? extends Reportable>.
     */
    public static String exportToCsv(List<String> headers, List<? extends Reportable> items) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // Header row
        if (headers != null && !headers.isEmpty()) {
            pw.println(String.join(",", headers));
        }

        // Data rows
        if (items != null) {
            for (Reportable item : items) {
                if (item != null) {
                    pw.println(item.toCsvRow());
                }
            }
        }

        pw.flush();
        return sw.toString();
    }
}
