package com.healthcare.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Functional Interface for mapping rows of a JDBC ResultSet to domain model objects.
 *
 * @param <T> Domain model type
 */
@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(ResultSet rs) throws SQLException;
}
