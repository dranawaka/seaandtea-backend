# Sea & Tea Tours - Backend Implementation Guide

## 🚀 Overview
This document provides a complete backend implementation using Spring Boot for the Sea & Tea Tours platform - "The Upwork for Travel Guides in Sri Lanka".

## 🏗️ Architecture
- **Framework**: Spring Boot 3.2+
- **Database**: PostgreSQL (Primary) + Redis (Caching)
- **Authentication**: JWT + Spring Security
- **File Storage**: AWS S3 / Local Storage
- **Payment**: Stripe Integration
- **Real-time**: WebSocket for instant booking
- **Documentation**: OpenAPI 3.0 (Swagger)

## 📁 Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── seaandtea/
│   │           ├── SeaAndTeaApplication.java
│   │           ├── config/
│   │           ├── controller/
│   │           ├── service/
│   │           ├── repository/
│   │           ├── entity/
│   │           ├── dto/
│   │           ├── exception/
│   │           ├── security/
│   │           └── util/
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
```

## 🛠️ Dependencies (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.seaandtea</groupId>
    <artifactId>seaandtea-backend</artifactId>
    <version>1.0.0</version>
    <name>Sea & Tea Tours Backend</name>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
        </dependency>
        
        <!-- Stripe -->
        <dependency>
            <groupId>com.stripe</groupId>
            <artifactId>stripe-java</artifactId>
            <version>24.8.0</version>
        </dependency>
        
        <!-- AWS S3 -->
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>s3</artifactId>
            <version>2.24.12</version>
        </dependency>
        
        <!-- OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.2.0</version>
        </dependency>
        
        <!-- Utilities -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.5.5.Final</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>1.5.5.Final</version>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## ⚙️ Configuration (application.yml)

```yaml
spring:
  application:
    name: seaandtea-backend
  
  profiles:
    active: dev
  
  datasource:
    url: jdbc:postgresql://localhost:5432/seaandtea
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  flyway:
    enabled: true
    baseline-on-migrate: true
  
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD:}
  
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:your-secret-key-here}
  expiration: 86400000 # 24 hours

# Stripe Configuration
stripe:
  secret-key: ${STRIPE_SECRET_KEY}
  publishable-key: ${STRIPE_PUBLISHABLE_KEY}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}

# AWS Configuration
aws:
  s3:
    bucket-name: ${AWS_S3_BUCKET:seaandtea-uploads}
    region: ${AWS_REGION:us-east-1}
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}

# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /api/v1

# Logging
logging:
  level:
    com.seaandtea: DEBUG
    org.springframework.security: DEBUG
