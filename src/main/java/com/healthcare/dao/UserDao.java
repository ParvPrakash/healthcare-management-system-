package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Role;
import com.healthcare.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao extends Repository<User, Integer> {
    Optional<User> findByEmail(String email) throws DatabaseException;
    List<User> findByRole(Role role, int limit, int offset) throws DatabaseException;
    int countByRole(Role role) throws DatabaseException;
    List<User> searchUsers(String query, Role role, int limit, int offset) throws DatabaseException;
    int countSearchUsers(String query, Role role) throws DatabaseException;
    void updateStatus(int id, String status) throws DatabaseException;
    void recordFailedLogin(String email) throws DatabaseException;
    void resetFailedLogins(String email) throws DatabaseException;
    void updatePassword(int id, String newPasswordHash) throws DatabaseException;
}
