📅 Documentation : Module de Gestion des Rendez-vous (Appointment)
1. Vue d'ensemble
   Le module Appointment est responsable de la gestion du cycle de vie des tests de niveau pour les nouveaux arrivants de l'école EnglishForU. Il assure la transition entre un simple visiteur et un étudiant inscrit.
   Port d'écoute : 8087
   Préfixe API : /api/appointments
   Base de données : Table appointment dans PostgreSQL.
2. Flux Métier (Business Workflow)
   Le processus suit une logique stricte pour garantir la sécurité et l'intégrité de l'évaluation :
   Réservation (Booking) : Le visiteur choisit un créneau et son mode de passage (ON_SITE ou REMOTE). Le système génère alors un Access Code unique à 6 chiffres.
   Confirmation : L'administrateur valide le rendez-vous. Un email automatique est envoyé au visiteur contenant son code d'accès.
   Accès au Test : Le visiteur saisit son email et son code sur la plateforme. La Gateway interroge l'endpoint de vérification.
   Examen & Surveillance : Pendant le test, le système de Proctoring compte les sorties d'onglet.
   Finalisation : À la fin du QCM, le score, le niveau calculé et le compteur de triche sont sauvegardés. Le statut passe à COMPLETED.
3. Spécifications des API (Endpoints)
   A. Réservation
   URL : POST /api/appointments/book
   Description : Crée une demande de rendez-vous initiale.
   Logique complexe : Génération aléatoire d'un code via String.format("%06d", new Random().nextInt(1000000)).
   B. Vérification de l'Accès
   URL : POST /api/appointments/verify-access
   Corps (JSON) : { "email": "...", "code": "..." }
   Description : Sécurise l'entrée du test. Si le couple email/code est invalide, renvoie une erreur 401 Unauthorized.
   C. Finalisation du Test
   URL : PUT /api/appointments/{id}/complete
   Paramètres : result (Niveau), score (Note), cheatCount (Triche).
   Description : Met à jour le dossier de l'étudiant et envoie l'email de félicitations contenant ses résultats.
4. Détails du Modèle de Données (Entity)
   Attribut	Type	Description
   id	UUID	Identifiant unique généré automatiquement.
   accessCode	String	Code secret à 6 chiffres pour accéder au test.
   locationType	Enum	Définit si le test est à l'école ou à distance.
   status	Enum	PENDING, CONFIRMED, CANCELLED, COMPLETED.
   tabSwitchCount	Integer	Compteur de suspicion de triche (perte de focus).
   levelResult	String	Niveau final attribué par l'algorithme (ex: B2).
   qcmScore	String	Note brute obtenue au QCM (ex: 3 / 4).
5. Logiques Avancées & Sécurité
   🛡️ Algorithme de Proctoring
   Le système intègre une détection de triche passive. Le Frontend surveille l'événement JavaScript window.blur. Chaque détection incrémente une variable qui est transmise au microservice via l'endpoint /complete.
   Impact Admin : Une alerte visuelle s'affiche si tabSwitchCount > 0.
   📧 Notifications Automatisées
   Le service est lié à NotificationService qui utilise JavaMailSender.
   Email 1 : Envoi du code d'accès lors de la confirmation par l'Admin.
   Email 2 : Envoi du rapport de score après la fin du test.
6. Tests Unitaires
   La fiabilité de la logique de réservation est testée via JUnit 5 et Mockito.
   Test de génération de code : Vérifie que le code généré fait toujours 6 chiffres.
   Test de validation de date : Empêche la création d'un créneau si l'heure de fin est passée.