# EnglishForU – Microservices Platform

## 📌 About the Project
**EnglishForU** is an **academic project** built at **ESPRIT School of Engineering** designed to help students **master microservices architecture** through a **30-hour hands-on training program**. The platform manages an English-language school: appointments, courses, packages, discussions, clubs/events, and more.

## 📌 Project Architecture
![](https://github.com/badi3a/JobBoard_Microservice/blob/main/documentation/diag/microservices-global-architecture.drawio%20(2).png)

## 🎯 Main Goal
The primary objective of this project is to provide students with:
✅ A **practical understanding** of microservices concepts.
✅ Experience in **service decomposition, inter-service communication, and scalability**.
✅ Hands-on training with **modern tools and frameworks** used in microservices development.

## 🛠️ Technologies & Concepts
- **Spring Boot** (microservices backend)
- **Angular** (frontend)
- **API Gateway & Service Discovery (Eureka)**
- **Message Brokers (Kafka, RabbitMQ, etc.)**
- **Containerization (Docker, Kubernetes)**
- **Database Management (PostgreSQL / MySQL)**
- **Security with JWT (Authentication & Authorization)**
- **WebSockets (STOMP / SockJS) for real-time chat**

## 📅 Training Duration
**30 hours** of guided learning and project development.

---

## 🚀 Microservices

| Service | Port | Description |
|---|---|---|
| Eureka Server | 8761 | Service registry |
| API Gateway | 8090 | Central entry point |
| User Service | 8081 | Authentication & user management |
| ClubEvent Service | 8083 | Club and event management |
| Appointment Service | 8087 | Appointment booking & level-test |
| Discussion Service | 8088 | Study groups & real-time chat |
| Course Service | 8084 | Course & content management |
| Package Service | 8085 | Package offers, promo codes & payments |
| Quiz Service | 8056 | Quizzes & evaluations |
| Frontend | 80 | Main Angular application |

---

## 📦 Package Management Module (Payment • PromoCode • PackageOffer • Items)

### Features
- **Package Offers**: Create / update / delete, pricing, duration, activation
- **Items**: Item CRUD, assign to package offers
- **Promo Codes**: CRUD, validity periods, usage limits, percentage & fixed discounts
- **Payment**: Checkout flow, transaction tracking, payment history

---

## 📅 Appointment & Discussion Module

### Appointment Features
- **Dynamic booking**: On Site or Remote mode
- **Access Code**: Auto-generated 6-digit code sent by email after confirmation
- **Level Test**: QCM evaluation with automatic scoring (A1→C2) + anti-cheat proctoring (tab-switch detection)

### Discussion Features
- **Study Groups**: Admin creates virtual classes
- **Real-time Chat**: WebSocket (STOMP/SockJS) with message edit/delete/pin/typing indicators
- **Smart Filtering**: Each user (tutor/student) sees only their own groups

---

## 🛠️ Getting Started

### Prerequisites
- Java 17+, Maven
- Node.js 18+, npm
- Docker & Docker Compose
- PostgreSQL (or use Docker)

### Run with Docker Compose
```bash
git clone https://github.com/nadine197/Esprit-Pidev-4SAE9-2026-EnglishForU.git
cd Esprit-Pidev-4SAE9-2026-EnglishForU
docker compose up --build
```

### Run Frontend locally
```bash
cd frontend
npm install
ng serve
```

### Run Backend service locally
```bash
cd backEnd/microservices/<ServiceName>
mvn clean spring-boot:run
```

---

## 🎓 Acknowledgment
This project is part of the academic training provided by **ESPRIT School of Engineering**, aiming to equip students with industry-relevant skills in modern software development.

## 👨‍🏫 Supervisor
Connect on LinkedIn: [Badia Bouhdid](https://www.linkedin.com/in/badiabouhdid/)

## 👨‍💻 Contributors
- **Gestion Event/Club**: KHLIL2002 (Mohamed Khalil Essouri)
- **Appointment & Discussion**: MED KHALIL ESSOURI
- **Package & Payment**: Era Pinbad
- **Nadine Razki** and other team members
