"""
Main entry point - starts the chatbot.
"""

from config.settings import Settings
from bot.telegram_bot import TelegramBot


def main():
    """Start the entire application."""
    print("Starting Telegram Chatbot with GROQ LLM...")
    print()

    Settings.validate()

    bot = TelegramBot()
    bot.start()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nApplication stopped by user.")
    except Exception as exc:
        print(f"\nApplication error: {exc}")
        import traceback

        traceback.print_exc()
