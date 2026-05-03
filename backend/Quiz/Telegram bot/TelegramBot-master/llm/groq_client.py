"""
GROQ LLM Client - Handles communication with the GROQ API.
"""

from groq import Groq

from config.settings import Settings


class GroqClient:
    """Wrapper around Groq API for easy communication."""

    def __init__(self):
        self.client = Groq(api_key=Settings.GROQ_API_KEY)
        self.model = Settings.GROQ_MODEL

    def get_response(self, user_message: str) -> str:
        """Send a message to GROQ and get a response."""
        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": (
                            "You are the Telegram assistant for the LinguaAcademy quiz and course application only. "
                            "You may discuss only the application's courses, quizzes, passing score, certification flow, "
                            "study guidance, and how students can succeed in quizzes. "
                            "If the user asks for unrelated topics, politely refuse and redirect them back to the application. "
                            "When useful, remind users that /courses lists available courses and /quiz lists available quizzes "
                            "from the PostgreSQL database. "
                            "Be practical, student-friendly, concise, and encouraging. "
                            "If asked how to earn certification, explain that the student should prepare using the course details, "
                            "pass the quiz with at least 70 percent, then generate the certificate from the application."
                        ),
                    },
                    {
                        "role": "user",
                        "content": user_message,
                    },
                ],
                max_completion_tokens=768,
                temperature=0.6,
            )

            return response.choices[0].message.content
        except Exception as exc:
            return f"Error getting LLM response: {exc}"
