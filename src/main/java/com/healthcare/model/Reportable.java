package com.healthcare.model;

import java.util.Map;

/**
 * Interface contract for entities that can be summarized into reports/exports.
 */
public interface Reportable {
    String getReportIdentifier();
    String getReportTitle();
    Map<String, Object> toReportMap();

    /**
     * Default method to provide CSV export line.
     */
    default String toCsvRow() {
        return getReportIdentifier() + "," + getReportTitle();
    }
}
