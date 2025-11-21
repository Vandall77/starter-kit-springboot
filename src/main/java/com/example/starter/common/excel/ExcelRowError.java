package com.example.starter.common.excel;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Representasi error per baris Excel.
 * - rowIndex dibuat 1-based supaya sama dengan baris di Excel (baris pertama = 1).
 */
@Data
@AllArgsConstructor
public class ExcelRowError {

    /**
     * Nomor baris di Excel (1-based, termasuk header).
     */
    private int rowIndex;

    /**
     * Pesan error yang menjelaskan kenapa baris gagal diproses.
     */
    private String message;
}
