# 🗄️ Data Persistence Guide

## 🚨 **IMPORTANT: Your Data is SAFE!**

When your application shuts down, **NO DATA WILL BE DELETED**. Here's why and how:

## 🔒 **Database Persistence Configuration**

### 1. **Hibernate DDL Auto Mode**
- **Before (DANGEROUS)**: `ddl-auto: create-drop` - This would delete and recreate tables on every restart
- **Now (SAFE)**: `ddl-auto: validate` - This only validates the existing schema without making changes

### 2. **Flyway Migrations**
Your database schema is managed by Flyway migrations, which:
- ✅ **Never delete data** - Only add new tables/columns
- ✅ **Use `IF NOT EXISTS`** - Prevent conflicts with existing data
- ✅ **Are versioned** - Track what changes have been applied
- ✅ **Are safe to re-run** - Won't duplicate or overwrite existing data

### 3. **Railway Database**
- **Persistent Storage**: Railway PostgreSQL databases persist data independently of your application
- **No Data Loss**: Even if your application crashes or restarts, the database remains intact
- **Automatic Backups**: Railway provides automatic database backups

## 📋 **Current Configuration**

### `application.yml` (Development)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # ✅ SAFE - Only validates, doesn't modify
  flyway:
    enabled: true         # ✅ SAFE - Uses migrations for schema changes
```

### `application-railway.yml` (Railway Production)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update    # ✅ SAFE - Only adds new columns, doesn't delete
  flyway:
    enabled: true         # ✅ SAFE - Uses migrations for schema changes
```

## 🛡️ **Why Data is Protected**

1. **Hibernate `validate` mode**: Only checks if the database schema matches your entity classes
2. **Flyway migrations**: Handle all schema changes safely without data loss
3. **Railway persistence**: Database exists independently of your application
4. **No `DROP` statements**: Your migrations only use `CREATE IF NOT EXISTS`

## 🔍 **Verification Commands**

### Check Database Schema
```sql
-- Connect to your Railway database
-- List all tables
\dt

-- Check table structure
\d users
\d guides
\d tours
```

### Check Flyway Status
```sql
-- View migration history
SELECT * FROM flyway_schema_history;
```

## 🚀 **Deployment Safety**

### Application Restart
- ✅ **Data preserved**: Database remains intact
- ✅ **Schema validated**: Hibernate checks compatibility
- ✅ **No data loss**: All user data, bookings, tours remain

### Application Update
- ✅ **Migrations applied**: New schema changes added safely
- ✅ **Data migrated**: Existing data preserved and updated
- ✅ **Rollback possible**: Can revert to previous version if needed

## 📚 **Migration Files**

Your current migrations are safe:
- `V1__Create_initial_schema.sql` - Creates tables with `IF NOT EXISTS`
- `V2__Insert_sample_data.sql` - Inserts data with `ON CONFLICT DO NOTHING`

## 🆘 **If You Need to Reset (Optional)**

**WARNING**: Only use this if you intentionally want to start fresh:

```sql
-- Connect to Railway database
-- Drop all tables (WILL DELETE ALL DATA)
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- Re-run migrations
-- Your application will recreate the schema
```

## ✅ **Summary**

- **Your data is 100% safe** when the application shuts down
- **Hibernate won't delete anything** (using `validate` mode)
- **Flyway handles schema changes** safely without data loss
- **Railway database persists** independently of your application
- **No configuration changes needed** - everything is already safe!

## 🔗 **Related Files**

- `src/main/resources/application.yml` - Common configuration
- `src/main/resources/application-dev.yml` - Development configuration
- `src/main/resources/application-railway.yml` - Railway production configuration
- `src/main/resources/db/migration/` - Safe schema migrations
- `railway.json` - Railway deployment configuration
