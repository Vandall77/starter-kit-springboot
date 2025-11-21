# Starter Kit Spring Boot (Minimal Auth + Role/Permission)

Versi: v3 (tanpa Flyway, schema dibuat otomatis oleh Hibernate/JPA).

Fitur utama:
- JWT-based authentication (access token)
- Refresh token table (refresh_tokens)
- Role & permission (roles, permissions, user_roles, role_permissions)
- Audit log dengan AOP sederhana (@Auditable)
- Contoh domain `Item` dengan CRUD sederhana
- Seed data via `DataInitializer`: role ADMIN/USER, permissions CRUD, user admin/admin123

## Cara menjalankan

1. Buat database PostgreSQL kosong, misal `starter_kit`.
2. Update konfigurasi koneksi di `src/main/resources/application.yml` (username/password DB).
3. Jalankan:

   ```bash
   mvn spring-boot:run
   ```

4. Cek endpoint:
   - `POST /api/auth/login`
   - `GET /api/auth/me` (dengan header Authorization: Bearer &lt;token&gt;)
   - `GET /api/items` (butuh login)
