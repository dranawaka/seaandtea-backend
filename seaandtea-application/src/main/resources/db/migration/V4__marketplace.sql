-- Marketplace fields: destination search, daily departure, and wishlists.

ALTER TABLE tours ADD COLUMN IF NOT EXISTS destination VARCHAR(100);
ALTER TABLE tours ADD COLUMN IF NOT EXISTS departure_time TIME;

UPDATE tours
SET destination = CASE
    WHEN meeting_point ILIKE '%Trincomalee%' THEN 'Trincomalee'
    WHEN meeting_point ILIKE '%Nuwara Eliya%' THEN 'Nuwara Eliya'
    WHEN meeting_point ILIKE '%Sigiriya%' THEN 'Sigiriya'
    WHEN meeting_point ILIKE '%Colombo%' THEN 'Colombo'
    WHEN meeting_point ILIKE '%Bentota%' THEN 'Bentota'
    WHEN meeting_point ILIKE '%Mirissa%' THEN 'Mirissa'
    WHEN meeting_point ILIKE '%Kandy%' THEN 'Kandy'
    WHEN meeting_point ILIKE '%Galle%' THEN 'Galle'
    WHEN meeting_point ILIKE '%Ella%' THEN 'Ella'
    WHEN meeting_point ILIKE '%Yala%' THEN 'Yala'
    ELSE 'Colombo'
END
WHERE destination IS NULL OR btrim(destination) = '';

UPDATE tours
SET departure_time = TIME '09:00:00'
WHERE departure_time IS NULL;

ALTER TABLE tours ALTER COLUMN destination SET DEFAULT 'Colombo';
ALTER TABLE tours ALTER COLUMN departure_time SET DEFAULT TIME '09:00:00';
ALTER TABLE tours ALTER COLUMN destination SET NOT NULL;
ALTER TABLE tours ALTER COLUMN departure_time SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_tours_destination ON tours(destination);

CREATE TABLE IF NOT EXISTS wishlists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tour_id BIGINT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_wishlists_user_tour UNIQUE (user_id, tour_id)
);

CREATE INDEX IF NOT EXISTS idx_wishlists_user_id ON wishlists(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_tour_date ON bookings(tour_id, booking_date);
