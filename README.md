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
#  Appointment & Discussion Microservice - EnglishForU

Ce microservice est le cœur fonctionnel de la plateforme **EnglishForU**, une solution intelligente de gestion pour une école de langue anglaise. Il gère à la fois le cycle de vie des rendez-vous de test de niveau et la structure des groupes de discussion pédagogiques.

##  Fonctionnalités Clés

### 1. Gestion des Rendez-vous (CRUD & Plus)
*   **Réservation dynamique** : Choix du mode de passage (**On Site** ou **Remote**).
*   **Sécurité d'accès** : Génération automatique d'un **Access Code** unique à 6 chiffres envoyé par email après confirmation.
*   **Vérification sécurisée** : Accès au test uniquement via email + code via un endpoint dédié filtré par la Gateway.

### 2. Module de Test Intelligent (QCM & Proctoring)
*   **Évaluation Automatique** : Calcul du score final (ex: "3 / 4") et attribution du niveau (A1, B2, C1) sauvegardés en base de données.
*   **Système Anti-Triche (Advanced Proctoring)** : 
    *   Détection du changement d'onglet (Event Blur).
    *   Compteur de suspicion (`tabSwitchCount`) envoyé au backend.
    *   Affichage des alertes de triche sur l'interface Administrateur.

### 3. Module de Discussion
*   **Groupes de Discussion** : Création de classes virtuelles par l'Admin.
*   **Affectation Dynamique** : Liaison multi-étudiants et tuteur via IDs UUID.
*   **Filtrage Intelligent** : Requêtes SQL natives pour assurer que chaque utilisateur (Tuteur/Étudiant) ne voit que les groupes auxquels il appartient.

##  Tech Stack

*   **Framework** : Java Spring Boot 3.x
*   **Base de données** : PostgreSQL
*   **ORM** : Spring Data JPA / Hibernate
*   **Architecture** : Microservices (Eureka Discovery, Spring Cloud Gateway)
*   **Sécurité** : JWT Token parsing, Proxy Forwarding
*   **Outils** : Lombok, Maven

##  Installation & Configuration

1.  **Cloner le repository** :
    ```bash
    git clone https://github.com/votre-repo/appointment-service.git
    ```
2.  **Configuration Base de Données** :
    Créez une base de données PostgreSQL nommée `GestionAppointPI`.
3.  **Variables d'environnement** :
    Copiez le fichier `src/main/resources/application.properties.example` vers `application.properties` et remplissez vos accès :
    *   `DB_PASSWORD`
    *   `MAIL_PASSWORD` (Gmail App Password)
    *   `TWILIO_TOKEN` (Pour les SMS)
4.  **Lancement** :
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

##  API Endpoints Principaux

| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/appointments/book` | Réserver une session de test. |
| `POST` | `/api/appointments/verify-access` | Vérifier le code d'accès de l'étudiant. |
| `PUT` | `/api/appointments/{id}/complete` | Finaliser le test (Score + Niveau + Triche). |
| `GET` | `/api/discussions/groups/user/{id}` | Récupérer les groupes d'un utilisateur spécifique. |
| `POST` | `/api/discussions/groups` | Créer un nouveau groupe (Admin). |

##  Auteurs
*   Développement Backend : [MED KHALIL ESSOURI]
