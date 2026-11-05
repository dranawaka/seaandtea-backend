-- Flyway Migration V2: Insert sample data
-- This script inserts sample data for testing and development
-- 
-- DEFAULT PASSWORD FOR ALL USERS: 'password'
-- BCrypt hash: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
--
-- TEST ACCOUNTS:
-- Admin: admin@seaandtea.com / password
-- Guides: guide1@seaandtea.com, guide2@seaandtea.com, guide3@seaandtea.com, guide4@seaandtea.com / password
-- Tourists: tourist1@example.com, tourist2@example.com, tourist3@example.com, tourist4@example.com / password

-- Insert sample users (password: 'password' - bcrypt hash)
INSERT INTO users (email, password_hash, first_name, last_name, phone, date_of_birth, nationality, is_verified, is_active, role, created_at, updated_at) VALUES
-- Admin User
('admin@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'User', '+94112345678', '1990-01-01', 'Sri Lankan', true, true, 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Guide Users
('guide1@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Kamal', 'Perera', '+94112345679', '1985-05-15', 'Sri Lankan', true, true, 'GUIDE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('guide2@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Nimal', 'Fernando', '+94112345680', '1988-08-20', 'Sri Lankan', true, true, 'GUIDE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('guide3@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Priya', 'Jayawardana', '+94112345681', '1990-12-05', 'Sri Lankan', true, true, 'GUIDE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('guide4@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Rajesh', 'Silva', '+94112345682', '1983-03-18', 'Sri Lankan', true, true, 'GUIDE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Tourist Users
('tourist1@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Smith', '+1234567890', '1992-03-10', 'American', true, true, 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('tourist2@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Emma', 'Johnson', '+1234567891', '1990-07-22', 'British', true, true, 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('tourist3@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Sophie', 'Mueller', '+49123456789', '1994-09-14', 'German', true, true, 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('tourist4@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Hiroshi', 'Tanaka', '+81123456789', '1987-11-30', 'Japanese', true, true, 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- Insert sample guides
INSERT INTO guides (user_id, bio, hourly_rate, daily_rate, response_time_hours, is_available, total_tours, average_rating, total_reviews, verification_status, created_at, updated_at) VALUES
(2, 'Experienced tea plantation guide with 10+ years of experience. Specialized in Ceylon tea tours and cultural experiences in the beautiful hill country of Sri Lanka. Passionate about sharing the rich history and tradition of Ceylon tea.', 25.00, 200.00, 2, true, 150, 4.8, 120, 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Professional beach and water sports guide. Certified diving instructor with expertise in marine life and coastal tours. Specializes in snorkeling, diving, and whale watching along Sri Lankas stunning coastline.', 30.00, 250.00, 4, true, 200, 4.9, 180, 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Cultural heritage specialist and wildlife enthusiast. Expert in ancient temples, archaeological sites, and national park safaris. Fluent in multiple languages with deep knowledge of Sri Lankan history and traditions.', 28.00, 220.00, 3, true, 180, 4.7, 145, 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Adventure tourism guide specializing in hiking, trekking, and mountain climbing. Experienced in leading groups through Sri Lankas diverse landscapes including rainforests, mountains, and coastal areas.', 35.00, 280.00, 6, true, 95, 4.9, 78, 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample guide specialties
INSERT INTO guide_specialties (guide_id, specialty, years_experience, created_at) VALUES
-- Guide 1 (Kamal) - Tea & Cultural Tours
(1, 'Tea Tours', 10, CURRENT_TIMESTAMP),
(1, 'Cultural Tours', 8, CURRENT_TIMESTAMP),
(1, 'Hill Country Tours', 12, CURRENT_TIMESTAMP),

-- Guide 2 (Nimal) - Beach & Water Sports
(2, 'Beach Tours', 12, CURRENT_TIMESTAMP),
(2, 'Water Sports', 15, CURRENT_TIMESTAMP),
(2, 'Marine Life Tours', 10, CURRENT_TIMESTAMP),
(2, 'Whale Watching', 8, CURRENT_TIMESTAMP),

-- Guide 3 (Priya) - Cultural & Wildlife
(3, 'Cultural Tours', 6, CURRENT_TIMESTAMP),
(3, 'Wildlife Safaris', 5, CURRENT_TIMESTAMP),
(3, 'Temple Tours', 7, CURRENT_TIMESTAMP),
(3, 'Archaeological Sites', 4, CURRENT_TIMESTAMP),

-- Guide 4 (Rajesh) - Adventure & Trekking
(4, 'Adventure Tours', 8, CURRENT_TIMESTAMP),
(4, 'Hiking & Trekking', 12, CURRENT_TIMESTAMP),
(4, 'Mountain Climbing', 10, CURRENT_TIMESTAMP),
(4, 'Nature Photography', 6, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample guide languages
INSERT INTO guide_languages (guide_id, language, proficiency_level, created_at) VALUES
-- Guide 1 (Kamal) Languages
(1, 'English', 'FLUENT', CURRENT_TIMESTAMP),
(1, 'Sinhala', 'NATIVE', CURRENT_TIMESTAMP),
(1, 'Tamil', 'INTERMEDIATE', CURRENT_TIMESTAMP),

-- Guide 2 (Nimal) Languages
(2, 'English', 'FLUENT', CURRENT_TIMESTAMP),
(2, 'Sinhala', 'NATIVE', CURRENT_TIMESTAMP),
(2, 'German', 'INTERMEDIATE', CURRENT_TIMESTAMP),
(2, 'French', 'BASIC', CURRENT_TIMESTAMP),

-- Guide 3 (Priya) Languages
(3, 'English', 'FLUENT', CURRENT_TIMESTAMP),
(3, 'Sinhala', 'NATIVE', CURRENT_TIMESTAMP),
(3, 'Tamil', 'FLUENT', CURRENT_TIMESTAMP),
(3, 'Hindi', 'INTERMEDIATE', CURRENT_TIMESTAMP),

-- Guide 4 (Rajesh) Languages
(4, 'English', 'FLUENT', CURRENT_TIMESTAMP),
(4, 'Sinhala', 'NATIVE', CURRENT_TIMESTAMP),
(4, 'Japanese', 'INTERMEDIATE', CURRENT_TIMESTAMP),
(4, 'Spanish', 'BASIC', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample tours
INSERT INTO tours (guide_id, title, description, category, duration_hours, max_group_size, price_per_person, instant_booking, secure_payment, languages, highlights, included_items, excluded_items, meeting_point, cancellation_policy, is_active, created_at, updated_at) VALUES
-- Tea Tours
(1, 'Ceylon Tea Experience', 'Discover the world-famous Ceylon tea plantations. Learn about tea processing, taste different varieties, and enjoy the scenic beauty of the hill country. Visit traditional tea factories and meet local tea pluckers.', 'TEA_TOURS', 6, 8, 75.00, true, true, '["English", "Sinhala"]', '["Tea plantation visit", "Tea factory tour", "Tea tasting session", "Scenic viewpoints", "Local tea plucker interaction"]', '["Transportation", "Tea tasting", "Local guide", "Refreshments", "Factory visit"]', '["Lunch", "Personal expenses", "Souvenirs"]', 'Kandy Railway Station', 'Free cancellation up to 24 hours before tour', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(1, 'Nuwara Eliya Tea Estate Tour', 'Explore the picturesque tea estates of Nuwara Eliya, known as Little England. Experience colonial architecture, cool climate, and premium tea gardens.', 'TEA_TOURS', 8, 6, 95.00, true, true, '["English", "Sinhala", "Tamil"]', '["Premium tea estate visit", "Colonial bungalow tour", "High-quality tea tasting", "Gregory Lake visit"]', '["Transportation", "Tea tasting", "Guide", "Entrance fees"]', '["Meals", "Personal expenses"]', 'Nuwara Eliya Town Center', 'Free cancellation up to 48 hours before tour', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Beach Tours
(2, 'Beach Adventure & Marine Life', 'Explore pristine beaches, go snorkeling, and discover marine life. Perfect for nature lovers and adventure seekers. Includes dolphin and whale watching opportunities.', 'BEACH_TOURS', 8, 6, 120.00, true, true, '["English", "Sinhala"]', '["Beach exploration", "Snorkeling", "Marine life observation", "Sunset viewing", "Dolphin spotting"]', '["Equipment rental", "Safety gear", "Refreshments", "Local guide", "Boat ride"]', '["Lunch", "Personal expenses", "Underwater photography"]', 'Mirissa Beach', 'Free cancellation up to 48 hours before tour', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(2, 'Unawatuna Coral Garden Snorkeling', 'Discover the vibrant coral reefs and tropical fish at Unawatuna Bay. Perfect for beginners and experienced snorkelers alike.', 'BEACH_TOURS', 4, 8, 65.00, true, true, '["English", "Sinhala", "German"]', '["Coral reef exploration", "Tropical fish viewing", "Beach relaxation", "Swimming"]', '["Snorkeling equipment", "Life jackets", "Guide", "Refreshments"]', '["Meals", "Transportation", "Personal expenses"]', 'Unawatuna Beach', 'Free cancellation up to 24 hours before tour', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Cultural Tours
(3, 'Ancient Kingdoms Cultural Tour', 'Journey through Sri Lankas ancient kingdoms visiting UNESCO World Heritage sites including Sigiriya, Dambulla, and Polonnaruwa.', 'CULTURAL_TOURS', 10, 4, 150.00, false, true, '["English", "Sinhala", "Tamil"]', '["Sigiriya Rock Fortress", "Dambulla Cave Temple", "Ancient city ruins", "Traditional village visit"]', '["Transportation", "Entrance fees", "Local guide", "Traditional lunch"]', '["Personal expenses", "Tips", "Souvenirs"]', 'Dambulla Golden Temple', 'Free cancellation up to 72 hours before tour', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(3, 'Kandy Temple & Cultural Show', 'Experience the spiritual heart of Sri Lanka with visits to sacred temples and traditional cultural performances in the hill capital.', 'CULTURAL_TOURS', 6, 10, 80.00, true, true, '["English", "Sinhala", "Tamil", "Hindi"]', '["Temple of the Tooth Relic", "Traditional dance show", "Royal Botanical Gardens", "Local market visit"]', '["Transportation", "Entrance fees", "Cultural show tickets", "Guide"]', '["Meals", "Personal expenses", "Photography fees"]', 'Kandy City Center', 'Free cancellation up to 24 hours before tour', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Adventure Tours
(4, 'Adams Peak Sunrise Trek', 'Challenge yourself with the sacred pilgrimage to Adams Peak. Witness breathtaking sunrise views from 2,243 meters above sea level.', 'ADVENTURE_TOURS', 12, 6, 110.00, false, true, '["English", "Sinhala"]', '["Sacred pilgrimage route", "Sunrise viewing", "Mountain climbing", "Religious significance"]', '["Professional guide", "Safety equipment", "Flashlights", "Emergency kit"]', '["Meals", "Accommodation", "Personal expenses"]', 'Dalhousie Town', 'No cancellation once trek begins', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(4, 'Ella Rock Hiking Adventure', 'Hike through tea plantations and cloud forests to reach Ella Rock summit. Experience stunning panoramic views of the hill country.', 'ADVENTURE_TOURS', 6, 8, 85.00, true, true, '["English", "Sinhala", "Japanese"]', '["Hill country hiking", "Tea plantation walk", "Summit views", "Nine Arch Bridge visit"]', '["Professional guide", "Hiking equipment", "Refreshments", "Transportation"]', '["Meals", "Personal expenses", "Tips"]', 'Ella Railway Station', 'Free cancellation up to 48 hours before tour', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Wildlife Tours
(3, 'Yala National Park Safari', 'Embark on an exciting wildlife safari in Yala National Park. Spot leopards, elephants, and exotic birds in their natural habitat.', 'WILDLIFE_TOURS', 8, 6, 135.00, true, true, '["English", "Sinhala", "Tamil"]', '["Leopard spotting", "Elephant herds", "Bird watching", "Natural landscapes"]', '["Safari jeep", "Professional tracker", "Park entrance", "Refreshments"]', '["Meals", "Accommodation", "Personal expenses"]', 'Yala National Park Entrance', 'Free cancellation up to 48 hours before tour', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Food Tours
(1, 'Authentic Sri Lankan Cuisine Tour', 'Discover the rich flavors of Sri Lankan cuisine. Visit local markets, cooking demonstrations, and taste authentic dishes.', 'FOOD_TOURS', 5, 8, 70.00, true, true, '["English", "Sinhala"]', '["Local market visit", "Cooking demonstration", "Spice garden tour", "Traditional meal"]', '["All food tastings", "Cooking class", "Recipe book", "Transportation"]', '["Personal expenses", "Additional drinks"]', 'Kandy Market Square', 'Free cancellation up to 24 hours before tour', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample tour images
INSERT INTO tour_images (tour_id, image_url, is_primary, alt_text, created_at) VALUES
-- Ceylon Tea Experience (Tour 1)
(1, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/tea_plantation_kandy.jpg', true, 'Tea plantation in Kandy hills', CURRENT_TIMESTAMP),
(1, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/tea_factory_interior.jpg', false, 'Tea factory processing area', CURRENT_TIMESTAMP),
(1, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/tea_tasting_session.jpg', false, 'Tea tasting with local guide', CURRENT_TIMESTAMP),

-- Nuwara Eliya Tea Estate (Tour 2)
(2, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/nuwara_eliya_estate.jpg', true, 'Nuwara Eliya tea estate panorama', CURRENT_TIMESTAMP),
(2, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/colonial_bungalow.jpg', false, 'Colonial tea estate bungalow', CURRENT_TIMESTAMP),

-- Beach Adventure (Tour 3)
(3, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/mirissa_beach.jpg', true, 'Mirissa Beach coastline', CURRENT_TIMESTAMP),
(3, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/snorkeling_underwater.jpg', false, 'Snorkeling in crystal clear water', CURRENT_TIMESTAMP),
(3, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/dolphin_watching.jpg', false, 'Dolphins near Mirissa', CURRENT_TIMESTAMP),

-- Unawatuna Snorkeling (Tour 4)
(4, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/unawatuna_coral.jpg', true, 'Coral reef at Unawatuna', CURRENT_TIMESTAMP),
(4, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/tropical_fish.jpg', false, 'Tropical fish in coral garden', CURRENT_TIMESTAMP),

-- Ancient Kingdoms Tour (Tour 5)
(5, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/sigiriya_rock.jpg', true, 'Sigiriya Rock Fortress', CURRENT_TIMESTAMP),
(5, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/dambulla_caves.jpg', false, 'Dambulla Cave Temple interior', CURRENT_TIMESTAMP),

-- Kandy Cultural Tour (Tour 6)
(6, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/temple_tooth_relic.jpg', true, 'Temple of the Tooth Relic', CURRENT_TIMESTAMP),
(6, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/kandyan_dancers.jpg', false, 'Traditional Kandyan dancers', CURRENT_TIMESTAMP),

-- Adams Peak Trek (Tour 7)
(7, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/adams_peak_sunrise.jpg', true, 'Sunrise view from Adams Peak', CURRENT_TIMESTAMP),
(7, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/pilgrimage_trail.jpg', false, 'Pilgrimage trail to summit', CURRENT_TIMESTAMP),

-- Ella Rock Hiking (Tour 8)
(8, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/ella_rock_view.jpg', true, 'Panoramic view from Ella Rock', CURRENT_TIMESTAMP),
(8, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/nine_arch_bridge.jpg', false, 'Nine Arch Bridge in Ella', CURRENT_TIMESTAMP),

-- Yala Safari (Tour 9)
(9, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/yala_leopard.jpg', true, 'Leopard in Yala National Park', CURRENT_TIMESTAMP),
(9, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/elephant_herd.jpg', false, 'Elephant herd at waterhole', CURRENT_TIMESTAMP),

-- Cuisine Tour (Tour 10)
(10, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/sri_lankan_feast.jpg', true, 'Traditional Sri Lankan feast', CURRENT_TIMESTAMP),
(10, 'https://res.cloudinary.com/do0x8z2ju/image/upload/v1733061234/sample_tours/spice_market.jpg', false, 'Local spice market', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample bookings
INSERT INTO bookings (tour_id, tourist_id, guide_id, booking_date, start_time, end_time, number_of_people, total_amount, status, payment_status, special_requests, created_at, updated_at) VALUES
-- Confirmed bookings
(1, 6, 1, '2024-02-15', '09:00:00', '15:00:00', 2, 150.00, 'CONFIRMED', 'PAID', 'Vegetarian lunch preference', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 7, 2, '2024-02-20', '08:00:00', '16:00:00', 3, 360.00, 'CONFIRMED', 'PAID', 'Include photography tips', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 8, 3, '2024-02-25', '07:00:00', '17:00:00', 2, 300.00, 'CONFIRMED', 'PAID', 'Early morning pickup preferred', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 9, 4, '2024-03-01', '22:00:00', '10:00:00', 1, 110.00, 'CONFIRMED', 'PAID', 'First time hiking, need guidance', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Pending bookings
(2, 6, 1, '2024-03-05', '08:00:00', '16:00:00', 4, 380.00, 'PENDING', 'PENDING', 'Group of friends, need larger vehicle', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 7, 2, '2024-03-10', '09:00:00', '13:00:00', 2, 130.00, 'PENDING', 'PENDING', 'Celebrating anniversary', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 8, 3, '2024-03-15', '10:00:00', '16:00:00', 3, 240.00, 'PENDING', 'PENDING', 'Interested in photography opportunities', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Completed bookings
(1, 9, 1, '2024-01-10', '09:00:00', '15:00:00', 2, 150.00, 'COMPLETED', 'PAID', 'No special requests', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 6, 2, '2024-01-15', '08:00:00', '16:00:00', 1, 120.00, 'COMPLETED', 'PAID', 'Solo traveler', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample reviews
INSERT INTO reviews (booking_id, tourist_id, guide_id, tour_id, rating, comment, is_verified, created_at) VALUES
(1, 6, 1, 1, 5, 'Amazing experience! The tea plantation was beautiful and our guide Kamal was very knowledgeable. Learned so much about Ceylon tea production and history.', true, CURRENT_TIMESTAMP),
(2, 7, 2, 3, 5, 'Incredible snorkeling experience! Saw so many colorful fish and coral. Nimal was an excellent guide and made us feel very safe in the water.', true, CURRENT_TIMESTAMP),
(3, 8, 3, 5, 4, 'Fascinating cultural tour. Sigiriya was breathtaking and Priya explained the history beautifully. Would recommend to anyone interested in Sri Lankan heritage.', true, CURRENT_TIMESTAMP),
(4, 9, 4, 7, 5, 'Life-changing sunrise trek! Challenging but absolutely worth it. Rajesh was very supportive and encouraging throughout the climb.', true, CURRENT_TIMESTAMP),
(8, 9, 1, 1, 4, 'Great tea tour with beautiful scenery. The tea tasting was educational and fun. Guide was friendly and spoke excellent English.', true, CURRENT_TIMESTAMP),
(9, 6, 2, 3, 5, 'Perfect day at the beach! Marine life was amazing and the guide was very professional. Great value for money.', true, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample payments
INSERT INTO payments (booking_id, amount, currency, stripe_payment_intent_id, status, payment_method, created_at, updated_at) VALUES
-- Successful payments
(1, 150.00, 'USD', 'pi_test_123456', 'SUCCEEDED', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 360.00, 'USD', 'pi_test_789012', 'SUCCEEDED', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 300.00, 'USD', 'pi_test_345678', 'SUCCEEDED', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 110.00, 'USD', 'pi_test_456789', 'SUCCEEDED', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 150.00, 'USD', 'pi_test_567890', 'SUCCEEDED', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 120.00, 'USD', 'pi_test_678901', 'SUCCEEDED', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Pending payments
(5, 380.00, 'USD', 'pi_test_789013', 'PENDING', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 130.00, 'USD', 'pi_test_890124', 'PENDING', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 240.00, 'USD', 'pi_test_901235', 'PENDING', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample messages
INSERT INTO messages (sender_id, receiver_id, booking_id, message, is_read, created_at) VALUES
-- Tea tour conversation
(6, 2, 1, 'Hi! I have a question about the tea tour. What should I wear?', true, CURRENT_TIMESTAMP),
(2, 6, 1, 'Hello! Comfortable walking shoes and light clothing are recommended. The weather can be cool in the hills.', true, CURRENT_TIMESTAMP),
(6, 2, 1, 'Perfect! Also, are there any dietary restrictions for the refreshments?', true, CURRENT_TIMESTAMP),
(2, 6, 1, 'We can accommodate vegetarian preferences. Ill make sure to inform the tea estate about your dietary needs.', false, CURRENT_TIMESTAMP),

-- Beach tour conversation
(7, 3, 2, 'Hello! Im excited about the snorkeling tour. Is it suitable for beginners?', true, CURRENT_TIMESTAMP),
(3, 7, 2, 'Absolutely! The tour is perfect for beginners. Well provide all equipment and safety briefing.', true, CURRENT_TIMESTAMP),
(7, 3, 2, 'Great! Can we also do some underwater photography?', true, CURRENT_TIMESTAMP),
(3, 7, 2, 'Yes, I can bring an underwater camera and show you some techniques. It will be amazing!', false, CURRENT_TIMESTAMP),

-- Cultural tour conversation  
(8, 4, 3, 'Hi Priya! Quick question about the Ancient Kingdoms tour - how much walking is involved?', true, CURRENT_TIMESTAMP),
(4, 8, 3, 'Hello! There is moderate walking involved, especially at Sigiriya (about 1200 steps). Are you comfortable with that?', true, CURRENT_TIMESTAMP),
(8, 4, 3, 'Yes, that sounds fine. Looking forward to learning about the history!', false, CURRENT_TIMESTAMP),

-- Adventure tour conversation
(9, 5, 4, 'Hi! First time doing the Adams Peak trek. Any tips for preparation?', true, CURRENT_TIMESTAMP),
(5, 9, 4, 'Great choice! Bring warm clothes, water, and energy snacks. Start with some cardio training if possible.', true, CURRENT_TIMESTAMP),
(9, 5, 4, 'Thanks! What time do we usually reach the summit?', true, CURRENT_TIMESTAMP),
(5, 9, 4, 'We aim to reach the summit around 6 AM for the best sunrise view. Its truly magical!', false, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;


