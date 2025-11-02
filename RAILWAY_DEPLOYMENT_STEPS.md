# 🚂 Railway Deployment Steps - Updated Database Schema

## 🎯 **What's Been Updated**

### **1. Database Schema Migrations**
- ✅ **V1__Create_initial_schema.sql** - Creates all necessary tables
- ✅ **V2__Insert_sample_data.sql** - Inserts sample data for testing
- ✅ **Flyway Configuration** - Updated for Railway deployment

### **2. Railway Configuration**
- ✅ **railway.json** - Complete configuration with database credentials
- ✅ **application-prod.yml** - Production profile for Railway
- ✅ **Database URL** - Configured with Railway internal endpoint

## 🚀 **Deployment Steps**

### **Step 1: Verify Railway Project Link**
```bash
railway status
```

If not linked:
```bash
railway link
```

### **Step 2: Build Application (Optional - Railway can build automatically)**
```bash
mvn clean package -DskipTests
```

### **Step 3: Deploy to Railway**
```bash
railway up --detach
```

### **Step 4: Monitor Deployment**
```bash
railway logs
```

### **Step 5: Open Application**
```bash
railway open
```

## 🗄️ **Database Schema Details**

### **Tables Created:**
1. **users** - User accounts and profiles
2. **guides** - Guide profiles and ratings
3. **guide_specialties** - Guide expertise areas
4. **guide_languages** - Guide language skills
5. **tours** - Tour offerings and details
6. **tour_images** - Tour photos and media
7. **bookings** - Tour reservations
8. **reviews** - Customer feedback and ratings
9. **payments** - Payment transactions
10. **messages** - Communication between users

### **Sample Data Included:**
- **Admin User**: admin@seaandtea.com (password: password)
- **Guide Users**: guide1@seaandtea.com, guide2@seaandtea.com
- **Tourist Users**: tourist1@example.com, tourist2@example.com
- **Sample Tours**: Ceylon Tea Experience, Beach Adventure & Marine Life
- **Sample Bookings**: Confirmed and pending reservations
- **Sample Reviews**: Customer feedback

## 🔧 **Environment Variables Set**

### **Database Configuration (Already Set):**
- `DATABASE_URL` = postgresql://postgres:SIBfwvrqlFipfkvaqRkIZzvARpfEIoti@hopper.proxy.rlwy.net:23545/railway
- `DB_HOST` = hopper.proxy.rlwy.net
- `DB_PORT` = 23545
- `DB_NAME` = railway
- `DB_USERNAME` = postgres
- `DB_PASSWORD` = SIBfwvrqlFipfkvaqRkIZzvARpfEIoti
- `SPRING_PROFILES_ACTIVE` = prod

### **Required Variables to Set:**
- `JWT_SECRET` - Generate a secure random string
- `MAIL_USERNAME` - Your Gmail address
- `MAIL_PASSWORD` - Your Gmail app password

### **Optional Variables:**
- `STRIPE_SECRET_KEY` - Your Stripe secret key
- `STRIPE_PUBLISHABLE_KEY` - Your Stripe publishable key
- `STRIPE_WEBHOOK_SECRET` - Your Stripe webhook secret
- `AWS_ACCESS_KEY` - Your AWS access key
- `AWS_SECRET_KEY` - Your AWS secret key

## 🔍 **Verification Steps**

### **1. Check Application Health**
```
GET /api/v1/actuator/health
```

### **2. Test Database Connection**
- Check Railway logs for successful Flyway migrations
- Verify tables are created in Railway PostgreSQL database

### **3. Test Sample Endpoints**
- **Health Check**: `/api/v1/actuator/health`
- **Users**: `/api/v1/users` (if implemented)
- **Guides**: `/api/v1/guides` (if implemented)

## 🚨 **Troubleshooting**

### **Common Issues:**

#### **1. Database Connection Failed**
- Verify PostgreSQL database is running in Railway
- Check environment variables are set correctly
- Ensure database password is correct

#### **2. Flyway Migration Failed**
- Check Railway logs for specific error messages
- Verify database user has CREATE TABLE permissions
- Check if tables already exist

#### **3. Application Won't Start**
- Check Railway logs: `railway logs`
- Verify all required environment variables are set
- Check health check endpoint

### **Useful Commands:**
```bash
# Check deployment status
railway status

# View real-time logs
railway logs --follow

# Check environment variables
railway variables

# Restart deployment
railway restart
```

## 🎉 **Success Indicators**

### **Deployment Successful When:**
- ✅ Application builds without errors
- ✅ Database connection established
- ✅ Flyway migrations completed successfully
- ✅ Health check endpoint responds
- ✅ Application accessible via Railway URL

### **Expected URL Format:**
```
https://your-app-name.railway.app/api/v1
```

## 📚 **Next Steps After Deployment**

1. **Test API Endpoints** - Verify all functionality works
2. **Set Production Variables** - Configure JWT, email, and other services
3. **Monitor Performance** - Check Railway metrics and logs
4. **Scale if Needed** - Adjust resources based on usage
5. **Set Up Monitoring** - Configure alerts and monitoring

## 🔒 **Security Features**

- **Internal Database**: Uses Railway's `postgres.railway.internal` endpoint
- **Environment Variables**: Secure configuration management
- **JWT Authentication**: Secure user authentication
- **Database Triggers**: Automatic timestamp updates
- **Indexes**: Optimized database performance

Your application is now ready for Railway deployment with a complete database schema! 🚀
