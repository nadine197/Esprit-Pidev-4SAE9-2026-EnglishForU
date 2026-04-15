
"""
PostgreSQL access for Telegram course and quiz commands.
"""

from __future__ import annotations

from typing import Any, Dict, List

import pg8000.dbapi as pg8000

from config.settings import Settings

class QuizDatabaseService:
    """Reads course and quiz data from PostgreSQL."""

    def __init__(self) -> None:
        self._config = {
            "host": Settings.PG_HOST,
            "port": Settings.PG_PORT,
            "user": Settings.PG_USER,
            "password": Settings.PG_PASSWORD,
            "dbname": Settings.PG_DATABASE,
        }

    def fetch_courses(self) -> List[Dict[str, Any]]:
        query = """
            SELECT
                c.courseid,
                c.title,
                c.description,
                c.duration,
                COUNT(q.id) AS quiz_count
            FROM course c
            LEFT JOIN quiz q ON q.course_courseid = c.courseid
            GROUP BY c.courseid, c.title, c.description, c.duration
            ORDER BY c.courseid ASC
        """
        return self._fetch_all(query)

    def fetch_quizzes(self) -> List[Dict[str, Any]]:
        query = """
            SELECT
                q.id,
                q.title,
                q.passing_score,
                q.course_courseid,
                c.title AS course_title,
                COUNT(ques.id) AS question_count
            FROM quiz q
            LEFT JOIN course c ON c.courseid = q.course_courseid
            LEFT JOIN question ques ON ques.quiz_id = q.id
            GROUP BY q.id, q.title, q.passing_score, q.course_courseid, c.title
            ORDER BY q.id ASC
        """
        return self._fetch_all(query)

    def _fetch_all(self, query: str) -> List[Dict[str, Any]]:
        connection = None
        cursor = None
        try:
            connection = pg8000.connect(
                host=self._config["host"],
                port=self._config["port"],
                user=self._config["user"],
                password=self._config["password"],
                database=self._config["dbname"],
            )
            cursor = connection.cursor()
            cursor.execute(query)
            columns = [column[0] for column in cursor.description or []]
            rows = cursor.fetchall()
            return [dict(zip(columns, row)) for row in rows]
        except Exception as exc:
            raise RuntimeError(f"PostgreSQL error: {exc}") from exc
        finally:
            if cursor is not None:
                cursor.close()
            if connection is not None:
                connection.close()
