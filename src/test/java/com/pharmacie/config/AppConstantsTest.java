package com.pharmacie.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppConstantsTest {
    @Test
    void databaseConfigurationHasSafeDefaults() {
        assertNotNull(AppConstants.DB_URL);
        assertFalse(AppConstants.DB_URL.isBlank());
        assertNotNull(AppConstants.DB_USER);
        assertNotNull(AppConstants.DB_PASSWORD);
    }
}
