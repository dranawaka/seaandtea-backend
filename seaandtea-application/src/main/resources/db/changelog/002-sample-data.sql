--liquibase formatted sql

--changeset seaandtea:002-sample-data dbms:h2 context:local
--comment Sample users and marketplace data. Every account uses password: password
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM users

INSERT INTO users (id, email, password_hash, first_name, last_name, phone, date_of_birth, nationality, profile_picture_url, is_verified, is_active, role, created_at, updated_at) VALUES
(1, 'admin@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'User', '+94112345678', DATE '1990-01-01', 'Sri Lankan', 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=400&q=60', TRUE, TRUE, 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'guide1@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Kamal', 'Perera', '+94112345679', DATE '1985-05-15', 'Sri Lankan', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=60', TRUE, TRUE, 'GUIDE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'guide2@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Nimal', 'Fernando', '+94112345680', DATE '1988-08-20', 'Sri Lankan', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=60', TRUE, TRUE, 'GUIDE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'guide3@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Priya', 'Jayawardena', '+94112345681', DATE '1990-12-05', 'Sri Lankan', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=60', TRUE, TRUE, 'GUIDE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'guide4@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Rajesh', 'Silva', '+94112345682', DATE '1983-03-18', 'Sri Lankan', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=400&q=60', TRUE, TRUE, 'GUIDE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'guide5@seaandtea.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Ayesha', 'Bandara', '+94112345683', DATE '1993-06-11', 'Sri Lankan', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=400&q=60', TRUE, TRUE, 'GUIDE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'tourist1@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Smith', '+1234567890', DATE '1992-03-10', 'American', 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=60', TRUE, TRUE, 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'tourist2@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Emma', 'Johnson', '+447700900123', DATE '1990-07-22', 'British', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=400&q=60', TRUE, TRUE, 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'tourist3@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Sophie', 'Mueller', '+491512345678', DATE '1994-09-14', 'German', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=60', TRUE, TRUE, 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO guides (id, user_id, bio, hourly_rate, daily_rate, response_time_hours, is_available, total_tours, average_rating, total_reviews, verification_status, created_at, updated_at) VALUES
(1, 2, 'Tea and hill-country guide with ten years of estate walks, factory visits, and tastings.', 25.00, 200.00, 2, TRUE, 3, 4.80, 2, 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, 'Coastal guide and dive lead for whale watching, snorkeling, and south-coast boat days.', 30.00, 250.00, 4, TRUE, 4, 4.90, 1, 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 4, 'Cultural and wildlife guide covering temples, ancient cities, and national park safaris.', 28.00, 220.00, 3, TRUE, 3, 4.70, 0, 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 5, 'Adventure guide for hill hikes, tea-trail walks, and sunrise treks around Ella.', 35.00, 280.00, 6, TRUE, 1, 4.90, 0, 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 6, 'New Galle Fort guide awaiting verification. History walks and family tours.', 22.00, 160.00, 12, FALSE, 0, 0.00, 0, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO guide_specialties (id, guide_id, specialty, years_experience, created_at) VALUES
(1, 1, 'Tea Tours', 10, CURRENT_TIMESTAMP),
(2, 1, 'Food Tours', 6, CURRENT_TIMESTAMP),
(3, 2, 'Beach Tours', 12, CURRENT_TIMESTAMP),
(4, 2, 'Whale Watching', 8, CURRENT_TIMESTAMP),
(5, 3, 'Cultural Tours', 7, CURRENT_TIMESTAMP),
(6, 3, 'Wildlife Safaris', 5, CURRENT_TIMESTAMP),
(7, 4, 'Hiking', 12, CURRENT_TIMESTAMP),
(8, 4, 'Adventure Tours', 8, CURRENT_TIMESTAMP),
(9, 5, 'Historical Tours', 3, CURRENT_TIMESTAMP);

INSERT INTO guide_languages (id, guide_id, language, proficiency_level, created_at) VALUES
(1, 1, 'English', 'FLUENT', CURRENT_TIMESTAMP),
(2, 1, 'Sinhala', 'NATIVE', CURRENT_TIMESTAMP),
(3, 2, 'English', 'FLUENT', CURRENT_TIMESTAMP),
(4, 2, 'German', 'INTERMEDIATE', CURRENT_TIMESTAMP),
(5, 3, 'English', 'FLUENT', CURRENT_TIMESTAMP),
(6, 3, 'Tamil', 'FLUENT', CURRENT_TIMESTAMP),
(7, 4, 'English', 'FLUENT', CURRENT_TIMESTAMP),
(8, 4, 'Japanese', 'INTERMEDIATE', CURRENT_TIMESTAMP),
(9, 5, 'English', 'FLUENT', CURRENT_TIMESTAMP),
(10, 5, 'Sinhala', 'NATIVE', CURRENT_TIMESTAMP);

INSERT INTO tours (id, guide_id, title, description, category, duration_hours, max_group_size, price_per_person, instant_booking, secure_payment, languages, highlights, included_items, excluded_items, destination, departure_time, meeting_point, cancellation_policy, is_active, created_at, updated_at) VALUES
(1, 1, 'Ceylon Tea Experience', 'Walk a Kandy tea estate, see the factory, and taste several Ceylon grades with a local guide.', 'TEA_TOURS', 6, 8, 75.00, TRUE, TRUE, '["English","Sinhala"]', '["Tea fields","Factory tour","Tasting","Hill views"]', '["Transport","Tasting","Guide","Tea"]', '["Lunch","Souvenirs"]', 'Kandy', TIME '09:00:00', 'Kandy Railway Station', 'Free cancellation up to 24 hours before the tour', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Nuwara Eliya Tea Estate', 'Cool-climate estates, a planter bungalow stop, and a tasting above Gregory Lake.', 'TEA_TOURS', 8, 6, 95.00, TRUE, TRUE, '["English","Sinhala"]', '["Estate walk","Bungalow visit","Tasting","Lake view"]', '["Transport","Tasting","Guide","Fees"]', '["Meals"]', 'Nuwara Eliya', TIME '08:00:00', 'Nuwara Eliya Town Center', 'Free cancellation up to 48 hours before the tour', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 'Mirissa Whale and Beach', 'Morning boat for whales and dolphins, then a swim on Mirissa beach.', 'BEACH_TOURS', 8, 6, 120.00, TRUE, TRUE, '["English","Sinhala"]', '["Whale watching","Beach time","Coastal views"]', '["Boat","Guide","Life jackets","Water"]', '["Lunch","Photos"]', 'Mirissa', TIME '06:00:00', 'Mirissa Harbour', 'Free cancellation up to 48 hours before the tour', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2, 'Galle Fort and Snorkel', 'Walk the ramparts, then snorkel the reef off the southern coast.', 'BEACH_TOURS', 5, 8, 65.00, TRUE, TRUE, '["English","German"]', '["Galle Fort","Reef stop","Swim time"]', '["Gear","Guide","Water"]', '["Meals","Transport from Colombo"]', 'Galle', TIME '09:00:00', 'Galle Fort Main Gate', 'Free cancellation up to 24 hours before the tour', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, 'Bentota River and Beach', 'A calm river boat, a short beach stop, and time for a swim.', 'BEACH_TOURS', 6, 8, 90.00, TRUE, TRUE, '["English"]', '["River boat","Beach swim","Mangroves"]', '["Boat","Guide","Water"]', '["Meals"]', 'Bentota', TIME '09:00:00', 'Bentota Beach Hotel Road', 'Free cancellation up to 24 hours before the tour', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 3, 'Sigiriya and Dambulla', 'Climb Sigiriya rock and visit the cave temples with a historian guide.', 'CULTURAL_TOURS', 10, 6, 150.00, FALSE, TRUE, '["English","Tamil"]', '["Sigiriya","Cave temple","Village stop"]', '["Transport","Tickets","Guide","Lunch"]', '["Tips","Souvenirs"]', 'Sigiriya', TIME '07:00:00', 'Sigiriya Village Gate', 'Free cancellation up to 72 hours before the tour', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 3, 'Kandy Temple Morning', 'Temple of the Tooth, a short lake walk, and the botanic gardens.', 'CULTURAL_TOURS', 6, 10, 80.00, TRUE, TRUE, '["English","Tamil"]', '["Temple","Lake walk","Gardens"]', '["Transport","Tickets","Guide"]', '["Meals"]', 'Kandy', TIME '08:30:00', 'Kandy City Center', 'Free cancellation up to 24 hours before the tour', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 1, 'Colombo Street Food Walk', 'Markets, short eats, and a spice stop through Pettah with tastings included.', 'FOOD_TOURS', 4, 8, 45.00, TRUE, TRUE, '["English"]', '["Pettah market","Tastings","Spice stop"]', '["All tastings","Guide","Water"]', '["Hotel pickup"]', 'Colombo', TIME '16:00:00', 'Fort Railway Station', 'Free cancellation up to 24 hours before the tour', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 4, 'Ella Rock Hike', 'A guided hike through tea trails to Ella Rock, with the Nine Arch Bridge on the way back.', 'ADVENTURE_TOURS', 6, 8, 85.00, TRUE, TRUE, '["English","Japanese"]', '["Ella Rock","Tea trails","Nine Arch Bridge"]', '["Guide","Water","Trail snack"]', '["Meals","Tips"]', 'Ella', TIME '06:30:00', 'Ella Railway Station', 'Free cancellation up to 48 hours before the tour', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 3, 'Yala Leopard Safari', 'Dawn jeep safari in Yala for leopards, elephants, and birds.', 'WILDLIFE_TOURS', 8, 6, 135.00, TRUE, TRUE, '["English","Tamil"]', '["Game drive","Tracker","Park entry"]', '["Jeep","Tracker","Park ticket","Water"]', '["Meals","Lodging"]', 'Yala', TIME '05:30:00', 'Yala National Park Entrance', 'Free cancellation up to 48 hours before the tour', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 2, 'Trincomalee Snorkel Day', 'Boat to Pigeon Island, snorkeling on the reef, and a beach lunch stop.', 'BEACH_TOURS', 7, 8, 110.00, TRUE, TRUE, '["English"]', '["Pigeon Island","Reef","Beach stop"]', '["Boat","Gear","Guide","Water"]', '["Lunch"]', 'Trincomalee', TIME '08:00:00', 'Trincomalee Harbour', 'Free cancellation up to 48 hours before the tour', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tour_images (id, tour_id, image_url, is_primary, alt_text, created_at) VALUES
(1, 1, 'https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?auto=format&fit=crop&w=800&q=60', TRUE, 'Ceylon tea', CURRENT_TIMESTAMP),
(2, 2, 'https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=800&q=60', TRUE, 'Hill country estate', CURRENT_TIMESTAMP),
(3, 3, 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=60', TRUE, 'South coast beach', CURRENT_TIMESTAMP),
(4, 4, 'https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=800&q=60', TRUE, 'Galle coast', CURRENT_TIMESTAMP),
(5, 5, 'https://images.unsplash.com/photo-1519046904884-53103b34b206?auto=format&fit=crop&w=800&q=60', TRUE, 'Bentota beach', CURRENT_TIMESTAMP),
(6, 6, 'https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=800&q=60', TRUE, 'Ancient stone site', CURRENT_TIMESTAMP),
(7, 7, 'https://images.unsplash.com/photo-1546708973-b339540b5162?auto=format&fit=crop&w=800&q=60', TRUE, 'Kandy lake and temple', CURRENT_TIMESTAMP),
(8, 8, 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=800&q=60', TRUE, 'Street food', CURRENT_TIMESTAMP),
(9, 9, 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=60', TRUE, 'Hill hike', CURRENT_TIMESTAMP),
(10, 10, 'https://images.unsplash.com/photo-1549366021-9f761d450615?auto=format&fit=crop&w=800&q=60', TRUE, 'Wildlife safari', CURRENT_TIMESTAMP),
(11, 11, 'https://images.unsplash.com/photo-1559827260-dc66d52bef19?auto=format&fit=crop&w=800&q=60', TRUE, 'Trincomalee coast', CURRENT_TIMESTAMP);

INSERT INTO bookings (id, tour_id, tourist_id, guide_id, booking_date, start_time, end_time, number_of_people, total_amount, status, payment_status, special_requests, created_at, updated_at) VALUES
(1, 1, 7, 1, DATE '2026-10-04', TIME '09:00:00', TIME '15:00:00', 2, 150.00, 'COMPLETED', 'PAID', 'Vegetarian lunch if offered', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, 8, 2, DATE '2026-10-18', TIME '06:00:00', TIME '14:00:00', 3, 360.00, 'COMPLETED', 'PAID', 'Window seats on the boat', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 6, 9, 3, DATE '2026-12-12', TIME '07:00:00', TIME '17:00:00', 2, 300.00, 'CONFIRMED', 'PAID', 'Early hotel pickup', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 9, 7, 4, DATE '2026-12-20', TIME '06:30:00', TIME '12:30:00', 1, 85.00, 'CONFIRMED', 'PAID', 'First hike, please go steady', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, 8, 1, DATE '2027-01-08', TIME '08:00:00', TIME '16:00:00', 4, 380.00, 'PENDING', 'PENDING', 'Group of four friends', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 11, 7, 2, DATE '2027-01-16', TIME '08:00:00', TIME '15:00:00', 2, 220.00, 'PENDING', 'PENDING', 'Anniversary trip', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO reviews (id, booking_id, tourist_id, guide_id, tour_id, rating, comment, is_verified, created_at) VALUES
(1, 1, 7, 1, 1, 5, 'Beautiful estate and a clear tasting. Kamal knew every step of the process.', TRUE, CURRENT_TIMESTAMP),
(2, 2, 8, 2, 3, 5, 'We saw dolphins and the boat crew kept the morning easy and safe.', TRUE, CURRENT_TIMESTAMP);

INSERT INTO payments (id, booking_id, amount, currency, stripe_payment_intent_id, status, payment_method, created_at, updated_at) VALUES
(1, 1, 150.00, 'USD', 'pi_sample_tea_150', 'SUCCEEDED', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 360.00, 'USD', 'pi_sample_whale_360', 'SUCCEEDED', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, 300.00, 'USD', 'pi_sample_sigiriya_300', 'SUCCEEDED', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 4, 85.00, 'USD', 'pi_sample_ella_85', 'SUCCEEDED', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 5, 380.00, 'USD', 'pi_sample_nuwara_380', 'PENDING', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 6, 220.00, 'USD', 'pi_sample_trinco_220', 'PENDING', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO messages (id, sender_id, receiver_id, booking_id, message, is_read, created_at) VALUES
(1, 7, 2, 1, 'What should I wear for the tea estate walk?', TRUE, CURRENT_TIMESTAMP),
(2, 2, 7, 1, 'Closed shoes and a light layer. Mornings in the hills are cool.', TRUE, CURRENT_TIMESTAMP),
(3, 7, 2, 1, 'Can the tasting include a decaf option?', FALSE, CURRENT_TIMESTAMP),
(4, 8, 3, 2, 'Is the whale boat suitable if I get seasick?', TRUE, CURRENT_TIMESTAMP),
(5, 3, 8, 2, 'Yes. Sit mid-boat and I will bring motion bands.', FALSE, CURRENT_TIMESTAMP),
(6, 9, 4, 3, 'How many steps are there at Sigiriya?', FALSE, CURRENT_TIMESTAMP);

INSERT INTO products (id, name, description, current_price, original_price, category, rating, review_count, is_best_seller, is_active, created_at, updated_at) VALUES
(1, 'Ceylon Orange Pekoe 100g', 'Loose-leaf black tea from the Dimbula region. Bright, citrus, and malty.', 18.00, 24.00, 'TEA', 4.80, 86, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Nuwara Eliya Silver Tips', 'High-grown white tips with a light floral cup. Packed in a sealed tin.', 32.00, NULL, 'TEA', 4.90, 41, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Ceylon Cinnamon Quills', 'Hand-rolled Alba quills. Use whole in curries or steep for tea.', 12.00, 15.00, 'SPICES', 4.70, 54, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Handloom Beach Sarong', 'Cotton sarong woven in the south. Light enough for the beach and the train.', 28.00, NULL, 'SEA', 4.60, 33, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Kandy Wooden Mask', 'Small painted mask based on hill-country dance designs.', 45.00, 55.00, 'SOUVENIRS', 4.80, 22, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Virgin Coconut Hair Oil', 'Cold-pressed coconut oil in a 200ml bottle for hair and skin.', 16.00, NULL, 'BEAUTY', 4.50, 19, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Batik Cotton Shirt', 'Short-sleeve batik shirt dyed in a Colombo workshop.', 38.00, 48.00, 'CLOTHING', 4.40, 17, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO product_images (id, product_id, image_url, is_primary, alt_text, sort_order, created_at) VALUES
(1, 1, 'https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?auto=format&fit=crop&w=800&q=60', TRUE, 'Ceylon black tea', 0, CURRENT_TIMESTAMP),
(2, 2, 'https://images.unsplash.com/photo-1571934811356-5cc061b6821f?auto=format&fit=crop&w=800&q=60', TRUE, 'Silver tips tea', 0, CURRENT_TIMESTAMP),
(3, 3, 'https://images.unsplash.com/photo-1596040033229-a9821ebd058d?auto=format&fit=crop&w=800&q=60', TRUE, 'Cinnamon and spices', 0, CURRENT_TIMESTAMP),
(4, 4, 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=60', TRUE, 'Beach sarong', 0, CURRENT_TIMESTAMP),
(5, 5, 'https://images.unsplash.com/photo-1577083552431-6e5fd01988ec?auto=format&fit=crop&w=800&q=60', TRUE, 'Wooden mask', 0, CURRENT_TIMESTAMP),
(6, 6, 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=800&q=60', TRUE, 'Coconut oil', 0, CURRENT_TIMESTAMP),
(7, 7, 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=800&q=60', TRUE, 'Batik shirt', 0, CURRENT_TIMESTAMP);

INSERT INTO carts (id, user_id, created_at, updated_at) VALUES
(1, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO cart_items (id, cart_id, product_id, quantity) VALUES
(1, 1, 1, 2),
(2, 1, 3, 1);

INSERT INTO news_posts (id, title, body, author_id, is_published, created_at, updated_at) VALUES
(1, 'Hill-country tea flush is open', 'The Dimbula and Nuwara Eliya estates are in a fresh flush. Book a tasting this month if you want the brightest cups of the year. Guides are holding small groups so the factory floors stay quiet.', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Mirissa whale boats are running', 'Blue whales and spinner dolphins are being sighted off Mirissa. Morning departures leave the harbour at 6:00. Bring a light jacket and motion bands if you need them.', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'A new guide is waiting for review', 'Ayesha Bandara has applied to lead Galle Fort walks. Admins can approve or reject the profile from the guide queue. Verified guides are already live on the public list.', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO news_post_comments (id, post_id, user_id, text, created_at) VALUES
(1, 1, 7, 'The Kandy tasting was the best cup I had on the trip.', CURRENT_TIMESTAMP),
(2, 2, 8, 'We saw a pod of dolphins just past the headland.', CURRENT_TIMESTAMP),
(3, 2, 9, 'Is the December sea still calm enough for a first boat trip?', CURRENT_TIMESTAMP);

INSERT INTO news_post_likes (id, post_id, user_id, created_at) VALUES
(1, 1, 7, CURRENT_TIMESTAMP),
(2, 1, 8, CURRENT_TIMESTAMP),
(3, 2, 8, CURRENT_TIMESTAMP),
(4, 3, 9, CURRENT_TIMESTAMP);

INSERT INTO homepage_slider_images (id, image_url, sort_order, alt_text, created_at) VALUES
(1, 'https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?auto=format&fit=crop&w=1200&q=60', 0, 'Tea fields', CURRENT_TIMESTAMP),
(2, 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=60', 1, 'South coast', CURRENT_TIMESTAMP),
(3, 'https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=1200&q=60', 2, 'Ancient city', CURRENT_TIMESTAMP),
(4, 'https://images.unsplash.com/photo-1549366021-9f761d450615?auto=format&fit=crop&w=1200&q=60', 3, 'Yala wildlife', CURRENT_TIMESTAMP);

INSERT INTO wishlists (id, user_id, tour_id, created_at) VALUES
(1, 7, 1, CURRENT_TIMESTAMP),
(2, 7, 10, CURRENT_TIMESTAMP),
(3, 8, 9, CURRENT_TIMESTAMP);

--changeset seaandtea:003-restart-identity-h2 dbms:h2 context:local
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM users WHERE email = 'admin@seaandtea.com'
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE guides ALTER COLUMN id RESTART WITH 100;
ALTER TABLE guide_specialties ALTER COLUMN id RESTART WITH 100;
ALTER TABLE guide_languages ALTER COLUMN id RESTART WITH 100;
ALTER TABLE tours ALTER COLUMN id RESTART WITH 100;
ALTER TABLE tour_images ALTER COLUMN id RESTART WITH 100;
ALTER TABLE bookings ALTER COLUMN id RESTART WITH 100;
ALTER TABLE reviews ALTER COLUMN id RESTART WITH 100;
ALTER TABLE payments ALTER COLUMN id RESTART WITH 100;
ALTER TABLE messages ALTER COLUMN id RESTART WITH 100;
ALTER TABLE products ALTER COLUMN id RESTART WITH 100;
ALTER TABLE product_images ALTER COLUMN id RESTART WITH 100;
ALTER TABLE carts ALTER COLUMN id RESTART WITH 100;
ALTER TABLE cart_items ALTER COLUMN id RESTART WITH 100;
ALTER TABLE news_posts ALTER COLUMN id RESTART WITH 100;
ALTER TABLE news_post_comments ALTER COLUMN id RESTART WITH 100;
ALTER TABLE news_post_likes ALTER COLUMN id RESTART WITH 100;
ALTER TABLE homepage_slider_images ALTER COLUMN id RESTART WITH 100;
ALTER TABLE wishlists ALTER COLUMN id RESTART WITH 100;

--changeset seaandtea:003-restart-identity-pg dbms:postgresql context:local
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM users WHERE email = 'admin@seaandtea.com'
SELECT setval(pg_get_serial_sequence('users', 'id'), GREATEST((SELECT MAX(id) FROM users), 1));
SELECT setval(pg_get_serial_sequence('guides', 'id'), GREATEST((SELECT MAX(id) FROM guides), 1));
SELECT setval(pg_get_serial_sequence('guide_specialties', 'id'), GREATEST((SELECT MAX(id) FROM guide_specialties), 1));
SELECT setval(pg_get_serial_sequence('guide_languages', 'id'), GREATEST((SELECT MAX(id) FROM guide_languages), 1));
SELECT setval(pg_get_serial_sequence('tours', 'id'), GREATEST((SELECT MAX(id) FROM tours), 1));
SELECT setval(pg_get_serial_sequence('tour_images', 'id'), GREATEST((SELECT MAX(id) FROM tour_images), 1));
SELECT setval(pg_get_serial_sequence('bookings', 'id'), GREATEST((SELECT MAX(id) FROM bookings), 1));
SELECT setval(pg_get_serial_sequence('reviews', 'id'), GREATEST((SELECT MAX(id) FROM reviews), 1));
SELECT setval(pg_get_serial_sequence('payments', 'id'), GREATEST((SELECT MAX(id) FROM payments), 1));
SELECT setval(pg_get_serial_sequence('messages', 'id'), GREATEST((SELECT MAX(id) FROM messages), 1));
SELECT setval(pg_get_serial_sequence('products', 'id'), GREATEST((SELECT MAX(id) FROM products), 1));
SELECT setval(pg_get_serial_sequence('product_images', 'id'), GREATEST((SELECT MAX(id) FROM product_images), 1));
SELECT setval(pg_get_serial_sequence('carts', 'id'), GREATEST((SELECT MAX(id) FROM carts), 1));
SELECT setval(pg_get_serial_sequence('cart_items', 'id'), GREATEST((SELECT MAX(id) FROM cart_items), 1));
SELECT setval(pg_get_serial_sequence('news_posts', 'id'), GREATEST((SELECT MAX(id) FROM news_posts), 1));
SELECT setval(pg_get_serial_sequence('news_post_comments', 'id'), GREATEST((SELECT MAX(id) FROM news_post_comments), 1));
SELECT setval(pg_get_serial_sequence('news_post_likes', 'id'), GREATEST((SELECT MAX(id) FROM news_post_likes), 1));
SELECT setval(pg_get_serial_sequence('homepage_slider_images', 'id'), GREATEST((SELECT MAX(id) FROM homepage_slider_images), 1));
SELECT setval(pg_get_serial_sequence('wishlists', 'id'), GREATEST((SELECT MAX(id) FROM wishlists), 1));
