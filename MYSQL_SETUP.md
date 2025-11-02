# MySQL Database Setup for Notia

## Overview

Notia now uses **MySQL** instead of H2 for better performance, scalability, and multi-user support.

## Prerequisites

### 1. Install MySQL

**Windows:**
```powershell
# Using Chocolatey
choco install mysql

# Or download from: https://dev.mysql.com/downloads/installer/
```

**macOS:**
```bash
brew install mysql
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install mysql-server
```

### 2. Start MySQL Service

**Windows:**
```powershell
net start MySQL
```

**macOS:**
```bash
brew services start mysql
```

**Linux:**
```bash
sudo systemctl start mysql
sudo systemctl enable mysql
```

## Configuration

### Environment Variables

Notia uses environment variables for MySQL configuration. Set these before running the application:

#### Default Values:
- **Host**: `localhost`
- **Port**: `3306`
- **Database**: `notia_db`
- **User**: `root`
- **Password**: `` (empty)

#### Setting Environment Variables:

**Windows PowerShell:**
```powershell
# Required
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="your_password"

# Optional (uses defaults if not set)
$env:MYSQL_HOST="localhost"
$env:MYSQL_PORT="3306"
$env:MYSQL_DATABASE="notia_db"
```

**Windows Command Prompt:**
```cmd
set MYSQL_USER=root
set MYSQL_PASSWORD=your_password
set MYSQL_HOST=localhost
set MYSQL_PORT=3306
set MYSQL_DATABASE=notia_db
```

**macOS/Linux:**
```bash
export MYSQL_USER="root"
export MYSQL_PASSWORD="your_password"
export MYSQL_HOST="localhost"
export MYSQL_PORT="3306"
export MYSQL_DATABASE="notia_db"
```

## MySQL Setup

### 1. Secure Installation (Recommended)

```bash
mysql_secure_installation
```

Follow the prompts to:
- Set root password
- Remove anonymous users
- Disallow root login remotely
- Remove test database

### 2. Create Database (Optional)

The application automatically creates the database if it doesn't exist, but you can create it manually:

```sql
mysql -u root -p

CREATE DATABASE IF NOT EXISTS notia_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Verify
SHOW DATABASES;

-- Exit
EXIT;
```

### 3. Create Dedicated User (Recommended)

For better security, create a dedicated user for Notia:

```sql
mysql -u root -p

-- Create user
CREATE USER 'notia_user'@'localhost' IDENTIFIED BY 'strong_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON notia_db.* TO 'notia_user'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Exit
EXIT;
```

Then use these credentials:
```powershell
$env:MYSQL_USER="notia_user"
$env:MYSQL_PASSWORD="strong_password"
```

## Database Schema

The application automatically creates these tables:

### Tables Created:

1. **notes**
   - `id` (INT, AUTO_INCREMENT, PRIMARY KEY)
   - `title` (VARCHAR(255), NOT NULL)
   - `content` (TEXT)
   - `created_on` (DATE)
   - `updated_on` (DATE)
   - `is_embedded` (BOOLEAN)
   - `is_subnote` (BOOLEAN)
   - `parent_id` (INT, FOREIGN KEY)

2. **categories**
   - `id` (INT, AUTO_INCREMENT, PRIMARY KEY)
   - `name` (VARCHAR(255), NOT NULL, UNIQUE)

3. **tags**
   - `id` (INT, AUTO_INCREMENT, PRIMARY KEY)
   - `name` (VARCHAR(255), NOT NULL, UNIQUE)

4. **note_categories**
   - `note_id` (INT, FOREIGN KEY)
   - `category_id` (INT, FOREIGN KEY)
   - PRIMARY KEY (note_id, category_id)

5. **note_tags**
   - `note_id` (INT, FOREIGN KEY)
   - `tag_id` (INT, FOREIGN KEY)
   - PRIMARY KEY (note_id, tag_id)

### Features:
- UTF-8 (utf8mb4) encoding for full emoji support
- CASCADE deletion for referential integrity
- Proper indexing with PRIMARY KEYs
- UNIQUE constraints on names

## Running Notia with MySQL

### Full Setup Example:

**Terminal 1: Start ChromaDB**
```powershell
chroma run --host :: --port 8000
```

**Terminal 2: Start MySQL (if not running)**
```powershell
net start MySQL
```

**Terminal 3: Run Notia**
```powershell
# Set environment variables
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="your_password"
$env:GEMINI_API_KEY="your_gemini_key"

# Run application
mvn javafx:run
```

