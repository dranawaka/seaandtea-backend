--liquibase formatted sql

--changeset seaandtea:004-sri-lanka-slider-photos
--comment Replace stock and non-Sri-Lankan slider photos with photographs of the island.
UPDATE homepage_slider_images
SET image_url = '/images/slider/tea-nuwara-eliya.jpg', sort_order = 1, alt_text = 'Tea pluckers near Nuwara Eliya'
WHERE id = 1;

UPDATE homepage_slider_images
SET image_url = '/images/slider/beach-unawatuna.jpg', sort_order = 2, alt_text = 'Unawatuna beach'
WHERE id = 2;

UPDATE homepage_slider_images
SET image_url = '/images/slider/sigiriya.jpg', sort_order = 0, alt_text = 'Sigiriya from Pidurangala'
WHERE id = 3;

UPDATE homepage_slider_images
SET image_url = '/images/slider/yala-elephants.jpg', sort_order = 4, alt_text = 'Sri Lankan elephants'
WHERE id = 4;

INSERT INTO homepage_slider_images (id, image_url, sort_order, alt_text, created_at)
SELECT 5, '/images/slider/ella-nine-arch.jpg', 3, 'Nine Arch Bridge, Ella', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM homepage_slider_images WHERE id = 5);

UPDATE tour_images
SET image_url = '/images/slider/sigiriya.jpg', alt_text = 'Sigiriya rock fortress'
WHERE image_url LIKE '%photo-1548013146%';
