# 🚂 Railway Deployment Guide

This guide explains how to deploy the Sea & Tea Backend to Railway using the internal database endpoint.

## 🎯 **Overview**

Railway is a modern platform that makes it easy to deploy applications with built-in database support. We'll use Railway's internal endpoint `postgres.railway.internal` for secure database connections.

## 📋 **Prerequisites**

- [Railway CLI](https://docs.railway.app/develop/cli) installed
- Railway account created
- PostgreSQL database added to your Railway project

## 🚀 **Quick Deployment**

### **Option 1: Using the Deployment Script**

```bash
# Run the deployment script
./deploy-railway.ps1
```

### **Option 2: Manual Deployment**

```bash
# 1. Build the application
mvn clean package -DskipTests

# 2. Deploy to Railway
railway up --detach
```

## ⚙️ **Configuration**

### **Database Configuration**

The application is configured to use Railway's internal database endpoint with both individual variables and a complete DATABASE_URL:

```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://${DB_HOST:postgres.railway.internal}:${DB_PORT:5432}/${DB_NAME:railway}}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD}
```

**Current Railway Configuration:**
- **DATABASE_URL**: `postgresql://postgres:SIBfwvrqlFipfkvaqRkIZzvARpfEIoti@hopper.proxy.rlwy.net:23545/railway`
- **DB_HOST**: `hopper.proxy.rlwy.net`
- **DB_PORT**: `23545`
- **DB_NAME**: `railway`
- **DB_USERNAME**: `postgres`
- **DB_PASSWORD**: `SIBfwvrqlFipfkvaqRkIZzvARpfEIoti`

### **Environment Variables**

Set these in your Railway project dashboard:

#### **Required Variables**
- `DB_PASSWORD` - Your PostgreSQL database password
- `JWT_SECRET` - A secure random string for JWT tokens
- `MAIL_USERNAME` - Your Gmail address
- `MAIL_PASSWORD` - Your Gmail app password

#### **Optional Variables**
- `STRIPE_SECRET_KEY` - Your Stripe secret key
- `STRIPE_PUBLISHABLE_KEY` - Your Stripe publishable key
- `STRIPE_WEBHOOK_SECRET` - Your Stripe webhook secret
- `AWS_ACCESS_KEY` - Your AWS access key
- `AWS_SECRET_KEY` - Your AWS secret key
- `AWS_S3_BUCKET` - Your S3 bucket name (default: seaandtea-uploads)
- `AWS_REGION` - Your AWS region (default: us-east-1)

## 🔧 **Railway Configuration File**

The `railway.json` file contains:

```json
{
  "build": {
    "builder": "NIXPACKS"
  },
  "deploy": {
    "startCommand": "java -jar target/*.jar",
    "healthcheckPath": "/api/v1/actuator/health",
    "healthcheckTimeout": 300
  },
  "variables": {
    "DB_HOST": "postgres.railway.internal",
    "DB_PORT": "5432",
    "DB_NAME": "railway",
    "SPRING_PROFILES_ACTIVE": "prod"
  }
}
```

## 📊 **Production Profile**

The `application-prod.yml` profile is optimized for Railway:

- Uses `postgres.railway.internal` as database host
- Sets `ddl-auto: update` for production safety
- Configures proper logging levels
- Uses Railway's `PORT` environment variable

## 🗄️ **Database Setup**

### **1. Add PostgreSQL Database**
1. Go to Railway dashboard
2. Click **"New"** → **"Database"** → **"Add PostgreSQL"**
3. Wait for database creation

### **2. Get Connection Details**
Railway automatically sets these environment variables:
- `DB_HOST` = `postgres.railway.internal`
- `DB_PORT` = `5432`
- `DB_NAME` = `railway`
- `DB_USERNAME` = `postgres`
- `DB_PASSWORD` = (auto-generated)

## 🔍 **Monitoring & Debugging**

### **Check Deployment Status**
```bash
railway status
```

### **View Logs**
```bash
railway logs
```

### **Open Application**
```bash
railway open
```

## 🚨 **Troubleshooting**

### **Common Issues**

#### **1. Database Connection Failed**
- Ensure PostgreSQL database is added to Railway
- Check `DB_PASSWORD` environment variable is set
- Verify database is running in Railway dashboard

#### **2. Build Failed**
- Ensure Maven is installed: `mvn --version`
- Check Java version: `java --version`
- Verify all dependencies are available

#### **3. Application Won't Start**
- Check Railway logs: `railway logs`
- Verify all required environment variables are set
- Check health check endpoint: `/api/v1/actuator/health`

### **Health Check**

The application includes a health check endpoint:
```
GET /api/v1/actuator/health
```

Railway uses this for deployment monitoring.

## 💰 **Cost Considerations**

- **Free Tier**: Limited deployments and database usage
- **Pro Plan**: $20/month for unlimited deployments
- **Database**: Additional cost based on usage

## 🔒 **Security Features**

- Internal database endpoint (`postgres.railway.internal`)
- Environment variable-based configuration
- JWT-based authentication
- Secure database connections

## 📚 **Additional Resources**

- [Railway Documentation](https://docs.railway.app/)
- [Railway CLI Reference](https://docs.railway.app/develop/cli)
- [Spring Boot on Railway](https://docs.railway.app/deploy/deployments/railway-up)

## 🎉 **Success!**

Once deployed, your application will be available at:
```
https://your-app-name.railway.app/api/v1
```

The database will be accessible via the internal endpoint `postgres.railway.internal` for secure, fast connections within Railway's network.
