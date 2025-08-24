-- Flyway Migration V2: Insert sample data
-- This script inserts sample data for testing and development

-- Insert sample users (password: 'password' - bcrypt hash)
INSERT INTO users (email, password_hash, first_name, last_name, phone, date_of_birth, nationality, is_verified, is_active, role, created_at, updated_at) VALUES
('admin@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'User', '+94112345678', '1990-01-01', 'Sri Lankan', true, true, 'ADMIN', NOW(), NOW()),
('guide1@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Kamal', 'Perera', '+94112345679', '1985-05-15', 'Sri Lankan', true, true, 'GUIDE', NOW(), NOW()),
('guide2@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Nimal', 'Fernando', '+94112345680', '1988-08-20', 'Sri Lankan', true, true, 'GUIDE', NOW(), NOW()),
('tourist1@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Smith', '+1234567890', '1992-03-10', 'American', true, true, 'USER', NOW(), NOW()),
('tourist2@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Emma', 'Johnson', '+1234567891', '1990-07-22', 'British', true, true, 'USER', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Insert sample guides
INSERT INTO guides (user_id, bio, hourly_rate, daily_rate, response_time_hours, is_available, total_tours, average_rating, total_reviews, verification_status, created_at, updated_at) VALUES
(2, 'Experienced tea plantation guide with 10+ years of experience. Specialized in Ceylon tea tours and cultural experiences.', 25.00, 200.00, 2, true, 150, 4.8, 120, 'VERIFIED', NOW(), NOW()),
(3, 'Professional beach and water sports guide. Certified diving instructor with expertise in marine life and coastal tours.', 30.00, 250.00, 4, true, 200, 4.9, 180, 'VERIFIED', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insert sample guide specialties
INSERT INTO guide_specialties (guide_id, specialty, years_experience, created_at) VALUES
(1, 'Tea Tours', 10, NOW()),
(1, 'Cultural Tours', 8, NOW()),
(2, 'Beach Tours', 12, NOW()),
(2, 'Water Sports', 15, NOW()),
(2, 'Marine Life Tours', 10, NOW())
ON CONFLICT DO NOTHING;

-- Insert sample guide languages
INSERT INTO guide_languages (guide_id, language, proficiency_level, created_at) VALUES
(1, 'English', 'FLUENT', NOW()),
(1, 'Sinhala', 'NATIVE', NOW()),
(1, 'Tamil', 'INTERMEDIATE', NOW()),
(2, 'English', 'FLUENT', NOW()),
(2, 'Sinhala', 'NATIVE', NOW()),
(2, 'German', 'INTERMEDIATE', NOW())
ON CONFLICT DO NOTHING;

-- Insert sample tours
INSERT INTO tours (guide_id, title, description, category, duration_hours, max_group_size, price_per_person, instant_booking, secure_payment, languages, highlights, included_items, excluded_items, meeting_point, cancellation_policy, is_active, created_at, updated_at) VALUES
(1, 'Ceylon Tea Experience', 'Discover the world-famous Ceylon tea plantations. Learn about tea processing, taste different varieties, and enjoy the scenic beauty of the hill country.', 'TEA_TOURS', 6, 8, 75.00, true, true, '["English", "Sinhala"]', '["Tea plantation visit", "Tea factory tour", "Tea tasting session", "Scenic viewpoints"]', '["Transportation", "Tea tasting", "Local guide", "Refreshments"]', '["Lunch", "Personal expenses"]', 'Kandy Railway Station', 'Free cancellation up to 24 hours before tour', true, NOW(), NOW()),
(2, 'Beach Adventure & Marine Life', 'Explore pristine beaches, go snorkeling, and discover marine life. Perfect for nature lovers and adventure seekers.', 'BEACH_TOURS', 8, 6, 120.00, true, true, '["English", "Sinhala"]', '["Beach exploration", "Snorkeling", "Marine life observation", "Sunset viewing"]', '["Equipment rental", "Safety gear", "Refreshments", "Local guide"]', '["Lunch", "Personal expenses"]', 'Mirissa Beach', 'Free cancellation up to 48 hours before tour', true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insert sample tour images
INSERT INTO tour_images (tour_id, image_url, is_primary, alt_text, created_at) VALUES
(1, 'https://example.com/tea-tour-1.jpg', true, 'Tea plantation in Kandy', NOW()),
(1, 'https://example.com/tea-tour-2.jpg', false, 'Tea factory interior', NOW()),
(2, 'https://example.com/beach-tour-1.jpg', true, 'Mirissa Beach', NOW()),
(2, 'https://example.com/beach-tour-2.jpg', false, 'Snorkeling in crystal clear water', NOW())
ON CONFLICT DO NOTHING;

-- Insert sample bookings
INSERT INTO bookings (tour_id, tourist_id, guide_id, booking_date, start_time, end_time, number_of_people, total_amount, status, payment_status, special_requests, created_at, updated_at) VALUES
(1, 4, 1, '2024-02-15', '09:00:00', '15:00:00', 2, 150.00, 'CONFIRMED', 'PAID', 'Vegetarian lunch preference', NOW(), NOW()),
(2, 5, 2, '2024-02-20', '08:00:00', '16:00:00', 3, 360.00, 'PENDING', 'PENDING', 'Include photography tips', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insert sample reviews
INSERT INTO reviews (booking_id, tourist_id, guide_id, tour_id, rating, comment, is_verified, created_at) VALUES
(1, 4, 1, 1, 5, 'Amazing experience! The tea plantation was beautiful and our guide was very knowledgeable.', true, NOW())
ON CONFLICT DO NOTHING;

-- Insert sample payments
INSERT INTO payments (booking_id, amount, currency, stripe_payment_intent_id, status, payment_method, created_at, updated_at) VALUES
(1, 150.00, 'USD', 'pi_test_123456', 'SUCCEEDED', 'card', NOW(), NOW()),
(2, 360.00, 'USD', 'pi_test_789012', 'PENDING', 'card', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Insert sample messages
INSERT INTO messages (sender_id, receiver_id, booking_id, message, is_read, created_at) VALUES
(4, 2, 1, 'Hi! I have a question about the tea tour. What should I wear?', false, NOW()),
(2, 4, 1, 'Hello! Comfortable walking shoes and light clothing are recommended. The weather can be cool in the hills.', true, NOW())
ON CONFLICT DO NOTHING;


