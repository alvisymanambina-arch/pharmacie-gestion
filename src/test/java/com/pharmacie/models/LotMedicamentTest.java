package com.pharmacie.models;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LotMedicamentTest {
    @Test
    void storesBatchTraceabilityFields() {
        LotMedicament lot = new LotMedicament();
        lot.setId(7);
        lot.setMedicamentId(12);
        lot.setNumeroLot("LOT-2026-001");
        lot.setDateExpiration(LocalDate.of(2027, 1, 31));
        lot.setQuantiteInitiale(100);
        lot.setQuantiteDisponible(75);
        lot.setPrixAchat(new BigDecimal("1250.00"));

        assertEquals(7, lot.getId());
        assertEquals(12, lot.getMedicamentId());
        assertEquals("LOT-2026-001", lot.getNumeroLot());
        assertEquals(LocalDate.of(2027, 1, 31), lot.getDateExpiration());
        assertEquals(75, lot.getQuantiteDisponible());
        assertEquals(new BigDecimal("1250.00"), lot.getPrixAchat());
    }
}
