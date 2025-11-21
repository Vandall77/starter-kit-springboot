package com.example.starter.common.excel;

import com.example.starter.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base service generic untuk import Excel.
 *
 * Konsep:
 * - FE (React / Swing) bisa DnD file Excel -> kirim ke endpoint multipart.
 * - Controller cukup panggil metode di service ini dengan:
 *   - rowMapper: cara baca setiap Row -> DTO/Entity
 *   - batchSaver: cara simpan List<T> ke database (per batch/chunk)
 *
 * Tujuan starter-kit:
 * - Lo tinggal re-use ExcelImportService ini di fitur apa pun (Item, Supplier, dsb).
 * - Behavior DnD sebenarnya di FE, backend cuma handle parsing Excel + save.
 */
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    /**
     * Index baris header (0-based).
     * Misal header ada di baris pertama Excel -> 0.
     */
    public static final int DEFAULT_HEADER_ROW_INDEX = 0;

    /**
     * Default ukuran batch untuk simpan ke DB (biar hemat memori).
     */
    public static final int DEFAULT_BATCH_SIZE = 500;

    /**
     * Import Excel dengan header validation (opsional).
     *
     * @param file            file Excel (MultipartFile dari controller)
     * @param headerRowIndex  index baris header (0-based)
     * @param expectedHeaders urutan nama header yang diharapkan (boleh null/empty kalau tidak mau validasi)
     * @param rowMapper       fungsi convert Row -> T (DTO/Entity)
     * @param batchSaver      fungsi simpan List<T> ke DB (per batch)
     * @param <T>             tipe data yang ingin dihasilkan (DTO/Entity)
     * @return ExcelImportResult berisi summary hasil import
     */
    public <T> ExcelImportResult importExcel(
            MultipartFile file,
            int headerRowIndex,
            List<String> expectedHeaders,
            Function<Row, T> rowMapper,
            Consumer<List<T>> batchSaver
    ) {
        return importExcel(file, headerRowIndex, expectedHeaders, rowMapper, batchSaver, DEFAULT_BATCH_SIZE);
    }

    /**
     * Overload yang paling lengkap (bisa atur batchSize).
     */
    public <T> ExcelImportResult importExcel(
            MultipartFile file,
            int headerRowIndex,
            List<String> expectedHeaders,
            Function<Row, T> rowMapper,
            Consumer<List<T>> batchSaver,
            int batchSize
    ) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "File Excel tidak boleh kosong");
        }

        if (batchSize <= 0) {
            batchSize = DEFAULT_BATCH_SIZE;
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Sheet pertama pada file Excel tidak ditemukan");
            }

            ExcelImportResult result = new ExcelImportResult();
            List<T> buffer = new ArrayList<>();

            // 1) Validasi header (kalau expectedHeaders tidak null/empty)
            if (expectedHeaders != null && !expectedHeaders.isEmpty()) {
                Row headerRow = sheet.getRow(headerRowIndex);
                validateHeaderRow(headerRow, expectedHeaders);
            }

            // 2) Mulai baca data mulai baris setelah header
            int firstDataRowIndex = headerRowIndex + 1;
            int lastRowIndex = sheet.getLastRowNum();

            for (int rowIndex = firstDataRowIndex; rowIndex <= lastRowIndex; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) {
                    // Skip baris kosong (tidak dihitung totalRows)
                    continue;
                }

                result.incrementTotalRows();

                try {
                    T mapped = rowMapper.apply(row);
                    if (mapped != null) {
                        buffer.add(mapped);
                        result.incrementSuccess();
                    } else {
                        result.incrementFailed();
                        // +1 karena kita mau pakai nomor baris 1-based untuk user
                        result.addError(rowIndex + 1, "Row mapper menghasilkan null");
                    }

                    // Simpan per batch
                    if (buffer.size() >= batchSize) {
                        batchSaver.accept(buffer);
                        buffer.clear();
                    }
                } catch (Exception ex) {
                    result.incrementFailed();
                    result.addError(rowIndex + 1, ex.getMessage());
                }
            }

            // Simpan sisa buffer
            if (!buffer.isEmpty()) {
                batchSaver.accept(buffer);
            }

            return result;

        } catch (IOException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Gagal membaca file Excel: " + e.getMessage());
        } catch (ApiException ae) {
            // lempar ulang ApiException supaya tetap ketangkap GlobalExceptionHandler
            throw ae;
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Terjadi kesalahan saat import Excel: " + ex.getMessage());
        }
    }

    /**
     * Overload paling simpel:
     * - Header di baris pertama (index 0)
     * - Tidak pakai validasi header.
     */
    public <T> ExcelImportResult importExcel(
            MultipartFile file,
            Function<Row, T> rowMapper,
            Consumer<List<T>> batchSaver
    ) {
        return importExcel(file, DEFAULT_HEADER_ROW_INDEX, null, rowMapper, batchSaver, DEFAULT_BATCH_SIZE);
    }

    /**
     * Validasi header: pastikan nama kolom sesuai expectedHeaders (urutan dan nama).
     */
    protected void validateHeaderRow(Row headerRow, List<String> expectedHeaders) {
        if (headerRow == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Header Excel tidak ditemukan pada baris yang ditentukan");
        }

        for (int i = 0; i < expectedHeaders.size(); i++) {
            String expected = expectedHeaders.get(i);
            String actual = getStringCellValue(headerRow.getCell(i));

            if (actual == null || !expected.equalsIgnoreCase(actual.trim())) {
                String message = String.format(
                        "Header kolom ke-%d tidak sesuai. Diharapkan: '%s', ditemukan: '%s'",
                        (i + 1),
                        expected,
                        actual
                );
                throw new ApiException(HttpStatus.BAD_REQUEST, message);
            }
        }
    }

    /**
     * Cek apakah satu baris benar-benar kosong (semua cell kosong).
     */
    protected boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        short lastCellNum = row.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getStringCellValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Helper untuk ambil nilai cell sebagai String (aman dipakai di rowMapper).
     *
     * Versi ini pakai switch statement biasa (compatible Java 8),
     * bukan switch expression.
     */
    public String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellType();

        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    // kalau angkanya 123.0 -> jadi "123"
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                // Untuk starter kit, kita coba ambil sebagai String dulu.
                try {
                    return cell.getStringCellValue();
                } catch (Exception ex) {
                    // Kalau bukan string, coba numeric.
                    try {
                        double formulaNumeric = cell.getNumericCellValue();
                        if (formulaNumeric == Math.floor(formulaNumeric)) {
                            return String.valueOf((long) formulaNumeric);
                        }
                        return String.valueOf(formulaNumeric);
                    } catch (Exception e2) {
                        return null;
                    }
                }

            case BLANK:
            case _NONE:
            case ERROR:
            default:
                return null;
        }
    }
}