```

## 🗄️ Database Design

### Core Tables

#### 1. Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    nationality VARCHAR(100),
    profile_picture_url VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) DEFAULT 'USER', -- USER, GUIDE, ADMIN
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2. Guides Table
```sql
CREATE TABLE guides (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    bio TEXT,
    hourly_rate DECIMAL(10,2),
    daily_rate DECIMAL(10,2),
    response_time_hours INTEGER DEFAULT 24,
    is_available BOOLEAN DEFAULT TRUE,
    total_tours INTEGER DEFAULT 0,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INTEGER DEFAULT 0,
    verification_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED
    verification_documents JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3. Guide Specialties Table
```sql
CREATE TABLE guide_specialties (
    id BIGSERIAL PRIMARY KEY,
    guide_id BIGINT REFERENCES guides(id),
    specialty VARCHAR(100) NOT NULL,
    years_experience INTEGER,
    certification_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4. Guide Languages Table
```sql
CREATE TABLE guide_languages (
    id BIGSERIAL PRIMARY KEY,
    guide_id BIGINT REFERENCES guides(id),
    language VARCHAR(50) NOT NULL,
    proficiency_level VARCHAR(20) NOT NULL, -- BASIC, INTERMEDIATE, FLUENT, NATIVE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 5. Tours Table
```sql
CREATE TABLE tours (
    id BIGSERIAL PRIMARY KEY,
    guide_id BIGINT REFERENCES guides(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL, -- TEA_TOURS, BEACH_TOURS, CULTURAL_TOURS, etc.
    duration_hours INTEGER NOT NULL,
    max_group_size INTEGER DEFAULT 10,
    price_per_person DECIMAL(10,2) NOT NULL,
    instant_booking BOOLEAN DEFAULT FALSE,
    secure_payment BOOLEAN DEFAULT TRUE,
    languages JSONB, -- Array of supported languages
    highlights JSONB, -- Array of tour highlights
    included_items JSONB, -- Array of included items
    excluded_items JSONB, -- Array of excluded items
    meeting_point VARCHAR(500),
    cancellation_policy TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 6. Tour Images Table
```sql
CREATE TABLE tour_images (
    id BIGSERIAL PRIMARY KEY,
    tour_id BIGINT REFERENCES tours(id),
    image_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    alt_text VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 7. Bookings Table
```sql
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    tour_id BIGINT REFERENCES tours(id),
    tourist_id BIGINT REFERENCES users(id),
    guide_id BIGINT REFERENCES guides(id),
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    number_of_people INTEGER NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, CONFIRMED, CANCELLED, COMPLETED
    payment_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PAID, REFUNDED
    payment_intent_id VARCHAR(255),
    special_requests TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 8. Reviews Table
```sql
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id),
    tourist_id BIGINT REFERENCES users(id),
    guide_id BIGINT REFERENCES guides(id),
    tour_id BIGINT REFERENCES tours(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 9. Payments Table
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES bookings(id),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    stripe_payment_intent_id VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, SUCCEEDED, FAILED, CANCELLED
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 10. Messages Table
```sql
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT REFERENCES users(id),
    receiver_id BIGINT REFERENCES users(id),
    booking_id BIGINT REFERENCES bookings(id),
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔐 Security Configuration

### JWT Security Config
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;
    
    @Autowired
    private AuthenticationProvider authenticationProvider;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### JWT Service
```java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

## 🎯 Core Controllers

### 1. Authentication Controller
```java
@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }
}
```

### 2. Guides Controller
```java
@RestController
@RequestMapping("/api/v1/guides")
@Validated
@Tag(name = "Guides", description = "Guide management APIs")
public class GuideController {
    
    @Autowired
    private GuideService guideService;
    
    @GetMapping
    public ResponseEntity<PageResponse<GuideResponse>> getGuides(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        GuideSearchCriteria criteria = GuideSearchCriteria.builder()
                .specialty(specialty)
                .language(language)
                .location(location)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();
        
        PageResponse<GuideResponse> guides = guideService.searchGuides(criteria, page, size);
        return ResponseEntity.ok(guides);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GuideDetailResponse> getGuideById(@PathVariable Long id) {
        GuideDetailResponse guide = guideService.getGuideById(id);
        return ResponseEntity.ok(guide);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GuideResponse> createGuide(@Valid @RequestBody CreateGuideRequest request) {
        GuideResponse guide = guideService.createGuide(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(guide);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<GuideResponse> updateGuide(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateGuideRequest request) {
        GuideResponse guide = guideService.updateGuide(id, request);
        return ResponseEntity.ok(guide);
    }
    
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GuideResponse> verifyGuide(@PathVariable Long id) {
        GuideResponse guide = guideService.verifyGuide(id);
        return ResponseEntity.ok(guide);
    }
}
```

### 3. Tours Controller
```java
@RestController
@RequestMapping("/api/v1/tours")
@Validated
@Tag(name = "Tours", description = "Tour management APIs")
public class TourController {
    
    @Autowired
    private TourService tourService;
    
    @GetMapping
    public ResponseEntity<PageResponse<TourResponse>> getTours(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        TourSearchCriteria criteria = TourSearchCriteria.builder()
                .category(category)
                .location(location)
                .minDuration(minDuration)
                .maxDuration(maxDuration)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();
        
        PageResponse<TourResponse> tours = tourService.searchTours(criteria, page, size);
        return ResponseEntity.ok(tours);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TourDetailResponse> getTourById(@PathVariable Long id) {
        TourDetailResponse tour = tourService.getTourById(id);
        return ResponseEntity.ok(tour);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('GUIDE')")
    public ResponseEntity<TourResponse> createTour(@Valid @RequestBody CreateTourRequest request) {
        TourResponse tour = tourService.createTour(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tour);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<TourResponse> updateTour(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateTourRequest request) {
        TourResponse tour = tourService.updateTour(id, request);
        return ResponseEntity.ok(tour);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 4. Bookings Controller
```java
@RestController
@RequestMapping("/api/v1/bookings")
@Validated
@Tag(name = "Bookings", description = "Booking management APIs")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<BookingDetailResponse> getBookingById(@PathVariable Long id) {
        BookingDetailResponse booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
        List<BookingResponse> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/guide/{guideId}")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getGuideBookings(@PathVariable Long guideId) {
        List<BookingResponse> bookings = bookingService.getGuideBookings(guideId);
        return ResponseEntity.ok(bookings);
    }
    
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        BookingResponse booking = bookingService.confirmBooking(id);
        return ResponseEntity.ok(booking);
    }
    
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        BookingResponse booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(booking);
    }
    
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable Long id) {
        BookingResponse booking = bookingService.completeBooking(id);
        return ResponseEntity.ok(booking);
    }
}
```

### 5. Reviews Controller
```java
@RestController
@RequestMapping("/api/v1/reviews")
@Validated
@Tag(name = "Reviews", description = "Review management APIs")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @GetMapping("/guide/{guideId}")
    public ResponseEntity<PageResponse<ReviewResponse>> getGuideReviews(
            @PathVariable Long guideId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageResponse<ReviewResponse> reviews = reviewService.getGuideReviews(guideId, page, size);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/tour/{tourId}")
    public ResponseEntity<PageResponse<ReviewResponse>> getTourReviews(
            @PathVariable Long tourId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageResponse<ReviewResponse> reviews = reviewService.getTourReviews(tourId, page, size);
        return ResponseEntity.ok(reviews);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse review = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponse review = reviewService.updateReview(id, request);
        return ResponseEntity.ok(review);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 💳 Payment Integration

### Stripe Service
```java
@Service
@Slf4j
public class StripeService {
    
    @Value("${stripe.secret-key}")
    private String secretKey;
    
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;
    
    @Autowired
    private PaymentService paymentService;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
    
    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount().longValue())
                    .setCurrency(request.getCurrency())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .setMetadata(Map.of(
                            "bookingId", request.getBookingId().toString(),
                            "touristId", request.getTouristId().toString(),
                            "guideId", request.getGuideId().toString()
                    ))
                    .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            return PaymentIntentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .build();
                    
        } catch (StripeException e) {
            log.error("Error creating payment intent: {}", e.getMessage());
            throw new PaymentException("Failed to create payment intent", e);
        }
    }
    
    public void handleWebhook(String payload, String sigHeader) {
        Event event = null;
        
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature: {}", e.getMessage());
            throw new PaymentException("Invalid webhook signature", e);
        }
        
        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getData().getObject();
            paymentService.handlePaymentSuccess(paymentIntent.getId());
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getData().getObject();
            paymentService.handlePaymentFailure(paymentIntent.getId());
        }
    }
}
```

## 📧 Email Service

### Email Service Implementation
```java
@Service
@Slf4j
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Async
    public void sendBookingConfirmation(String toEmail, String touristName, String tourTitle, 
                                      LocalDateTime tourDate, String guideName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Booking Confirmation - " + tourTitle);
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your booking for '%s' has been confirmed!\n\n" +
                "Tour Date: %s\n" +
                "Guide: %s\n\n" +
                "We look forward to providing you with an amazing experience!\n\n" +
                "Best regards,\nSea & Tea Tours Team",
                touristName, tourTitle, tourDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")), guideName
            ));
            
            mailSender.send(message);
            log.info("Booking confirmation email sent to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to: {}", toEmail, e);
        }
    }
    
    @Async
    public void sendGuideNotification(String toEmail, String guideName, String touristName, 
                                   String tourTitle, LocalDateTime tourDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("New Booking Request - " + tourTitle);
            message.setText(String.format(
                "Dear %s,\n\n" +
                "You have received a new booking request!\n\n" +
                "Tourist: %s\n" +
                "Tour: %s\n" +
                "Date: %s\n\n" +
                "Please log in to your dashboard to confirm or decline this booking.\n\n" +
                "Best regards,\nSea & Tea Tours Team",
                guideName, touristName, tourTitle, tourDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))
            ));
            
            mailSender.send(message);
            log.info("Guide notification email sent to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send guide notification email to: {}", toEmail, e);
        }
    }
}
```

## 🔄 Real-time Communication

### WebSocket Configuration
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new BookingWebSocketHandler(), "/ws/bookings")
                .setAllowedOrigins("*");
    }
}
```

### WebSocket Handler
```java
@Component
@Slf4j
public class BookingWebSocketHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = extractUserId(session);
        sessions.put(userId, session);
        log.info("WebSocket connection established for user: {}", userId);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = extractUserId(session);
        sessions.remove(userId);
        log.info("WebSocket connection closed for user: {}", userId);
    }
    
    public void sendBookingNotification(String userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("Failed to send WebSocket message to user: {}", userId, e);
            }
        }
    }
    
    private String extractUserId(WebSocketSession session) {
        // Extract user ID from session attributes or query parameters
        return session.getUri().getQuery().split("=")[1];
    }
}
```

## 🧪 Testing

### Test Configuration
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
class SeaAndTeaApplicationTests {
    
    @Test
    void contextLoads() {
        // Context loads successfully
    }
}
```

