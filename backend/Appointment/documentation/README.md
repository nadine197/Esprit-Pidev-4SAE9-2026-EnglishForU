# 📅 Appointment & Proctoring Service
> **Core Module for EnglishForU Intelligent Platform**

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=for-the-badge&logo=spring-boot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=for-the-badge&logo=docker)

---

## 🌟 Overview
Le module **Appointment** est le moteur de conversion de notre école. Il transforme les prospects en étudiants évalués grâce à un workflow automatisé unique intégrant une surveillance par IA.

| Information | Détails |
| :--- | :--- |
| **Microservice** | Appointment-Service |
| **Port par défaut** | `8087` |
| **Base de données** | `GestionAppointPI` (PostgreSQL) |
| **Protocole Temps Réel** | WebSocket STOMP |

---

## 🛠 Workflow Logic (Cycle de vie)

1.  **RESERVATION** ➔ Le visiteur réserve. Le système génère un `accessCode` (6 chiffres).
2.  **VALIDATION** ➔ L'Admin confirme. Le `NotificationService` envoie le code par email.
3.  **ACCÈS** ➔ Authentification sécurisée via `/verify-access` (Email + Code).
4.  **EXAMEN** ➔ Passage du QCM avec **Proctoring Actif** (détection de triche).
5.  **SCORING** ➔ Calcul du niveau (A1-C1) et archivage du dossier complet.

---

## 🚀 API Reference

### 📧 Gestion des rendez-vous
`POST /api/appointments/book`
> Crée une session. Génère le code de sécurité.

`POST /api/appointments/verify-access`
> Vérifie l'identité avant de débloquer le test.

### 🧠 Évaluation & Proctoring
`PUT /api/appointments/{id}/complete`
> **Paramètres :** `result` (Niveau), `score` (Note), `cheatCount` (Triche).  
> *Met à jour la base de données et déclenche l'email final.*

---

## 🛡️ Advanced Feature: Smart Proctoring (ODD 4)
Pour garantir une **Éducation de Qualité (ODD 4)**, nous avons développé un système de surveillance asynchrone :

*   **Technologie :** Capture de l'événement `window:blur` côté client.
*   **Logique :** Chaque sortie d'onglet est comptabilisée et persistée en base de données.
*   **Visualisation Admin :** Les sessions avec `tabSwitchCount > 0` sont marquées par un badge d'alerte rouge pour audit manuel.

---

## 📊 Data Structure (Entity: Appointment)

| Propriété | Type | Description |
| :--- | :--- | :--- |
| `id` | `UUID` | Identifiant universel unique. |
| `accessCode` | `String` | Jeton de sécurité à usage unique. |
| `status` | `Enum` | PENDING, CONFIRMED, CANCELLED, COMPLETED. |
| `qcmScore` | `String` | Résultat brut (ex: "18/20"). |
| `tabSwitchCount`| `Integer` | Score de suspicion de triche. |

---

## 🧪 Quality Assurance (JUnit 5 & Mockito)
Nous maintenons une couverture de test stricte sur la logique métier complexe :

- ✅ **Génération de code** : Vérification de l'unicité et du format.
- ✅ **Validation temporelle** : Empêchement des chevauchements de créneaux.
- ✅ **Intégrité Proctoring** : Validation du tunnel de données de surveillance.

```bash
# Lancer la suite de tests
mvn clean test