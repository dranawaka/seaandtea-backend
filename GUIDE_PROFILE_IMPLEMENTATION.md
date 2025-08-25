# Guide Profile Management Implementation

## Overview
This document describes the complete implementation of the Guide Profile Management feature for the Sea & Tea Tours backend application.

## Features Implemented

### 1. Core Entities
- **Guide**: Main entity with bio, rates, availability, and verification status
- **GuideSpecialty**: Specialties with years of experience and certifications
- **GuideLanguage**: Languages with proficiency levels (BASIC, INTERMEDIATE, FLUENT, NATIVE)

### 2. Data Transfer Objects (DTOs)
- **GuideProfileRequest**: For creating/updating guide profiles with validation
- **GuideProfileResponse**: For returning guide profile data
- **UserRoleUpdateRequest**: For updating user roles

### 3. Service Layer
- **GuideService**: Complete business logic for guide profile management
  - Create guide profile
  - Update guide profile
  - Get guide profile by ID or user ID
  - Get guide profile if exists (returns profile or 404)
  - Delete guide profile
  - Automatic user role update to GUIDE

### 4. Controller Layer
- **GuideController**: REST API endpoints for guide profile management
  - `POST /api/v1/guides` - Create guide profile
  - `GET /api/v1/guides/{guideId}` - Get guide profile by ID
  - `GET /api/v1/guides/my-profile` - Get current user's guide profile
  - `PUT /api/v1/guides/my-profile` - Update current user's guide profile
  - `PUT /api/v1/guides/{guideId}` - Update guide profile by ID (admin)
  - `DELETE /api/v1/guides/my-profile` - Delete current user's guide profile
  - `DELETE /api/v1/guides/{guideId}` - Delete guide profile by ID (admin)
  - `GET /api/v1/guides/my-profile/exists` - Get guide profile if exists (returns profile or 404)

### 5. Repository Layer
- **GuideRepository**: Enhanced with search and filtering capabilities
- **GuideSpecialtyRepository**: For managing guide specialties
- **GuideLanguageRepository**: For managing guide languages

### 6. Authentication & Security
- **JWT Integration**: User ID embedded in JWT tokens
- **Role Management**: Automatic role update to GUIDE when profile is created
- **Authorization**: Endpoints require valid JWT tokens

## API Endpoints

### Guide Profile Management
```
POST   /api/v1/guides                    - Create guide profile
GET    /api/v1/guides/{guideId}          - Get guide profile by ID
GET    /api/v1/guides/my-profile         - Get current user's guide profile
PUT    /api/v1/guides/my-profile         - Update current user's guide profile
PUT    /api/v1/guides/{guideId}          - Update guide profile by ID
DELETE /api/v1/guides/my-profile         - Delete current user's guide profile
DELETE /api/v1/guides/{guideId}          - Delete guide profile by ID
GET    /api/v1/guides/my-profile/exists  - Get guide profile if exists (returns profile or 404)
```

### User Role Management
```
PUT    /api/v1/users/role                - Update user role
```

## Data Validation

### Guide Profile Request Validation
- **Bio**: Required, minimum 50 characters
- **Hourly Rate**: Required, positive decimal with 2 decimal places
- **Daily Rate**: Required, positive decimal with 2 decimal places
- **Response Time**: Required, 1-168 hours (1 week max)
- **Availability**: Required boolean
- **Specialties**: Required, 1-10 specialties
- **Languages**: Required, 1-5 languages

### Specialty Validation
- **Specialty Name**: Required
- **Years Experience**: 0-50 years
- **Certification URL**: Optional

### Language Validation
- **Language**: Required
- **Proficiency Level**: Required (BASIC, INTERMEDIATE, FLUENT, NATIVE)

## Business Logic

### Guide Profile Creation
1. Validate user exists and doesn't have a guide profile
2. Update user role to GUIDE
3. Create guide entity with basic information
4. Create and associate specialties
5. Create and associate languages
6. Save complete guide profile
7. Return comprehensive response

### Guide Profile Updates
1. Validate guide exists
2. Update basic guide information
3. Clear existing specialties and languages
4. Create new specialties and languages
5. Save updated guide profile
6. Return updated response

### Automatic Role Management
- When a user creates their first guide profile, their role is automatically updated from USER to GUIDE
- This ensures proper access control and feature availability

## Error Handling

### Common Error Scenarios
- **User not found**: 404 Not Found
- **Guide profile already exists**: 400 Bad Request
- **Guide not found**: 400 Bad Request
- **Invalid request data**: 400 Bad Request with validation details
- **Authentication required**: 401 Unauthorized
- **Invalid JWT token**: 401 Unauthorized

### Validation Error Messages
- Clear, user-friendly error messages for each validation rule
- Field-specific error reporting
- Comprehensive validation for all required fields

## Testing

### Unit Tests
- **GuideServiceTest**: Comprehensive testing of service layer
- Tests for successful operations and error scenarios
- Mock-based testing with proper assertions

### Test Coverage
- Guide profile creation
- Guide profile updates
- Error handling scenarios
- Data validation

## Security Features

### JWT Token Security
- User ID embedded in JWT claims
- Secure token extraction and validation
- Proper authorization checks

### Role-Based Access Control
- Automatic role elevation to GUIDE
- Endpoint protection based on user roles
- Secure profile management

## Database Schema

### Tables
- **guides**: Main guide information
- **guide_specialties**: Guide specialties with experience
- **guide_languages**: Guide language proficiencies
- **users**: User information with roles

### Relationships
- One-to-one relationship between User and Guide
- One-to-many relationship between Guide and GuideSpecialty
- One-to-many relationship between Guide and GuideLanguage

## Future Enhancements

### Planned Features
1. **Profile Verification System**
   - Document upload and verification
   - Background checks
   - Certification validation

2. **Advanced Search and Filtering**
   - Location-based guide search
   - Specialty-based filtering
   - Rating and review filtering

3. **Guide Availability Management**
   - Calendar-based availability
   - Booking time slots
   - Conflict resolution

4. **Performance Metrics**
   - Response time tracking
   - Booking success rates
   - Customer satisfaction scores

## Integration Points

### Frontend Integration
- Guide profile creation form
- Profile editing interface
- Specialty and language management
- Dynamic form validation

### External Services
- Payment processing for guide services
- Notification system for profile updates
- Email verification system

## Deployment Considerations

### Environment Variables
- JWT secret key configuration
- Database connection settings
- API base URL configuration

### Performance Optimization
- Database indexing on frequently queried fields
- Caching for guide profile data
- Pagination for large result sets

## Monitoring and Logging

### Logging
- Comprehensive logging for all operations
- User action tracking
- Error logging with context

### Metrics
- Guide profile creation rates
- Update frequency tracking
- Error rate monitoring

## Conclusion

The Guide Profile Management feature provides a comprehensive solution for managing tour guide profiles in the Sea & Tea Tours application. It includes:

- Complete CRUD operations for guide profiles
- Robust validation and error handling
- Secure authentication and authorization
- Automatic role management
- Comprehensive testing coverage
- Scalable architecture for future enhancements

The implementation follows Spring Boot best practices and provides a solid foundation for the tour guide management system.

