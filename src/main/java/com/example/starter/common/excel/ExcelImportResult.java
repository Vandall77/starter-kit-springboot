package com.example.starter.common.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Hasil proses import Excel (summary).
 * Dipakai untuk response ke FE supaya bisa ditampilkan:
 * - total data yang dibaca
 * - berapa yang sukses
 * - berapa yang gagal
 * - detail error per baris (jika ada)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResult {

    /**
     * Total baris data yang diproses (tidak termasuk header).
     */
    private int totalRows;

    /**
     * Jumlah baris yang berhasil di-convert + disimpan.
     */
    private int successCount;

    /**
     * Jumlah baris yang gagal diproses.
     */
    private int failedCount;

    /**
     * List error per baris (opsional).
     */
    @Builder.Default
    private List<ExcelRowError> errors = new ArrayList<>();

    public void incrementSuccess() {
        this.successCount++;
    }

    public void incrementFailed() {
        this.failedCount++;
    }

    public void incrementTotalRows() {
        this.totalRows++;
    }

    public void addError(int rowIndex, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new ExcelRowError(rowIndex, message));
    }
}
