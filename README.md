# Package Management Platform (Payment • PromoCode • PackageOffer • Items)

## Overview
This project was developed as part of the [PI/Academic Program Name] at **Esprit School of Engineering – Tunisia** (Academic Year [2025–2026]).
It is a full-stack web application for managing packages and offers, applying promo codes, handling items, and processing payments.

## Features
### Package Offers
- Create / update / delete package offers
- Define offer pricing, duration, and included items
- Activate / deactivate offers
- Search and filter offers

### Items
- Item CRUD management
- Assign items to package offers
- Stock/availability management (optional)

### Promo Codes
- Promo code CRUD
- Validity period (start/end date)
- Usage limits (global / per user) (optional)
- Apply promo code to a package offer checkout
- Discount types:
  - Percentage discount
  - Fixed amount discount

### Payment
- Checkout flow (offer selection → promo apply → total calculation → payment)
- Payment transaction creation + status tracking
- Payment history per user/admin
- Basic fraud/invalid checks (invalid amount, expired promo, etc.)

### Security & Roles (recommended)
- Authentication (JWT)
- Roles:
  - Admin: manage items/offers/promo/payments
  - User: browse offers, apply promo codes, pay, view history

## Tech Stack
### Frontend
- Angular (UI, routing, forms, services)
- Angular Material / Bootstrap (optional)

### Backend
- Spring Boot (REST APIs)
- Spring Security + JWT (recommended)
- Spring Data JPA / Hibernate
- MySQL / PostgreSQL (or H2 for dev)

## Architecture
- **Angular** consumes **Spring Boot REST APIs**
- Typical modules:
  - `Offer/PackageOffer` module
  - `Item` module
  - `PromoCode` module
  - `Payment` module
- Suggested backend layering:
  - `controller` → `service` → `repository` → `entity/dto`
- Suggested patterns:
  - DTOs for request/response
  - Global exception handling (`@ControllerAdvice`)
  - Validation (`@Valid`, Bean Validation)

## Contributors
- Era Pinbad (Branch: package-management)
- [Teammate Name]
- [Teammate Name]

## Academic Context
Developed at **Esprit School of Engineering – Tunisia**  
[PI Name] – [Class/Group] | Academic Year [2025–2026]

## Getting Started

### Prerequisites
- Java 17+ (or your project version)
- Maven or Gradle
- Node.js 18+ and npm
- MySQL/PostgreSQL (if not using H2)

---

### Backend (Spring Boot)

#### 1) Configure database
Edit `application.properties` (or `application.yml`):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/package_db
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
2) Run the backend
cd backend
mvn clean install
mvn spring-boot:run

Backend default URL:

http://localhost:8080

Frontend (Angular)
1) Install dependencies
cd frontend
npm install
2) Run the frontend
ng serve

Frontend default URL:

http://localhost:4200

API Quick Map (example)

Replace endpoints with your real ones.

Items

GET /api/items

POST /api/items

PUT /api/items/{id}

DELETE /api/items/{id}

Package Offers

GET /api/offers

POST /api/offers

PUT /api/offers/{id}

DELETE /api/offers/{id}

Promo Codes

GET /api/promocodes

POST /api/promocodes

POST /api/promocodes/apply (offerId + code → discount/total)

Payment

POST /api/payments/checkout

GET /api/payments/history

Acknowledgments

Esprit School of Engineering – Tunisia

GitHub Education Program (if applicable)

Supervisors / Tutors: mr mohamed rjab
