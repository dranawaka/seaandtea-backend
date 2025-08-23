# 🚀 Sea & Tea Tours API - Postman Collection Setup Guide

This guide will help you set up and use the complete Postman collection for testing the Sea & Tea Tours backend API.

## 📋 Prerequisites

- **Postman Desktop App** (Download from [postman.com](https://www.postman.com/downloads/))
- **Sea & Tea Tours Backend** running locally on Docker
- **Git** (to clone the repository)

## 🎯 Quick Start

### 1. Import the Collection

1. Open Postman
2. Click **Import** button
3. Drag and drop `Sea_Tea_Tours_API.postman_collection.json` or click **Upload Files**
4. The collection will appear in your Collections tab

### 2. Import the Environment

1. Click **Import** button again
2. Drag and drop `Sea_Tea_Tours_Environment.postman_environment.json`
3. Select the environment from the dropdown in the top-right corner

### 3. Start the Backend

```bash
# Clone the repository (if not already done)
git clone https://github.com/dranawaka/seaandtea-backend.git
cd seaandtea-backend

# Start the application
docker-compose up -d
```

Wait for the application to start (check logs with `docker-compose logs app`)

## 🔧 Environment Variables

The collection uses the following environment variables:

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `base_url` | API base URL | `http://localhost:8080/api/v1` |
| `auth_token` | JWT authentication token | (auto-populated after login) |
| `user_id` | Current user ID | (set after user operations) |
| `tour_id` | Tour ID for testing | (set after tour operations) |
| `guide_id` | Guide ID for testing | (set after guide operations) |
| `booking_id` | Booking ID for testing | (set after booking operations) |
| `review_id` | Review ID for testing | (set after review operations) |
| `conversation_id` | Conversation ID for testing | (set after messaging operations) |
| `recipient_id` | Recipient user ID for messaging | (set after user operations) |

## 🧪 Testing Workflow

### Step 1: Health Check
Start by testing the health endpoint to ensure the API is running:

1. **Health Check** (`GET /health`)
   - Should return status 200
   - Verify the application is running

### Step 2: User Registration
Create a test user account:

1. **User Registration** (`POST /auth/register`)
   - Use the provided sample data or modify as needed
   - Note the response for user details

### Step 3: User Login
Authenticate to get a JWT token:

1. **User Login** (`POST /auth/login`)
   - Use the credentials from registration
   - The `auth_token` will be automatically set in the environment
   - Verify you receive a valid JWT token

### Step 4: Test Protected Endpoints
Now you can test endpoints that require authentication:

1. **Get Current User Profile** (`GET /users/profile`)
   - Should return the authenticated user's profile
   - Verify the Authorization header is automatically included

### Step 5: Create Test Data
Set up test data for other endpoints:

1. **Create Guide Profile** (`POST /guides`)
   - Create a guide profile for the authenticated user
   - Note the `guide_id` from the response

2. **Create Tour** (`POST /tours`)
   - Create a test tour (requires guide or admin role)
   - Note the `tour_id` from the response

### Step 6: Test Full Workflow
Test the complete booking and review workflow:

1. **Create Booking** (`POST /bookings`)
   - Use the `tour_id` and `guide_id` from previous steps
   - Note the `booking_id` from the response

2. **Create Review** (`POST /reviews`)
   - Use the `tour_id` from previous steps
   - Note the `review_id` from the response

## 📚 Collection Structure

The collection is organized into logical groups:

### 🔐 Authentication
- User Registration
- User Login

### 🏥 Health & Status
- Health Check
- Ping

### 👥 User Management
- Get Current User Profile
- Update User Profile
- Change Password

### 🏖️ Tours
- Get All Tours
- Get Tour by ID
- Search Tours
- Create Tour (Admin/Guide)
- Update Tour
- Delete Tour

### 👨‍🏫 Guides
- Get All Guides
- Get Guide by ID
- Search Guides
- Create Guide Profile
- Update Guide Profile

### 📅 Bookings
- Get User Bookings
- Get Booking by ID
- Create Booking
- Update Booking
- Cancel Booking

### ⭐ Reviews
- Get Tour Reviews
- Create Review
- Update Review
- Delete Review

### 💳 Payments
- Create Payment Intent
- Confirm Payment
- Get Payment History

### 📱 Messaging
- Get Conversations
- Get Messages in Conversation
- Send Message

### 📊 Admin
- Get All Users (Admin)
- Update User Role (Admin)
- Get System Statistics (Admin)

## 🔒 Authentication Flow

The collection includes automatic JWT token management:

1. **Login Request**: When you make a login request, the response is automatically parsed
2. **Token Extraction**: If a token is found in the response, it's automatically set as `auth_token`
3. **Automatic Headers**: All subsequent requests automatically include the `Authorization: Bearer {token}` header

## 🧪 Test Scripts

Each request includes common test scripts:

- **Status Code Check**: Verifies the response status is 200
- **Response Time Check**: Ensures response time is under 2000ms
- **Header Validation**: Confirms required headers are present

## 🚨 Common Issues & Solutions

### Issue: 403 Forbidden
**Cause**: Missing or invalid authentication token
**Solution**: 
1. Ensure you've logged in successfully
2. Check that the `auth_token` environment variable is set
3. Verify the token hasn't expired

### Issue: Connection Refused
**Cause**: Backend application not running
**Solution**:
1. Check if Docker containers are running: `docker-compose ps`
2. Start the application: `docker-compose up -d`
3. Wait for startup and check logs: `docker-compose logs app`

### Issue: 500 Internal Server Error
**Cause**: Backend application error
**Solution**:
1. Check application logs: `docker-compose logs app`
2. Verify database connection
3. Check if all required services are running

### Issue: Environment Variables Not Working
**Cause**: Environment not selected or variables not set
**Solution**:
1. Ensure the correct environment is selected in the top-right dropdown
2. Check that all required variables are set in the environment
3. Verify variable names match exactly (case-sensitive)

## 🔄 Updating the Collection

When new endpoints are added to the API:

1. **Add New Requests**: Create new request items in the appropriate folder
2. **Update Variables**: Add any new environment variables needed
3. **Test Scripts**: Include appropriate test scripts for validation
4. **Documentation**: Update this README with new endpoint information

## 📖 Additional Resources

- **API Documentation**: Available at `http://localhost:8080/api/v1/swagger-ui/` when the app is running
- **Backend Repository**: [GitHub Repository](https://github.com/dranawaka/seaandtea-backend)
- **Docker Setup**: See `README.md` in the main repository
- **Postman Learning Center**: [postman.com/learning](https://learning.postman.com/)

## 🎉 Getting Help

If you encounter issues:

1. **Check the logs**: `docker-compose logs app`
2. **Verify environment**: Ensure all variables are set correctly
3. **Test health endpoint**: Start with the basic health check
4. **Review this guide**: Follow the testing workflow step by step

---

**Happy Testing! 🚀**

The Sea & Tea Tours API collection is designed to make testing comprehensive and efficient. Follow this guide to get started and explore all the available endpoints.
