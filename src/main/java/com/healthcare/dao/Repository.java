package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;

import java.util.List;
import java.util.Optional;

/**
 * Generic Repository interface defining standard CRUD operations.
 * Demonstrates Generics (<T, ID>) and Java 8+ default methods.
 *
 * @param <T>  Entity model type
 * @param <ID> Unique identifier type
 */
public interface Repository<T, ID> {
    Optional<T> findById(ID id) throws DatabaseException;

    List<T> findAll() throws DatabaseException;

    T save(T entity) throws DatabaseException;

    boolean update(T entity) throws DatabaseException;

    boolean deleteById(ID id) throws DatabaseException;

    /**
     * Default method checking existence of an entity by ID.
     */
    default boolean existsById(ID id) throws DatabaseException {
        return findById(id).isPresent();
    }

    /**
     * Default method returning total record count.
     */
    default int count() throws DatabaseException {
        return findAll().size();
    }
}
