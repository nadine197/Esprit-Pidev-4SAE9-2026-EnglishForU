SELECT 'CREATE DATABASE "ClubEventPI"'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ClubEventPI')\gexec

SELECT 'CREATE DATABASE "GestionUserPI"'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'GestionUserPI')\gexec
