"""
Configuration module - Loads environment variables safely.
"""

import os

from dotenv import load_dotenv

load_dotenv()


class Settings:
    """Stores all configuration from environment variables."""

    TELEGRAM_BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN")
    GROQ_API_KEY = os.getenv("GROQ_API_KEY")
    GROQ_MODEL = os.getenv("GROQ_MODEL", "llama-3.3-70b-versatile")

    PG_HOST = os.getenv("PG_HOST", "127.0.0.1")
    PG_PORT = int(os.getenv("PG_PORT", "5432"))
    PG_USER = os.getenv("PG_USER", "postgres")
    PG_PASSWORD = os.getenv("PG_PASSWORD", "")
    PG_DATABASE = os.getenv("PG_DATABASE", "GestionUserPI")

    @staticmethod
    def validate() -> None:
        """Check if all required credentials are set."""
        if not Settings.TELEGRAM_BOT_TOKEN:
            raise ValueError("TELEGRAM_BOT_TOKEN not found in .env")
        if not Settings.GROQ_API_KEY:
            raise ValueError("GROQ_API_KEY not found in .env")

        print("All credentials loaded successfully!")