### Guide Service Test
```java
@ExtendWith(MockitoExtension.class)
class GuideServiceTest {
    
    @Mock
    private GuideRepository guideRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private GuideService guideService;
    
    @Test
    void createGuide_ValidRequest_ReturnsGuideResponse() {
        // Given
        CreateGuideRequest request = CreateGuideRequest.builder()
                .userId(1L)
                .bio("Experienced tour guide")
                .hourlyRate(new BigDecimal("25.00"))
                .dailyRate(new BigDecimal("200.00"))
                .specialties(List.of("Tea Tours", "Cultural Tours"))
                .languages(List.of("English", "Sinhala"))
                .build();
        
        User user = new User();
        user.setId(1L);
        user.setEmail("guide@example.com");
        user.setRole("USER");
        
        Guide guide = new Guide();
        guide.setId(1L);
        guide.setUser(user);
        guide.setBio(request.getBio());
        guide.setHourlyRate(request.getHourlyRate());
        guide.setDailyRate(request.getDailyRate());
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(guideRepository.save(any(Guide.class))).thenReturn(guide);
        
        // When
        GuideResponse response = guideService.createGuide(request);
        
        // Then
        assertNotNull(response);
        assertEquals(request.getBio(), response.getBio());
        assertEquals(request.getHourlyRate(), response.getHourlyRate());
        verify(guideRepository).save(any(Guide.class));
    }
}
```

