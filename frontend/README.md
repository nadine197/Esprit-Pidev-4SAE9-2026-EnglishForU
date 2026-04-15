# EnglishForU – English Learning Platform

## 📌 Overview

EnglishForU is a full-stack web application developed as part of the **PIDEV – 4th Year Engineering Program** at **Esprit School of Engineering (2025–2026)**.

The platform helps students improve their English skills through:
- Interactive courses
- Quizzes
- Study groups
- Progress tracking

It also provides dashboards for teachers and administrators.

---

## 🏗️ Architecture

The system is based on a **Microservices Architecture**:

- Authentication Service
- User Service
- Course Service
- Quiz Service
- Discussion Service
- Gateway (API Gateway)
- Eureka Server

Each service has its own database and communicates via REST APIs.

The **API Gateway** manages routing and security using JWT authentication.

---

## 💻 Tech Stack

### Frontend
- Angular 16+
- TypeScript
- Bootstrap / Angular Material

### Backend
- Spring Boot (Microservices)
- Spring Cloud
- JWT Security
- REST APIs

### Database
- PostgreSQL

---

## ⚙️ Angular Development

### Development server
```bash
ng serve