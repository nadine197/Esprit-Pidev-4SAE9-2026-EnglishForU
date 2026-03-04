# EnglishForU - Complete Docker Stack

## Full Stack Architecture

This Docker setup includes the complete EnglishForU application:

### Backend Services
- **PostgreSQL**: Database server (Port 5432)
- **Eureka Server**: Service discovery (Port 8761)
- **User Management**: User service with authentication (Port 8081)

### Frontend
- **Angular Application**: SPA with Tailwind CSS (Port 4200)

## Prerequisites

- Docker Desktop installed and running
- At least 4GB RAM allocated to Docker
- Ports 4200, 5432, 8761, and 8081 available

## Quick Start

### Run Complete Stack (Production)

From the project root directory:

```powershell
# Build and start all services (frontend + backend)
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

### Run Individual Stacks

**Backend Only:**
```powershell
cd backend
docker-compose up --build
```

**Frontend Only:**
```powershell
cd frontend
docker-compose up --build
```

## Access Points

- **Frontend Application**: http://localhost:4200
- **Eureka Dashboard**: http://localhost:8761
- **User Management API**: http://localhost:8081
- **PostgreSQL**: localhost:5432

## Development Mode

For development with hot reload:

**Frontend:**
```powershell
cd frontend
docker-compose -f docker-compose.dev.yml up --build
```

**Backend:**
```powershell
cd backend
docker-compose -f docker-compose.dev.yml up --build
```

## Service Startup Order

The services start in the following order:
1. PostgreSQL (waits for health check)
2. Eureka Server (waits for health check)
3. User Management (depends on PostgreSQL and Eureka)
4. Frontend (depends on User Management)

## Common Commands

### View All Services Status
```powershell
docker-compose ps
```

### View Logs
```powershell
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f frontend
docker-compose logs -f user-management
docker-compose logs -f eureka-server
docker-compose logs -f postgres
```

### Stop All Services
```powershell
docker-compose down
```

### Stop and Remove All Data
```powershell
docker-compose down -v
```

### Restart Specific Service
```powershell
docker-compose restart frontend
docker-compose restart user-management
```

### Rebuild Without Cache
```powershell
docker-compose build --no-cache
```

## Database Management

### Connect to PostgreSQL
```powershell
docker exec -it englishforu-postgres psql -U postgres -d GestionUserPI
```

### Backup Database
```powershell
docker exec englishforu-postgres pg_dump -U postgres GestionUserPI > backup.sql
```

### Restore Database
```powershell
docker exec -i englishforu-postgres psql -U postgres GestionUserPI < backup.sql
```

## Troubleshooting

### Check Service Health
```powershell
docker-compose ps
docker inspect englishforu-user-service --format='{{.State.Health.Status}}'
```

### Service Won't Start
```powershell
# Check logs for errors
docker-compose logs service-name

# Restart the service
docker-compose restart service-name
```

### Port Already in Use
```powershell
# Find process using port
netstat -ano | findstr :4200
netstat -ano | findstr :8081

# Kill the process
taskkill /PID <PID> /F
```

### Clear Everything and Start Fresh
```powershell
# Stop and remove containers, networks, and volumes
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Rebuild and start
docker-compose up --build
```

## Network Configuration

All services communicate through the `englishforu-network` bridge network:
- Services can reference each other by service name
- Frontend → `http://user-management:8081`
- User Management → `http://eureka-server:8761`
- User Management → `postgresql://postgres:5432`

## Environment Variables

Create a `.env` file in the root directory to customize:

```env
# Database
POSTGRES_DB=GestionUserPI
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your_jwt_secret

# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id

# Ports
FRONTEND_PORT=4200
BACKEND_PORT=8081
EUREKA_PORT=8761
POSTGRES_PORT=5432
```

## Production Deployment

For production deployment:

1. Update environment variables in `.env`
2. Configure proper secrets management
3. Use external database instead of containerized PostgreSQL
4. Set up reverse proxy (nginx/traefik) for SSL
5. Configure proper logging and monitoring

## Resource Requirements

Minimum recommended resources:
- **RAM**: 4GB
- **CPU**: 2 cores
- **Disk**: 10GB

Individual service memory limits:
- Frontend: ~256MB
- User Management: ~512MB
- Eureka Server: ~256MB
- PostgreSQL: ~256MB

## Health Checks

All services include health checks:
- **PostgreSQL**: Checks every 10s
- **Eureka**: Checks every 30s (starts after 60s)
- **User Management**: Checks every 30s (starts after 120s)

## Volumes

Data persistence:
- `postgres_data`: PostgreSQL database files

To view volumes:
```powershell
docker volume ls
docker volume inspect englishforu_postgres_data
```
