/**
 * Data Access Object (DAO) persistence layer.
 * <p>
 * Implements the Repository pattern using generic contract {@link com.healthcare.dao.Repository}
 * and custom functional interface {@link com.healthcare.dao.RowMapper} for type-safe JDBC result set extraction.
 * Every DAO implementation manages raw JDBC queries with parameterized
 * {@link java.sql.PreparedStatement} instances, try-with-resources resource handling,
 * and HikariCP pooled connection leasing via {@link com.healthcare.util.DBConnection}.
 * </p>
 */
package com.healthcare.dao;
