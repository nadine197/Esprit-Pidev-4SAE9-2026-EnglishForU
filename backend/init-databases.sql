-- ============================================================
-- Script d'initialisation des bases de données
-- Une base dédiée par microservice (principe microservices)
-- À exécuter en tant que superuser PostgreSQL (postgres)
-- Usage : psql -U postgres -f init-databases.sql
-- ============================================================

-- 1. User microservice
CREATE DATABASE "UserPI"
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE   = 'en_US.UTF-8'
    TEMPLATE   = template0;

-- 2. Course microservice
CREATE DATABASE "CoursePI"
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE   = 'en_US.UTF-8'
    TEMPLATE   = template0;

-- 3. ClubEvent microservice
CREATE DATABASE "ClubEventPI"
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE   = 'en_US.UTF-8'
    TEMPLATE   = template0;

-- 4. Package microservice
CREATE DATABASE "GestionPackagePI"
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE   = 'en_US.UTF-8'
    TEMPLATE   = template0;

-- 5. Quiz microservice
CREATE DATABASE "QuizPI"
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE   = 'en_US.UTF-8'
    TEMPLATE   = template0;

-- 6. Appointment microservice
CREATE DATABASE "AppointmentPI"
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE   = 'en_US.UTF-8'
    TEMPLATE   = template0;

-- 7. Discussion microservice
CREATE DATABASE "DiscussionPI"
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE   = 'en_US.UTF-8'
    TEMPLATE   = template0;

-- ============================================================
-- Vérification : liste toutes les bases créées
-- ============================================================
\l
