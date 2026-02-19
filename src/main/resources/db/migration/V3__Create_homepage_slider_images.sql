-- Flyway Migration V3: Homepage slider images (admin-managed)
CREATE TABLE IF NOT EXISTS homepage_slider_images (
    id BIGSERIAL PRIMARY KEY,
    image_url VARCHAR(1024) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    alt_text VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_homepage_slider_sort ON homepage_slider_images(sort_order);
