# Postman Setup Guide for Sea & Tea Tours API

This guide will help you set up Postman to test the Sea & Tea Tours Backend API.

## 📥 Import Files

1. **Import the API Collection:**
   - Open Postman
   - Click "Import" button
   - Import `Sea_Tea_Tours_API.postman_collection.json`

2. **Import Environment Files:**
   - Import `Sea_Tea_Tours_Environment.postman_environment.json` (Railway Production)
   - Import `Sea_Tea_Tours_Local.postman_environment.json` (Local Development)

## 🌍 Environment Setup

### Railway Production Environment
- **Name:** Sea & Tea Tours - Railway Production
- **Base URL:** `https://your-railway-domain.railway.app/api/v1`
- **Use this for:** Testing the deployed application

### Local Development Environment
- **Name:** Sea & Tea Tours - Local Development  
- **Base URL:** `http://localhost:8080/api/v1`
- **Use this for:** Testing locally (requires Maven and local database)

## 🔧 Environment Variables

| Variable | Description | Auto-populated |
|----------|-------------|----------------|
| `base_url` | API base URL | ✅ Yes |
| `auth_token` | JWT authentication token | ✅ Yes (after login) |
| `user_id` | Current user ID | ❌ Manual |
| `guide_id` | Guide profile ID | ❌ Manual |

## 🚀 Quick Start

1. **Select Environment:** Choose "Sea & Tea Tours - Railway Production"
2. **Test Health Endpoint:** Run "Health Check" to verify API is accessible
3. **Register User:** Use "User Registration" to create an account
4. **Login:** Use "User Login" to get authentication token
5. **Test Protected Endpoints:** The token will auto-populate for authenticated requests

## 📋 Available Endpoints

### 🔐 Authentication
- `POST /auth/register` - User registration
- `POST /auth/login` - User login

### 🏥 Health & Status
- `GET /health` - Application health check
- `GET /health/ping` - Simple ping endpoint
- `GET /actuator/health` - Spring Boot Actuator health

### 👥 User Management
- `GET /users/profile` - Get current user profile
- `PUT /users/profile` - Update user profile
- `PUT /users/password` - Change password
- `PUT /users/role` - Update user role

### 👨‍🏫 Guides
- `POST /guides` - Create guide profile
- `GET /guides/{id}` - Get guide by ID
- `GET /guides/my-profile` - Get my guide profile
- `PUT /guides/my-profile` - Update my guide profile
- `PUT /guides/{id}` - Update guide by ID (admin)
- `DELETE /guides/my-profile` - Delete my guide profile
- `DELETE /guides/{id}` - Delete guide by ID (admin)
- `GET /guides/my-profile/exists` - Get guide profile if exists (returns profile or 404)

## 🔑 Authentication Flow

1. **Register:** Create a new user account
2. **Login:** Authenticate and receive JWT token
3. **Token Auto-population:** Postman automatically sets the `auth_token` variable
4. **Protected Requests:** Include `Authorization: Bearer {token}` header

## 📝 Testing Workflow

### 1. Health Check
```
GET {{base_url}}/health
```
**Expected:** 200 OK with health status

### 2. User Registration
```
POST {{base_url}}/auth/register
Body: {
  "email": "test@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "dateOfBirth": "1990-01-01",
  "nationality": "US"
}
```
**Expected:** 200 OK with user details

### 3. User Login
```
POST {{base_url}}/auth/login
Body: {
  "email": "test@example.com",
  "password": "password123"
}
```
**Expected:** 200 OK with JWT token

### 4. Create Guide Profile
```
POST {{base_url}}/guides
Headers: Authorization: Bearer {{auth_token}}
Body: {
  "bio": "Experienced hiking guide",
  "specialties": ["HIKING", "NATURE"],
  "languages": ["ENGLISH"],
  "certifications": ["Wilderness First Aid"],
  "experience": 5,
  "hourlyRate": 45.00
}
```
**Expected:** 201 Created with guide profile

### 5. Get My Guide Profile
```
GET {{base_url}}/guides/my-profile
Headers: Authorization: Bearer {{auth_token}}
```
**Expected:** 200 OK with guide profile

## 🐛 Troubleshooting

### Common Issues

1. **401 Unauthorized:**
   - Check if `auth_token` is set
   - Verify token hasn't expired
   - Re-run login request

2. **404 Not Found:**
   - Verify `base_url` is correct
   - Check if endpoint path is correct
   - Ensure application is running

3. **500 Internal Server Error:**
   - Check application logs
   - Verify database connection
   - Check request payload format

### Environment Switching

- **Local Development:** Use "Sea & Tea Tours - Local Development"
- **Railway Production:** Use "Sea & Tea Tours - Railway Production"
- **Update base_url:** Modify environment variable as needed

## 📚 Additional Resources

- **API Documentation:** Swagger UI at `/swagger-ui/` (when implemented)
- **Health Monitoring:** Actuator endpoints for system health
- **Error Handling:** Check `/error` endpoint for detailed error information

## 🔄 Collection Updates

The collection automatically:
- Sets `auth_token` after successful login
- Includes proper headers for each request type
- Provides sample request bodies
- Handles authentication flow

## 🎯 Next Steps

1. **Test all endpoints** to ensure they work correctly
2. **Create test data** for comprehensive testing
3. **Set up automated testing** using Postman's test scripts
4. **Monitor performance** using Postman's response time metrics

---

**Note:** Replace `your-railway-domain.railway.app` with your actual Railway domain in the environment variables.
