# Database Setup Guide

The backend requires a PostgreSQL database. You have two options:

## Option 1: Using Docker (Recommended - Easiest)

1. Make sure Docker is installed and running on your machine.

2. Start the PostgreSQL container:
```bash
docker-compose up -d
```

3. The database will be available at `localhost:5432` with:
   - Username: `postgres`
   - Password: `postgres`
   - Database: `jammit`

4. Verify it's running:
```bash
docker ps
```

You should see `jammit-postgres` container running.

## Option 2: Local PostgreSQL Installation

### macOS (using Homebrew)

1. Install PostgreSQL:
```bash
brew install postgresql@15
brew services start postgresql@15
```

2. Create the database:
```bash
createdb jammit
```

3. Verify connection:
```bash
psql -d jammit -c "SELECT version();"
```

### Linux (Ubuntu/Debian)

1. Install PostgreSQL:
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

2. Start PostgreSQL service:
```bash
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

3. Create database and user:
```bash
sudo -u postgres psql
```

Then in the PostgreSQL prompt:
```sql
CREATE DATABASE jammit;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE jammit TO postgres;
\q
```

### Windows

1. Download and install PostgreSQL from: https://www.postgresql.org/download/windows/
2. During installation, set password to `postgres` (or update `.env` file)
3. Use pgAdmin or command line to create database:
```sql
CREATE DATABASE jammit;
```

## Environment Configuration

1. Copy the example environment file:
```bash
cp .env.example .env
```

2. Update `.env` with your database credentials if different from defaults:
```
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_NAME=jammit
```

## Verify Connection

After setting up the database, start the NestJS server:
```bash
npm run start:dev
```

You should see:
- No connection errors
- "Application is running on: http://localhost:3000"

## Troubleshooting

### Connection Refused Error

1. **Check if PostgreSQL is running:**
   - Docker: `docker ps` (should see jammit-postgres)
   - macOS: `brew services list` (should see postgresql@15 started)
   - Linux: `sudo systemctl status postgresql`

2. **Check if port 5432 is available:**
   ```bash
   lsof -i :5432
   # or
   netstat -an | grep 5432
   ```

3. **Verify database exists:**
   ```bash
   psql -U postgres -l | grep jammit
   ```

4. **Test connection manually:**
   ```bash
   psql -h localhost -U postgres -d jammit
   ```

### Reset Database (Docker)

If you need to start fresh:
```bash
docker-compose down -v
docker-compose up -d
```

This will delete all data and recreate the database.
