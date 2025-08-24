# Backend Integration for Sea & Tea Tours Frontend

## Overview
This document describes the backend integration implemented for the Sea & Tea Tours frontend application, including user registration, authentication, and session management.

## API Configuration

### Base URL
The application is configured to connect to the backend API at:
- **Development**: `http://localhost:8080/api/v1`
- **Production**: Set via `REACT_APP_API_BASE_URL` environment variable

### API Documentation
- **Swagger UI**: `http://localhost:8080/api/v1/swagger-ui/index.html`
- **Postman Collection**: `Sea_Tea_Tours_API.postman_collection.json`
- **Backend Implementation**: `BACKEND_IMPLEMENTATION.md`

### Configuration File
The API configuration is centralized in `src/config/api.js` and includes:
- All endpoint definitions
- Helper functions for building URLs
- Default headers configuration

## Authentication System

### Complete User Flow
1. **User Registration** → Creates account with backend
2. **User Login** → Authenticates and receives JWT token
3. **Session Management** → Token stored and managed automatically
4. **User Logout** → Clears session and redirects

### Authentication Context
The app uses React Context (`src/context/AuthContext.js`) to manage:
- User authentication state
- JWT token storage
- User profile information
- Login/logout functions

## Updated Registration Form

### New Fields Added
The registration form now includes all required fields from the backend API:

1. **First Name** * (required)
2. **Last Name** * (required)
3. **Email Address** * (required)
4. **Phone Number** * (required)
   - Format: International format supported (+1 (555) 123-4567)
   - Validation: Basic phone number format validation
5. **Date of Birth** * (required)
   - Validation: Must be 18+ years old
6. **Nationality** * (required)
   - Dropdown with 50+ country options
7. **Password** * (required)
   - Minimum 8 characters
   - Must contain uppercase, lowercase, and number
8. **Confirm Password** * (required)

### Form Validation
- Client-side validation for all required fields
- Age verification (18+ requirement)
- Phone number format validation
- Password strength requirements
- Real-time error clearing

### Backend Integration
The registration form now:
- Sends data to `POST /auth/register` endpoint
- Matches the exact payload structure expected by the backend
- Handles API responses and errors appropriately
- Redirects to login page on successful registration

## Updated Login Form

### Features
- **Real API Integration** with `POST /auth/login` endpoint
- **JWT Token Management** - automatically stores and manages tokens
- **User Session Persistence** - remembers login across browser sessions
- **Error Handling** - displays backend validation errors
- **Success Redirect** - navigates to home page after login

### Authentication Flow
1. User enters email and password
2. Form validates input
3. API call to backend login endpoint
4. On success: JWT token stored, user redirected to home
5. On failure: Error message displayed
6. User session maintained across page refreshes

## API Endpoints Used

### Authentication
- `POST /auth/register` - User registration
- `POST /auth/login` - User authentication and JWT token generation

## Error Handling

### Client-Side Errors
- Field validation errors
- Password confirmation mismatch
- Age verification

### API Errors
- Network errors
- Backend validation errors
- Server errors
- User-friendly error messages displayed

## Session Management

### JWT Token Storage
- **Local Storage**: Tokens stored securely in browser
- **Automatic Persistence**: Login state maintained across sessions
- **Token Validation**: Backend validates tokens on protected requests

### User State
- **Global Context**: User information available throughout the app
- **Real-time Updates**: UI updates immediately on login/logout
- **Profile Display**: User name shown in navigation when logged in

## Environment Setup

### Development
Create a `.env` file in the root directory:
```env
REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
NODE_ENV=development
```

### Production
Set the environment variable:
```bash
REACT_APP_API_BASE_URL=https://your-api-domain.com/api/v1
```

## Testing the Integration

### Prerequisites
1. Backend server running on `http://localhost:8080`
2. Frontend application running
3. Backend API endpoints accessible

### Test Steps
1. **Registration Test**:
   - Navigate to `/register` page
   - Fill out all required fields
   - Submit the form
   - Verify successful registration and redirect to login

2. **Login Test**:
   - Use the credentials from registration
   - Navigate to `/login` page
   - Enter email and password
   - Verify successful login and redirect to home
   - Check that user name appears in navigation

3. **Session Test**:
   - Refresh the page
   - Verify user remains logged in
   - Check navigation shows user name and logout button

4. **Logout Test**:
   - Click logout button
   - Verify user is logged out
   - Check navigation shows login/signup buttons

### API Testing Tools
- **Swagger UI**: `http://localhost:8080/api/v1/swagger-ui/index.html` - Interactive API documentation and testing
- **Postman**: Use the provided collection for comprehensive API testing
- **Browser DevTools**: Monitor network requests and responses

## Troubleshooting

### Common Issues

1. **CORS Errors**
   - Ensure backend allows requests from frontend origin
   - Check backend CORS configuration

2. **API Connection Failed**
   - Verify backend server is running
   - Check API base URL configuration
   - Test API endpoint directly using Swagger UI or Postman

3. **Validation Errors**
   - Check backend validation requirements
   - Ensure all required fields are sent
   - Verify data format matches backend expectations

4. **Authentication Issues**
   - Check JWT token format in backend
   - Verify token expiration settings
   - Check localStorage for stored tokens

