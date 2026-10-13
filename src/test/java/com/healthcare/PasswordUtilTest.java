package com.healthcare;

import com.healthcare.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordUtilTest {

    @Test
    void testPasswordHashing() {
        String adminHash = PasswordUtil.hashPassword("Admin@123");
        String doctorHash = PasswordUtil.hashPassword("Doctor@123");
        String patientHash = PasswordUtil.hashPassword("Patient@123");

        System.out.println("HASH_ADMIN=" + adminHash);
        System.out.println("HASH_DOCTOR=" + doctorHash);
        System.out.println("HASH_PATIENT=" + patientHash);

        assertTrue(PasswordUtil.checkPassword("Admin@123", adminHash));
        assertTrue(PasswordUtil.checkPassword("Doctor@123", doctorHash));
        assertTrue(PasswordUtil.checkPassword("Patient@123", patientHash));
    }
}
