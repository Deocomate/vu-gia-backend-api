-- V2__seed_db.sql inserted the `admin` user with a hardcoded bcrypt hash that is NOT
-- `admin123` (it's the well-known Spring Security example hash for "password"), which
-- silently contradicts docs/RUN_AND_SEED.md and prevents DataInitializer's own admin/admin123
-- seeding from ever running (it's a no-op once the username already exists).
-- Reset it to the documented `admin123` password so login works as documented.
UPDATE `users`
SET `password` = '$2a$10$OWC5mhK2S2UD162sPVJwneUrinUJslZM8tYI4gQMSTkvxoOtQH8I.',
    `updated_at` = NOW()
WHERE `username` = 'admin';
