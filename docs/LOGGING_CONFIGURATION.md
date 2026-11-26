# 📝 Logging Configuration Guide

## 🚨 **Important: Logging Level Values**

Spring Boot logging levels expect **string values**, not boolean values. Here are the correct values:

## ✅ **Valid Log Levels**

| Log Level | Description | Usage |
|-----------|-------------|-------|
| `TRACE` | Most detailed logging | Development debugging |
| `DEBUG` | Detailed debugging info | Development |
| `INFO` | General information | Production default |
| `WARN` | Warning messages | Production |
| `ERROR` | Error messages only | Production |
| `OFF` | No logging | Disable logging |

## ❌ **Invalid Values (Will Cause Errors)**

```yaml
# WRONG - These will cause binding errors
logging:
  level:
    org.hibernate.SQL: true        # ❌ Boolean not allowed
    org.hibernate.SQL: false       # ❌ Boolean not allowed
    org.hibernate.SQL: "true"      # ❌ String "true" not a log level
    org.hibernate.SQL: "false"     # ❌ String "false" not a log level
```

## ✅ **Correct Configuration**

### **Development Profile (`application-dev.yml`)**
```yaml
logging:
  level:
    com.seaandtea: DEBUG                    # ✅ Application logging
    org.springframework.security: DEBUG     # ✅ Security logging
    org.hibernate.SQL: DEBUG                # ✅ SQL queries visible
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # ✅ Parameter values
```

### **Railway Production Profile (`application-railway.yml`)**
```yaml
logging:
  level:
    com.seaandtea: INFO                     # ✅ Production logging
    org.springframework.security: INFO      # ✅ Security info only
    org.hibernate.SQL: INFO                 # ✅ Minimal SQL info
    org.hibernate.type.descriptor.sql.BasicBinder: INFO   # ✅ Minimal parameter info
```

## 🔧 **Common Logging Categories**

### **Application Logging**
```yaml
logging:
  level:
    com.seaandtea: DEBUG                    # Your application classes
    com.seaandtea.controller: DEBUG         # Controller classes only
    com.seaandtea.service: DEBUG            # Service classes only
    com.seaandtea.repository: DEBUG         # Repository classes only
```

### **Framework Logging**
```yaml
logging:
  level:
    org.springframework: INFO               # Spring Framework
    org.springframework.security: DEBUG     # Spring Security
    org.springframework.web: DEBUG          # Spring Web
    org.springframework.data: DEBUG         # Spring Data
```

### **Database Logging**
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG                # SQL queries
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # Query parameters
    org.hibernate.stat: DEBUG               # Hibernate statistics
    org.hibernate.engine: DEBUG             # Hibernate engine
```

### **External Library Logging**
```yaml
logging:
  level:
    org.postgresql: INFO                    # PostgreSQL driver
    com.zaxxer.hikari: INFO                # Connection pool
    org.flywaydb: INFO                      # Database migrations
    org.springframework.boot: INFO          # Spring Boot
```

## 🎯 **Recommended Configurations**

### **Local Development**
```yaml
logging:
  level:
    com.seaandtea: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.springframework.web: DEBUG
```

### **Production (Railway)**
```yaml
logging:
  level:
    com.seaandtea: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
    org.hibernate.type.descriptor.sql.BasicBinder: WARN
    org.springframework.web: INFO
```

### **Testing**
```yaml
logging:
  level:
    com.seaandtea: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.springframework.test: DEBUG
```

## 🚀 **Quick Fix Commands**

If you encounter logging binding errors:

```bash
# Check current logging configuration
grep -r "logging.level" src/main/resources/

# Fix boolean values to proper log levels
# Replace:
#   org.hibernate.SQL: true   →   org.hibernate.SQL: DEBUG
#   org.hibernate.SQL: false  →   org.hibernate.SQL: INFO
```

## 🔍 **Verification**

### **Check Logging in Application**
```bash
# Start application and check logs
mvn spring-boot:run

# Look for logging configuration in startup logs
# Should see: "Logging level for 'com.seaandtea' set to DEBUG"
```

### **Test Different Levels**
```yaml
# Test with different levels to see output
logging:
  level:
    com.seaandtea: TRACE    # Most verbose
    com.seaandtea: DEBUG    # Detailed
    com.seaandtea: INFO     # Normal
    com.seaandtea: WARN     # Warnings only
    com.seaandtea: ERROR    # Errors only
```

## ✅ **Summary**

- **Use log levels**: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `OFF`
- **Never use booleans**: `true`/`false` will cause binding errors
- **Development**: Use `DEBUG`/`TRACE` for detailed logging
- **Production**: Use `INFO`/`WARN` for minimal logging
- **Check your configs**: Ensure all logging levels are valid strings
