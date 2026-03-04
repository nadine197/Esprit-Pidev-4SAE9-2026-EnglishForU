# EnglishForU Backend - Docker Setup

## Services Architecture

This Docker setup includes:
- **PostgreSQL**: Database server (Port 5432)
- **Eureka Server**: Service discovery (Port 8761)
- **User Management**: User service with authentication (Port 8081)

## Prerequisites

- Docker Desktop installed and running
- At least 4GB RAM allocated to Docker
- Ports 5432, 8761, and 8081 available

## Quick Start

### Production Environment

```powershell
# Navigate to backend directory
cd backend

# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

### Development Environment

```powershell
# Use development configuration
docker-compose -f docker-compose.dev.yml up --build
```

## Service URLs

- **Eureka Dashboard**: http://localhost:8761
- **User Management API**: http://localhost:8081
- **PostgreSQL**: localhost:5432
  - Database: `GestionUserPI`
  - Username: `postgres`
  - Password: `inception722`

## Useful Commands

### Container Management

```powershell
# Stop all services
docker-compose down

# Stop and remove volumes (clean state)
docker-compose down -v

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f user-management

# Restart a specific service
docker-compose restart user-management
```

### Build Commands

```powershell
# Rebuild without cache
docker-compose build --no-cache

# Rebuild specific service
docker-compose build eureka-server
```

### Database Commands

```powershell
# Connect to PostgreSQL
docker exec -it englishforu-postgres psql -U postgres -d GestionUserPI

# Backup database
docker exec englishforu-postgres pg_dump -U postgres GestionUserPI > backup.sql

# Restore database
docker exec -i englishforu-postgres psql -U postgres GestionUserPI < backup.sql
```

## Environment Variables

You can override environment variables by creating a `.env` file in the backend directory:

```env
POSTGRES_DB=GestionUserPI
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret
GOOGLE_CLIENT_ID=your_google_client_id
```

## Troubleshooting

### Service won't start
```powershell
# Check service status
docker-compose ps

# Check service logs
docker-compose logs service-name
```

### Port already in use
```powershell
# Find process using port (e.g., 8081)
netstat -ano | findstr :8081

# Kill the process
taskkill /PID <PID> /F
```

### Database connection issues
```powershell
# Verify PostgreSQL is running
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres
```

## Network Configuration

All services are connected via the `englishforu-network` bridge network, allowing inter-service communication using service names as hostnames.

## Health Checks

- **Eureka Server**: Checks every 30s (starts after 60s)
- **User Management**: Checks every 30s (starts after 120s)
- **PostgreSQL**: Checks every 10s

## Volume Management

PostgreSQL data is persisted in a Docker volume:
- Production: `postgres_data`
- Development: `postgres_dev_data`

To remove volumes:
```powershell
docker volume rm backend_postgres_data
```
