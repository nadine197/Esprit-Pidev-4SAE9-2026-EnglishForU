package tn.spring.quiz.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.spring.quiz.DTO.QuizRecommendationAnswerDTO;
import tn.spring.quiz.DTO.QuizRecommendationResponse;
import tn.spring.quiz.Models.Course;
import tn.spring.quiz.Models.Question;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Repositories.QuizRepository;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GeminiQuizRecommendationService {

    private final QuizRepository quizRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    public GeminiQuizRecommendationService(QuizRepository quizRepository, ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public QuizRecommendationResponse recommendQuestion(Long quizId) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key is missing. Add gemini.api.key in the Quiz backend configuration first.");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Course course = quiz.getCourse();
        if (course == null) {
            throw new RuntimeException("This quiz is not linked to a course yet, so Gemini has no course context to use.");
        }

        try {
            String requestJson = objectMapper.writeValueAsString(buildGeminiRequest(quiz, course));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(buildGenerateContentUri())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(45))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException(extractErrorMessage(response.body()));
            }

            QuizRecommendationResponse recommendation = objectMapper.readValue(
                    extractRecommendationJson(response.body()),
                    QuizRecommendationResponse.class
            );

            return normalizeRecommendation(recommendation, course);
        } catch (IOException exception) {
            throw new RuntimeException("Gemini returned a response, but it could not be parsed into a quiz recommendation.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Gemini recommendation request was interrupted.", exception);
        }
    }

    private URI buildGenerateContentUri() {
        String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        return URI.create(baseUrl + "/models/" + model + ":generateContent?key=" + encodedKey);
    }

    private Map<String, Object> buildGeminiRequest(Quiz quiz, Course course) {
        Map<String, Object> request = new HashMap<>();
        request.put("systemInstruction", Map.of(
                "parts", List.of(Map.of(
                        "text",
                        "You are an expert instructional designer. Create one concise multiple-choice quiz question aligned to the course details. Return exactly four answers with exactly one correct answer."
                ))
        ));

        request.put("contents", List.of(Map.of(
                "parts", List.of(Map.of("text", buildPrompt(quiz, course)))
        )));

        request.put("generationConfig", Map.of(
                "temperature", 0.8,
                "response_mime_type", "application/json",
                "response_schema", Map.of(
                        "type", "OBJECT",
                        "properties", Map.of(
                                "questionText", Map.of("type", "STRING"),
                                "recommendationReason", Map.of("type", "STRING"),
                                "answers", Map.of(
                                        "type", "ARRAY",
                                        "items", Map.of(
                                                "type", "OBJECT",
                                                "properties", Map.of(
                                                        "text", Map.of("type", "STRING"),
                                                        "correct", Map.of("type", "BOOLEAN")
                                                ),
                                                "required", List.of("text", "correct")
                                        )
                                )
                        ),
                        "required", List.of("questionText", "recommendationReason", "answers")
                )
        ));

        return request;
    }

    private String buildPrompt(Quiz quiz, Course course) {
        String existingQuestions = quiz.getQuestions() == null || quiz.getQuestions().isEmpty()
                ? "No existing questions yet."
                : quiz.getQuestions().stream()
                .map(Question::getText)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .limit(8)
                .collect(Collectors.joining("\n- ", "- ", ""));

        String courseDescription = course.getDescription() != null && !course.getDescription().isBlank()
                ? course.getDescription().trim()
                : "No course description was provided.";

        String durationLabel = course.getDuration() != null
                ? course.getDuration() + " hours"
                : "Not specified";

        return """
                Create one new question recommendation for this quiz.

                Course title: %s
                Course description: %s
                Course duration: %s
                Quiz title: %s

                Existing quiz questions:
                %s

                Requirements:
                - The question must fit the course details closely.
                - Keep the wording clear and professional.
                - Return exactly 4 answers.
                - Exactly 1 answer must be marked correct.
                - Avoid duplicating the existing questions.
                - The recommendationReason should briefly explain why the question fits the course.
                """.formatted(
                safeText(course.getTitle(), "Untitled course"),
                courseDescription,
                durationLabel,
                safeText(quiz.getTitle(), "Untitled quiz"),
                existingQuestions
        );
    }

    private String extractRecommendationJson(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new RuntimeException("Gemini did not return a usable recommendation.");
        }

        return textNode.asText();
    }

    private QuizRecommendationResponse normalizeRecommendation(QuizRecommendationResponse recommendation, Course course) {
        if (recommendation == null) {
            throw new RuntimeException("Gemini returned an empty recommendation.");
        }

        String normalizedQuestion = recommendation.getQuestionText() != null
                ? recommendation.getQuestionText().trim()
                : "";

        if (normalizedQuestion.isBlank()) {
            throw new RuntimeException("Gemini returned a recommendation without a question.");
        }

        List<QuizRecommendationAnswerDTO> normalizedAnswers = recommendation.getAnswers() == null
                ? List.of()
                : recommendation.getAnswers().stream()
                .filter(Objects::nonNull)
                .map(this::normalizeAnswer)
                .filter(answer -> !answer.getText().isBlank())
                .limit(4)
                .collect(Collectors.toList());

        if (normalizedAnswers.size() < 2) {
            throw new RuntimeException("Gemini returned too few answers to build a useful quiz question.");
        }

        long correctCount = normalizedAnswers.stream().filter(QuizRecommendationAnswerDTO::isCorrect).count();
        if (correctCount == 0) {
            normalizedAnswers.get(0).setCorrect(true);
        } else if (correctCount > 1) {
            boolean firstCorrectSeen = false;
            for (QuizRecommendationAnswerDTO answer : normalizedAnswers) {
                if (answer.isCorrect() && !firstCorrectSeen) {
                    firstCorrectSeen = true;
                    continue;
                }
                if (answer.isCorrect()) {
                    answer.setCorrect(false);
                }
            }
        }

        recommendation.setQuestionText(normalizedQuestion);
        recommendation.setCourseTitle(safeText(course.getTitle(), "Untitled course"));
        recommendation.setRecommendationReason(safeText(recommendation.getRecommendationReason(), "This question matches the course scope and quiz objective."));
        recommendation.setAnswers(normalizedAnswers);
        return recommendation;
    }

    private QuizRecommendationAnswerDTO normalizeAnswer(QuizRecommendationAnswerDTO answer) {
        QuizRecommendationAnswerDTO normalized = new QuizRecommendationAnswerDTO();
        normalized.setText(answer.getText() != null ? answer.getText().trim() : "");
        normalized.setCorrect(answer.isCorrect());
        return normalized;
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = root.path("error").path("message").asText();
            if (!message.isBlank()) {
                return "Gemini request failed: " + message;
            }
        } catch (IOException ignored) {
        }

        return "Gemini request failed. Please verify the API key, model configuration, and network access.";
    }

    private String safeText(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }
}
