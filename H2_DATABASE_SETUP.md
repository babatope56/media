# H2 Database Setup Guide

## Configuration Complete ✓

Your application is now configured to use **H2 in-memory database** for development and testing.

### Database Connection Details

- **Database Type:** H2 (In-Memory)
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Driver:** `org.h2.Driver`
- **Username:** `sa`
- **Password:** 

### Accessing H2 Console

Once you start the application, you can access the H2 web console at:
```
http://localhost:8080/h2-console
```

Use the following credentials in the H2 console login:
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **User Name:** `sa`
- **Password:** 

### Key Features

1. **In-Memory Database** - Data is created fresh on each application start
2. **Auto Schema Creation** - Tables are automatically created based on JPA entities using `ddl-auto: create-drop`
3. **SQL Logging** - SQL queries are logged to the console for debugging (`show-sql: true`)
4. **H2 Console** - Web UI for database inspection at `/h2-console`

### Files Modified

1. **pom.xml**
   - Added H2 dependency: `com.h2database:h2`
   - Removed Oracle JDBC driver

2. **application.yaml**
   - Changed datasource URL from Oracle to H2
   - Configured H2 console endpoint
   - Set Hibernate dialect to H2Dialect
   - Enabled DDL auto-creation

3. **DataSourceConfig.java** (NEW)
   - Created database configuration class
   - Provides DataSource bean for the application

### Running the Application

```bash
cd /Users/tope/Documents/github_repos/media
mvn clean compile
mvn spring-boot:run
```

Once running, navigate to: `http://localhost:8080/h2-console`

### Database Persistence

The H2 database uses:
- `DB_CLOSE_DELAY=-1` - Prevents database from closing while application is running
- `DB_CLOSE_ON_EXIT=false` - Allows multiple connections to the same in-memory database

All data is lost when the application stops. For persistent data, you would need to configure H2 with a file-based storage or migrate to a full database like PostgreSQL.

### Next Steps

1. The tables will be automatically created when you start the application
2. You can view table structures in the H2 console
3. Use the console to inspect data and run SQL queries
4. The API endpoints can now interact with the database