### Debug Steps
1. Check browser console for errors
2. Verify network requests in browser DevTools
3. Check backend server logs
4. Test API endpoints directly using Swagger UI
5. Use Postman collection for isolated API testing
6. Check localStorage for authentication data

## Profile Management System

### User Profile Updates
Users can now update their personal information through the profile page (`/profile`):

1. **Personal Information**
   - First Name, Last Name
   - Phone Number
   - Date of Birth
   - Nationality
   - Email (read-only, cannot be changed)

2. **Password Management**
   - Change current password
   - New password validation (8+ chars, uppercase, lowercase, number)
   - Password confirmation

3. **Form Validation**
   - Client-side validation for all fields
   - Age verification (18+ required)
   - Real-time error clearing

### Guide Profile Management
Guides can manage their professional information through the guide profile page (`/guide-profile`):

1. **Professional Information**
   - Bio (minimum 50 characters)
   - Hourly and daily rates
   - Response time settings
   - Availability status

2. **Specialties & Skills**
   - Add/remove specialties (e.g., Hiking, Photography, History)
   - Dynamic tag management with add/remove functionality

3. **Languages & Certifications**
   - Language proficiency management
   - Professional certifications and training
   - Dynamic list management

4. **Profile Creation/Update**
   - New guides can create their first profile
   - Existing guides can update their information
   - Real-time validation and error handling

### API Integration
Both profile systems integrate with the backend API:

- **User Profile**: `PUT /users/profile` for updates, `PUT /users/password` for password changes
- **Guide Profile**: `POST /guides` for creation, `PUT /guides/:id` for updates
- **Authentication**: All requests require valid JWT tokens
- **Error Handling**: Comprehensive error messages from backend

### Navigation Integration
- Profile links added to main navigation
- Conditional guide profile link (only visible to users with GUIDE role)
- Guide tours management link added for guides
- Mobile-responsive navigation updates

## Tour Management System

### Complete Tour CRUD Operations
Guides can now fully manage their tour offerings through the dedicated tours page (`/guide-tours`):

1. **Tour Creation**
   - Comprehensive tour form with all necessary details
   - Title, description, category, location, duration
   - Pricing, group size limits, difficulty levels
   - Dynamic arrays for includes, excludes, requirements, highlights

2. **Tour Management**
   - View all tours in a responsive grid layout
   - Edit existing tour details
   - Delete tours with confirmation
   - Tour status management (Active, Inactive, Draft)

3. **Advanced Features**
   - Real-time search across tour titles, descriptions, and locations
   - Category-based filtering (Adventure, Cultural, Nature, etc.)
   - Status-based filtering for tour management
   - Responsive design for all device sizes

### Tour Form Fields
The tour creation/editing form includes:

- **Basic Information**: Title, category, location, duration
- **Pricing & Capacity**: Price per person, maximum group size
- **Tour Details**: Description, difficulty level
- **Dynamic Arrays**: 
  - What's included (transportation, meals, etc.)
  - What's not included (personal expenses, tips)
  - Requirements (clothing, equipment, fitness level)
  - Highlights (key attractions, experiences)

### API Integration
- **Tour Creation**: `POST /tours` with comprehensive tour data
- **Tour Updates**: `PUT /tours/:id` for editing existing tours
- **Tour Deletion**: `DELETE /tours/:id` with confirmation
- **Tour Listing**: `GET /tours` filtered by guide ID
- **Authentication**: All operations require valid JWT tokens

### User Experience Features
- **Modal Forms**: Clean, focused tour creation and editing
- **Real-time Validation**: Form validation with helpful error messages
- **Dynamic Tag Management**: Add/remove items for arrays with visual feedback
- **Success Notifications**: Clear feedback for all operations
- **Responsive Grid**: Tour cards with images, details, and action buttons

## Future Enhancements

### Planned Features
1. **Email Verification**
   - Send verification email after registration
   - Require email confirmation before login

2. **Social Login Integration**
   - Google OAuth
   - GitHub OAuth

3. **Enhanced Validation**
   - Real-time password strength indicator
   - Phone number internationalization
   - Address validation

4. **User Onboarding**
   - Profile completion wizard
   - Tour preferences setup
   - Guide application flow

5. **Advanced Authentication**
   - Password reset functionality
   - Account lockout protection
   - Multi-factor authentication

6. **Profile Enhancements**
   - Profile picture upload
   - Social media links
   - Emergency contact information
   - Travel preferences and restrictions

7. **Tour Management System**
   - Complete CRUD operations for guides
   - Tour creation with comprehensive details
   - Search and filtering capabilities
   - Tour status management
   - Booking and review integration

## API Documentation Reference

The backend API endpoints are documented in:
- **Swagger UI**: `http://localhost:8080/api/v1/swagger-ui/index.html` - Interactive API documentation
- **Postman Collection**: `Sea_Tea_Tours_API.postman_collection.json` - Complete API collection
- **Backend Implementation**: `BACKEND_IMPLEMENTATION.md` - Backend implementation details

## Support

For issues or questions regarding the backend integration:
1. Check this documentation
2. Review the API configuration files
3. Test with Swagger UI at `http://localhost:8080/api/v1/swagger-ui/index.html`
4. Use the Postman collection for comprehensive testing
5. Check backend server status and logs
6. Verify authentication flow in browser DevTools
