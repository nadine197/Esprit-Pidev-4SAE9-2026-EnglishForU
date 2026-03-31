# Project Setup Instructions

## Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Java JDK | 17+ | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org |
| Node.js | 18+ | https://nodejs.org |
| Docker Desktop | latest | https://www.docker.com/products/docker-desktop |

---

## API Keys to Fill In Before Running

### 1. Stripe
Edit `backEnd/microservices/ClubEvent/.env`:
```
STRIPE_SECRET_KEY=sk_test_...        ← your Stripe secret key
```
Edit `frontEnd/JobBoard/src/environments/environment.ts`:
```
stripePublishableKey: 'pk_test_...'  ← your Stripe publishable key
```
Get both at: https://dashboard.stripe.com → Developers → API Keys

### 2. Gmail (for event pass emails)
Edit `backEnd/microservices/ClubEvent/.env`:
```
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_16_char_app_password   ← NOT your login password
```
Generate an App Password:
1. Enable 2FA on Google: https://myaccount.google.com/security
2. Create App Password: https://myaccount.google.com/apppasswords

---

## Option A — Run with Docker (recommended)

```bash
# 1. From project root:
docker compose up --build
```

Services will start at:
- Frontend  → http://localhost:4200
- Gateway   → http://localhost:8090
- Eureka    → http://localhost:8761
- User MS   → http://localhost:8081
- ClubEvent → http://localhost:8083
- PostgreSQL→ localhost:5432

To stop everything:
```bash
docker compose down
```
To stop and wipe the database:
```bash
docker compose down -v
```

---

## Option B — Run Locally (without Docker)

### 1. Start PostgreSQL
Make sure PostgreSQL is running on port 5432 with:
- user: `postgres`
- password: `inception722`

Create the two databases:
```sql
CREATE DATABASE "GestionUserPI";
CREATE DATABASE "ClubEventPI";
```

### 2. Start Backend Services (in order)
```bash
# Eureka (service registry) — port 8761
cd backEnd/eureka
./mvnw spring-boot:run

# Gateway — port 8090
cd backEnd/Gateway
./mvnw spring-boot:run

# User microservice — port 8081
cd backEnd/microservices/User
./mvnw spring-boot:run

# ClubEvent microservice — port 8083
cd backEnd/microservices/ClubEvent
./mvnw spring-boot:run
```

### 3. Start Frontend
```bash
cd frontEnd/JobBoard
npm install          # installs node_modules (first time only)
npm start            # starts Angular dev server on http://localhost:4200
```

---

## Database Notes
- Schema is auto-managed by Hibernate (`ddl-auto=update`)
- Tables and new columns are created automatically on startup
- Existing data is never dropped

## Project Structure
```
backEnd/
  eureka/          ← Service registry (port 8761)
  Gateway/         ← API Gateway (port 8090)
  microservices/
    User/          ← Auth, registration (port 8081)
    ClubEvent/     ← Clubs, events, payments (port 8083)

frontEnd/
  JobBoard/        ← Angular 16 app (port 4200)

docker-compose.yml ← Runs everything together
init-db.sql        ← Creates databases on first Docker run
```
