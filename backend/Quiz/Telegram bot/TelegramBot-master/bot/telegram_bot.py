"""
Telegram Bot - Handles Telegram commands and messages.
"""

from __future__ import annotations

import logging
import time
from typing import Any, Dict, List

import telebot
from requests.exceptions import ConnectionError, Timeout
from telebot import apihelper

from agent.agent import ChatAgent
from bot.quiz_database import QuizDatabaseService
from config.settings import Settings

apihelper.SESSION_TIMEOUT = 10

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class TelegramBot:
    """Telegram bot that uses our agent and PostgreSQL course/quiz data."""

    def __init__(self):
        self.agent = ChatAgent()
        self.quiz_db = QuizDatabaseService()
        self.bot = telebot.TeleBot(Settings.TELEGRAM_BOT_TOKEN)

        @self.bot.message_handler(commands=["start", "help"])
        def handle_start(message):
            welcome_message = (
                "Welcome to the LinguaAcademy Quiz and Certification Assistant.\n\n"
                "I can help you with:\n"
                "- Available courses from the platform database\n"
                "- Available quizzes from the platform database\n"
                "- How to succeed in quizzes\n"
                "- How certification works in the application\n\n"
                "Commands:\n"
                "/courses - show available courses\n"
                "/quiz - show available quizzes\n\n"
                "You can also ask things like:\n"
                "- How do I prepare for quizzes?\n"
                "- How do I get my certificate?\n"
                "- What should I study first?"
            )
            self.bot.reply_to(message, welcome_message)

        @self.bot.message_handler(commands=["courses", "course"])
        def handle_courses_command(message):
            self._reply_with_courses(message)

        @self.bot.message_handler(commands=["quiz", "quizzes"])
        def handle_quizzes_command(message):
            self._reply_with_quizzes(message)

        @self.bot.message_handler(func=lambda message: True)
        def handle_message(message):
            try:
                text = (message.text or "").strip()
                self.bot.send_chat_action(message.chat.id, "typing")

                if self._is_courses_question(text):
                    self._reply_with_courses(message)
                    return

                if self._is_quizzes_question(text):
                    self._reply_with_quizzes(message)
                    return

                response = self.agent.process_message(text)
                self._send_long_message(message.chat.id, response)
            except Exception:
                logger.exception("Error processing message")
                self.bot.reply_to(
                    message,
                    "Sorry, I encountered an error while processing your message. Please try again.",
                )

    @staticmethod
    def _normalize_text(text: str) -> str:
        return " ".join((text or "").lower().split())

    def _is_courses_question(self, text: str) -> bool:
        normalized = self._normalize_text(text)
        phrases = (
            "show courses",
            "list courses",
            "available courses",
            "what courses",
            "course list",
            "all courses",
        )
        return normalized in {"courses", "course"} or any(
            phrase in normalized for phrase in phrases
        )

    def _is_quizzes_question(self, text: str) -> bool:
        normalized = self._normalize_text(text)
        phrases = (
            "show quizzes",
            "list quizzes",
            "available quizzes",
            "what quizzes",
            "quiz list",
            "all quizzes",
            "show quiz",
        )
        return normalized in {"quiz", "quizzes"} or any(
            phrase in normalized for phrase in phrases
        )

    def _reply_with_courses(self, message) -> None:
        try:
            courses = self.quiz_db.fetch_courses()
            response = self._format_courses_response(courses)
        except Exception as exc:
            logger.exception("Failed to load courses")
            response = (
                "Could not load courses from PostgreSQL.\n"
                f"Error: {exc}\n"
                "Check PG_HOST, PG_PORT, PG_USER, PG_PASSWORD, and PG_DATABASE in .env."
            )
        self._send_long_message(message.chat.id, response)

    def _reply_with_quizzes(self, message) -> None:
        try:
            quizzes = self.quiz_db.fetch_quizzes()
            response = self._format_quizzes_response(quizzes)
        except Exception as exc:
            logger.exception("Failed to load quizzes")
            response = (
                "Could not load quizzes from PostgreSQL.\n"
                f"Error: {exc}\n"
                "Check PG_HOST, PG_PORT, PG_USER, PG_PASSWORD, and PG_DATABASE in .env."
            )
        self._send_long_message(message.chat.id, response)

    def _send_long_message(self, chat_id: int, text: str, max_length: int = 3500) -> None:
        if len(text) <= max_length:
            self.bot.send_message(chat_id, text)
            return

        chunks: List[str] = []
        current: List[str] = []
        current_len = 0

        for block in text.split("\n\n"):
            block_len = len(block) + 2
            if current and current_len + block_len > max_length:
                chunks.append("\n\n".join(current))
                current = [block]
                current_len = block_len
            else:
                current.append(block)
                current_len += block_len

        if current:
            chunks.append("\n\n".join(current))

        for chunk in chunks:
            self.bot.send_message(chat_id, chunk)

    @staticmethod
    def _pick(row: Dict[str, Any], *keys: str, default: Any = "N/A") -> Any:
        for key in keys:
            if key in row and row[key] is not None and row[key] != "":
                return row[key]
        return default

    @staticmethod
    def _shorten(text: Any, limit: int = 120) -> str:
        if text is None:
            return "N/A"
        value = str(text).strip()
        if len(value) <= limit:
            return value
        return f"{value[:limit - 3]}..."

    @staticmethod
    def _format_duration(value: Any) -> str:
        if value in (None, "", "N/A"):
            return "N/A"
        return f"{value}h"

    def _format_courses_response(self, courses: List[Dict[str, Any]]) -> str:
        if not courses:
            return "No courses were found in the PostgreSQL course table."

        max_items = 20
        lines = [
            f"Available courses: {len(courses)}",
            "Use these course titles as your study plan before taking quizzes.",
        ]

        for idx, course in enumerate(courses[:max_items], start=1):
            title = self._pick(course, "title")
            course_id = self._pick(course, "courseid")
            duration = self._format_duration(self._pick(course, "duration", default=None))
            quiz_count = self._pick(course, "quiz_count", default=0)
            description = self._shorten(self._pick(course, "description", default="No description"))

            lines.append(
                f"{idx}. {title}\n"
                f"Course ID: {course_id} | Duration: {duration} | Quizzes: {quiz_count}\n"
                f"Description: {description}"
            )

        if len(courses) > max_items:
            lines.append(f"... and {len(courses) - max_items} more courses.")

        lines.append(
            "Tip: Review the course description first, then practice the related quiz until you can score at least 70%."
        )
        return "\n\n".join(lines)

    def _format_quizzes_response(self, quizzes: List[Dict[str, Any]]) -> str:
        if not quizzes:
            return "No quizzes were found in the PostgreSQL quiz table."

        max_items = 25
        lines = [
            f"Available quizzes: {len(quizzes)}",
            "Focus on understanding the linked course before starting the quiz.",
        ]

        for idx, quiz in enumerate(quizzes[:max_items], start=1):
            title = self._pick(quiz, "title")
            quiz_id = self._pick(quiz, "id")
            course_title = self._pick(quiz, "course_title", default="No linked course")
            passing_score = self._pick(quiz, "passing_score", default=70)
            question_count = self._pick(quiz, "question_count", default=0)

            lines.append(
                f"{idx}. {title}\n"
                f"Quiz ID: {quiz_id} | Course: {course_title}\n"
                f"Passing score: {passing_score}% | Questions: {question_count}"
            )

        if len(quizzes) > max_items:
            lines.append(f"... and {len(quizzes) - max_items} more quizzes.")

        lines.append(
            "Certification tip: Study the related course, aim for 70% or more in the quiz, then generate your certificate inside the application."
        )
        return "\n\n".join(lines)

    def test_connection(self) -> bool:
        print("\nTesting connection to Telegram API...")
        max_retries = 3
        retry_delay = 2

        for attempt in range(1, max_retries + 1):
            try:
                bot_info = self.bot.get_me()
                print(f"Connection successful! Bot: @{bot_info.username}")
                print(f"Bot ID: {bot_info.id}")
                return True
            except (ConnectionError, Timeout) as exc:
                print(
                    f"Connection attempt {attempt}/{max_retries} failed: {type(exc).__name__}"
                )
                if attempt < max_retries:
                    print(f"Retrying in {retry_delay} seconds...")
                    time.sleep(retry_delay)
                    retry_delay *= 2
                else:
                    print("Unable to connect to Telegram API after retries.")
                    print("Check internet access and bot token.")
                    return False
            except Exception as exc:
                print(f"Unexpected error: {exc}")
                return False

        return False

    def start(self) -> None:
        print("Bot is starting...")

        if not self.test_connection():
            print("Cannot start bot without Telegram API connection.")
            return

        print("Press Ctrl+C to stop the bot")
        print("Waiting for incoming messages...\n")

        try:
            self.bot.infinity_polling()
        except KeyboardInterrupt:
            print("\nBot stopped by user.")
        except Exception as exc:
            print(f"\nError: {exc}")
            print(f"Error type: {type(exc).__name__}")