### Expected Output:

```
Database 'notia_db' is ready.
Database is empty. Populating with example notes...
Successfully stored note 1 in vector database
Successfully stored note 2 in vector database
...
Successfully created example notes!
```

## Connection String

The application uses this connection string:

```
jdbc:mysql://localhost:3306/notia_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
```

**Parameters:**
- `useSSL=false` - Disables SSL (enable for production)
- `allowPublicKeyRetrieval=true` - Allows key retrieval
- `serverTimezone=UTC` - Sets timezone
- `characterEncoding=UTF-8` - Full emoji support

## Troubleshooting

### Error: "Access denied for user 'root'@'localhost'"

**Solution:**
```sql
# Reset root password
sudo mysql

ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
EXIT;
```

### Error: "Unknown database 'notia_db'"

**Solution:**
The app creates it automatically, but if needed:
```sql
mysql -u root -p
CREATE DATABASE notia_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Error: "Communications link failure"

**Causes:**
- MySQL not running
- Wrong host/port
- Firewall blocking

**Solutions:**
```powershell
# Check if MySQL is running
Get-Service MySQL*

# Start MySQL
net start MySQL

# Check port
netstat -an | findstr 3306
```

### Error: "Public Key Retrieval is not allowed"

**Solution:**
Already handled in connection string with `allowPublicKeyRetrieval=true`

### Slow Performance

**Solutions:**
```sql
# Check indexes
SHOW INDEX FROM notes;

# Optimize tables
OPTIMIZE TABLE notes;
OPTIMIZE TABLE categories;
OPTIMIZE TABLE tags;

# Analyze tables
ANALYZE TABLE notes;
```

## Backup & Restore

### Backup Database:

```powershell
mysqldump -u root -p notia_db > notia_backup.sql
```

### Restore Database:

```powershell
mysql -u root -p notia_db < notia_backup.sql
```

### Backup with ChromaDB:

```powershell
# Backup MySQL
mysqldump -u root -p notia_db > notia_backup.sql

# Backup ChromaDB
Compress-Archive -Path chroma_data -DestinationPath chroma_backup.zip
```

## Viewing Data

### Using MySQL Command Line:

```sql
mysql -u root -p notia_db

-- View all notes
SELECT id, title FROM notes;

-- View note content
SELECT * FROM notes WHERE id = 1;

-- View categories
SELECT * FROM categories;

-- View tags
SELECT * FROM tags;

-- Exit
EXIT;
```

### Using MySQL Workbench:

1. Download from: https://dev.mysql.com/downloads/workbench/
2. Connect to `localhost:3306`
3. Open `notia_db` database
4. Browse tables visually

### Using DBeaver (Cross-platform):

1. Download from: https://dbeaver.io/
2. Create new MySQL connection
3. Connect to `notia_db`
4. Browse and edit data

## Migration from H2

If you have existing data in H2 (`notia_db.mv.db`):

1. **Export from H2:**
   - Use H2 Console: `http://localhost:8082`
   - Export data as SQL

2. **Import to MySQL:**
   ```powershell
   mysql -u root -p notia_db < exported_data.sql
   ```

3. **Or start fresh:**
   - Delete `notia_db.mv.db`
   - Run Notia with MySQL
   - Example notes will be created

## Production Recommendations

### 1. Enable SSL:

```sql
# Generate certificates
mysql_ssl_rsa_setup

# Update connection string
jdbc:mysql://localhost:3306/notia_db?useSSL=true&requireSSL=true&...
```

### 2. Use Strong Passwords:

```powershell
$env:MYSQL_PASSWORD="$(New-Guid)$(Get-Random)"
```

### 3. Regular Backups:

```powershell
# Create backup script
$date = Get-Date -Format "yyyyMMdd_HHmmss"
mysqldump -u root -p notia_db > "backups/notia_$date.sql"
```

### 4. Monitor Performance:

```sql
-- Show slow queries
SHOW PROCESSLIST;

-- Show table status
SHOW TABLE STATUS FROM notia_db;
```

## Benefits of MySQL

✅ **Better Performance** - Faster than H2 for large datasets
✅ **Scalability** - Handles thousands of notes
✅ **Multi-user** - Multiple instances can share database
✅ **Reliability** - Battle-tested, production-ready
✅ **Tools** - Rich ecosystem of management tools
✅ **Backup** - Professional backup/restore options
✅ **Remote Access** - Can connect from other machines

Enjoy using Notia with MySQL! 🚀
