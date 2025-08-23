# Sea & Tea Tours Backend

A comprehensive Spring Boot backend for the Sea & Tea Tours platform - "The Upwork for Travel Guides in Sri Lanka".

## 🚀 Features

- **User Management**: Registration, authentication, and profile management
- **Guide Management**: Guide profiles, specialties, languages, and verification
- **Tour Management**: Tour creation, search, and management
- **Booking System**: Tour booking with real-time updates
- **Payment Integration**: Stripe payment processing
- **Review System**: Tour and guide reviews
- **Real-time Communication**: WebSocket for instant updates
- **File Storage**: AWS S3 integration for images
- **Email Notifications**: Automated email notifications
- **Security**: JWT-based authentication with Spring Security

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.2+
- **Database**: PostgreSQL + Redis (Caching)
- **Authentication**: JWT + Spring Security
- **Payment**: Stripe Integration
- **File Storage**: AWS S3
- **Real-time**: WebSocket
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Build Tool**: Maven
- **Java Version**: 17

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15+
- Redis 7+
- Docker & Docker Compose (optional)

## 🚀 Quick Start

### Option 1: Using Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd seaandtea-backend
   ```

2. **Start the services**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - API: http://localhost:8080/api/v1
   - Swagger UI: http://localhost:8080/swagger-ui/index.html
   - PostgreSQL: localhost:5432
   - Redis: localhost:6379

### Option 2: Local Development

1. **Set up PostgreSQL**
   ```sql
   CREATE DATABASE seaandtea_dev;
   CREATE USER postgres WITH PASSWORD 'password';
   GRANT ALL PRIVILEGES ON DATABASE seaandtea_dev TO postgres;
   ```

2. **Set up Redis**
   ```bash
   redis-server
   ```

3. **Configure environment variables**
   ```bash
   export DB_USERNAME=postgres
   export DB_PASSWORD=password
   export JWT_SECRET=your-secret-key-here
   ```

4. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `password` |
| `JWT_SECRET` | JWT signing secret | `your-secret-key-here` |
| `STRIPE_SECRET_KEY` | Stripe secret key | - |
| `STRIPE_PUBLISHABLE_KEY` | Stripe publishable key | - |
| `AWS_ACCESS_KEY` | AWS access key | - |
| `AWS_SECRET_KEY` | AWS secret key | - |
| `MAIL_USERNAME` | Email username | - |
| `MAIL_PASSWORD` | Email password | - |

### Application Profiles

- **dev**: Development configuration with H2 database
- **prod**: Production configuration with PostgreSQL

## 📚 API Documentation

Once the application is running, you can access:

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🗄️ Database Schema

The application uses the following main entities:

- **Users**: User accounts and authentication
- **Guides**: Guide profiles and verification
- **Tours**: Tour offerings and details
- **Bookings**: Tour reservations
- **Reviews**: Tour and guide ratings
- **Payments**: Payment transactions
- **Messages**: User communication

## 🔐 Security

- JWT-based authentication
- Role-based access control (USER, GUIDE, ADMIN)
- Password encryption with BCrypt
- CORS configuration
- Input validation and sanitization

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run integration tests
mvn verify
```

## 📦 Building

```bash
# Build the application
mvn clean package

# Build Docker image
docker build -t seaandtea-backend .
```

## 🚀 Deployment

### Docker Deployment

```bash
# Build and push to registry
docker build -t your-registry/seaandtea-backend:latest .
docker push your-registry/seaandtea-backend:latest

# Deploy with docker-compose
docker-compose -f docker-compose.prod.yml up -d
```

### Traditional Deployment

1. Build the JAR file: `mvn clean package`
2. Upload to your server
3. Run: `java -jar seaandtea-backend.jar --spring.profiles.active=prod`

## 📊 Monitoring

- Health checks at `/actuator/health`
- Metrics at `/actuator/metrics`
- Application info at `/actuator/info`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:

- Email: info@seaandtea.com
- Documentation: [API Docs](http://localhost:8080/swagger-ui/index.html)
- Issues: [GitHub Issues](https://github.com/your-org/seaandtea-backend/issues)

## 🔄 Changelog

### Version 1.0.0
- Initial release
- Core user management
- Guide and tour management
- Booking system
- Payment integration
- Real-time communication