## 🚀 Deployment

### Docker Configuration
```dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis
    networks:
      - seaandtea-network
  
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: seaandtea
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - seaandtea-network
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - seaandtea-network
  
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - app
    networks:
      - seaandtea-network

volumes:
  postgres_data:

networks:
  seaandtea-network:
    driver: bridge
```

## 📊 Monitoring & Logging

### Application Metrics
```java
@Configuration
@EnablePrometheusMetrics
public class MetricsConfig {
    
    @Bean
    MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
```

### Health Checks
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public Health health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "UP")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

## 🔒 Security Best Practices

1. **Input Validation**: Use Bean Validation annotations
2. **SQL Injection Prevention**: Use JPA repositories with parameterized queries
3. **XSS Protection**: Sanitize user inputs
4. **Rate Limiting**: Implement API rate limiting
5. **CORS Configuration**: Configure proper CORS policies
6. **Audit Logging**: Log all security-related events
7. **Password Hashing**: Use BCrypt for password hashing
8. **JWT Security**: Implement proper JWT token management

## 📈 Performance Optimization

1. **Database Indexing**: Create proper indexes on frequently queried columns
2. **Caching**: Implement Redis caching for frequently accessed data
3. **Connection Pooling**: Configure HikariCP connection pool
4. **Async Processing**: Use @Async for non-blocking operations
5. **Pagination**: Implement proper pagination for large datasets
6. **Image Optimization**: Compress and resize images before storage

## 🚀 Next Steps

1. **Set up CI/CD pipeline** with GitHub Actions
2. **Implement comprehensive testing** (Unit, Integration, E2E)
3. **Add monitoring and alerting** with Prometheus and Grafana
4. **Set up backup and recovery** procedures
5. **Implement API versioning** strategy
6. **Add comprehensive logging** and error tracking
7. **Set up staging environment** for testing
8. **Implement feature flags** for gradual rollouts

This backend implementation provides a solid foundation for your Sea & Tea Tours platform with all the core features needed for MVP and future scaling.
