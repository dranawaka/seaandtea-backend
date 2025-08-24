# 🌍 Environment Setup Guide

## 📋 **Two Environments Configured**

Your application now has two distinct environments:

1. **`dev`** - Local Development Environment
2. **`railway`** - Railway Production Environment

## 🔧 **Environment Configuration Files**

### **Main Configuration**
- `src/main/resources/application.yml` - Common settings and default profile
- `src/main/resources/application-dev.yml` - Development-specific configuration
- `src/main/resources/application-railway.yml` - Railway production configuration

### **Environment Variables**
- `env.dev` - Development environment template
- `env.railway` - Railway production environment template

## 🚀 **How to Use Each Environment**

### **1. Development Environment (`dev`)**

#### **Start Local Development**
```bash
# Set profile to dev (default)
export SPRING_PROFILES_ACTIVE=dev

# Or run with profile
mvn spring-boot:run -Dspring.profiles.active=dev
```

#### **Local Database Setup**
```bash
# Create local database
createdb seaandtea_dev

# Or using psql
psql -U postgres
CREATE DATABASE seaandtea_dev;
```

#### **Development Features**
- ✅ **Local PostgreSQL** on `localhost:5432`
- ✅ **Debug logging** enabled
- ✅ **SQL queries** visible in console
- ✅ **H2 fallback** available if PostgreSQL not running
- ✅ **Development JWT secret** (change in production)

### **2. Railway Environment (`railway`)**

#### **Deploy to Railway**
```bash
# Deploy with railway profile
railway up

# Check environment variables
railway variables

# View logs
railway logs
```

#### **Railway Database**
- ✅ **Persistent PostgreSQL** on Railway
- ✅ **Automatic backups** handled by Railway
- ✅ **Production-grade** database
- ✅ **Environment variables** managed by Railway

## 🔄 **Profile Switching**

### **Automatic Profile Selection**

| Environment | Profile | How It's Set |
|-------------|---------|--------------|
| **Local Development** | `dev` | Default in `application.yml` |
| **Railway Deployment** | `railway` | Set in `railway.json` and `railway.toml` |

### **Manual Profile Override**
```bash
# Override profile for local development
mvn spring-boot:run -Dspring.profiles.active=dev

# Override profile for testing
mvn spring-boot:run -Dspring.profiles.active=railway

# Set environment variable
export SPRING_PROFILES_ACTIVE=dev
```

## 📊 **Environment Differences**

| Setting | Dev | Railway |
|---------|-----|---------|
| **Database** | `localhost:5432` | `tramway.proxy.rlwy.net:59578` |
| **Logging** | `DEBUG` | `INFO` |
| **SQL Output** | `true` | `false` |
| **DDL Mode** | `validate` | `update` |
| **S3 Bucket** | `seaandtea-uploads-dev` | `seaandtea-uploads` |
| **JWT Secret** | Dev default | Environment variable |

## 🛠️ **Setup Commands**

### **Development Setup**
```bash
# 1. Copy environment template
cp env.dev .env.dev

# 2. Update values in .env.dev
nano .env.dev

# 3. Start local database
docker run -d --name postgres-dev -e POSTGRES_PASSWORD=password -e POSTGRES_DB=seaandtea_dev -p 5432:5432 postgres:15

# 4. Start application
mvn spring-boot:run
```

### **Railway Setup**
```bash
# 1. Link to Railway project
railway link

# 2. Set environment variables
railway variables set SPRING_PROFILES_ACTIVE=railway
railway variables set JWT_SECRET=your-production-secret

# 3. Deploy
railway up
```

## 🔍 **Verification**

### **Check Active Profile**
```bash
# View logs to see which profile is active
tail -f logs/spring.log

# Or check environment
echo $SPRING_PROFILES_ACTIVE
```

### **Database Connection**
```bash
# Dev environment
psql -h localhost -U postgres -d seaandtea_dev

# Railway environment
psql "postgresql://postgres:yPHavvxcXnHQzwrXQtrEqMcraqyzQETf@tramway.proxy.rlwy.net:59578/railway"
```

## 🚨 **Important Notes**

### **Data Persistence**
- ✅ **Development**: Local database persists between restarts
- ✅ **Railway**: Database persists independently of application
- ✅ **Both**: Use `ddl-auto: validate` (safe, no data loss)

### **Security**
- 🔒 **Development**: Use test/development keys
- 🔒 **Railway**: Use production keys (set in Railway dashboard)
- 🔒 **JWT**: Different secrets for each environment

### **Configuration**
- ⚙️ **Development**: Local files and environment variables
- ⚙️ **Railway**: Railway environment variables and `railway.json`

## 🔗 **Related Files**

- `src/main/resources/application.yml` - Common configuration
- `src/main/resources/application-dev.yml` - Development profile
- `src/main/resources/application-railway.yml` - Railway profile
- `railway.json` - Railway deployment config
- `railway.toml` - Alternative Railway config
- `env.dev` - Development environment template
- `env.railway` - Railway environment template

## ✅ **Summary**

You now have a clean separation between:
- **Local development** with `dev` profile
- **Railway production** with `railway` profile

Each environment has its own database, configuration, and settings, ensuring no conflicts between development and production!
