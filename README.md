# 📅 Appointment & Discussion Microservice - EnglishForU

Ce microservice est le cœur fonctionnel de la plateforme **EnglishForU**, une solution intelligente de gestion pour une école de langue anglaise. Il gère à la fois le cycle de vie des rendez-vous de test de niveau et la structure des groupes de discussion pédagogiques.

## 🚀 Fonctionnalités Clés

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

## 🛠 Tech Stack

*   **Framework** : Java Spring Boot 3.x
*   **Base de données** : PostgreSQL
*   **ORM** : Spring Data JPA / Hibernate
*   **Architecture** : Microservices (Eureka Discovery, Spring Cloud Gateway)
*   **Sécurité** : JWT Token parsing, Proxy Forwarding
*   **Outils** : Lombok, Maven

## 🌍 Objectifs de Développement Durable (ODD)

Ce projet intègre une dimension éthique et responsable :
*   **ODD 4 - Éducation de Qualité** : Garantie de l'intégrité des examens via le Proctoring et suivi personnalisé des niveaux pour ne laisser aucun étudiant de côté.
*   **ODD 12 - Consommation Responsable (Green AI)** : Optimisation des requêtes et utilisation de l'**ACP (Analyse en Composantes Principales)** pour réduire la charge de calcul des serveurs et la consommation énergétique.

## ⚙️ Installation & Configuration

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

## 🔌 API Endpoints Principaux

| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/appointments/book` | Réserver une session de test. |
| `POST` | `/api/appointments/verify-access` | Vérifier le code d'accès de l'étudiant. |
| `PUT` | `/api/appointments/{id}/complete` | Finaliser le test (Score + Niveau + Triche). |
| `GET` | `/api/discussions/groups/user/{id}` | Récupérer les groupes d'un utilisateur spécifique. |
| `POST` | `/api/discussions/groups` | Créer un nouveau groupe (Admin). |

## 👥 Auteurs
*   **Équipe ML Appliqué** - Architecture & Modélisation
*   Développement Backend : [Ton Nom/Pseudo]
