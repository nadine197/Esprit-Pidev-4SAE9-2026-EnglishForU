package tn.spring.quiz.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.spring.quiz.DTO.EvaluationMotivationSuggestionRequest;
import tn.spring.quiz.DTO.EvaluationMotivationSuggestionResponse;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Models.QuizAttempt;
import tn.spring.quiz.Models.StudentEvaluation;
import tn.spring.quiz.Repositories.QuizAttemptRepository;
import tn.spring.quiz.Repositories.StudentEvaluationRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GeminiEvaluationSuggestionService {

    public static class GeminiUpstreamException extends RuntimeException {
        public GeminiUpstreamException(String message) {
            super(message);
        }
    }

    private final StudentEvaluationRepository evaluationRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.api.fallback-models:gemini-2.0-flash}")
    private String fallbackModels;

    @Value("${gemini.api.retry-count:2}")
    private int retryCount;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    public GeminiEvaluationSuggestionService(StudentEvaluationRepository evaluationRepository,
                                             QuizAttemptRepository quizAttemptRepository,
                                             ObjectMapper objectMapper) {
        this.evaluationRepository = evaluationRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public EvaluationMotivationSuggestionResponse suggestMotivation(EvaluationMotivationSuggestionRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Gemini API key is missing. Set gemini.api.key or the GEMINI_API_KEY environment variable in the Quiz backend configuration.");
        }

        UUID studentId = request.getStudentId();
        QuizAttempt attempt = resolveAttempt(request.getQuizAttemptId(), studentId);
        List<StudentEvaluation> priorEvaluations = evaluationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);

        try {
            String requestJson = objectMapper.writeValueAsString(buildGeminiRequest(request, attempt, priorEvaluations));
            HttpResponse<String> response = sendWithRetryAndFallback(requestJson);

            EvaluationMotivationSuggestionResponse suggestion;
            try {
                suggestion = parseSuggestionResponse(response.body());
            } catch (IOException parseException) {
                suggestion = recoverSuggestionFromRawText(response.body(), request);
                if (suggestion == null) {
                    String preview = extractSuggestionPreview(response.body());
                    throw new RuntimeException(
                            "Gemini returned a response, but it could not be parsed into a motivation suggestion. Raw Gemini text preview: " + preview,
                            parseException
                    );
                }
            }

            return normalizeSuggestion(suggestion, request);
        } catch (IOException exception) {
            throw new RuntimeException("Gemini returned a response, but it could not be parsed into a motivation suggestion.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Gemini motivation suggestion request was interrupted.", exception);
        }
    }

    private QuizAttempt resolveAttempt(Long attemptId, UUID studentId) {
        if (attemptId == null) {
            return null;
        }

        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));

        if (studentId != null && !attempt.getStudentId().equals(studentId)) {
            throw new RuntimeException("The selected quiz attempt does not belong to the chosen student");
        }

        return attempt;
    }

    private URI buildGenerateContentUri() {
        return buildGenerateContentUri(model);
    }

    private URI buildGenerateContentUri(String modelName) {
        String normalizedBaseUrl = baseUrl.replaceAll("/+$", "");
        return URI.create(normalizedBaseUrl + "/models/" + modelName + ":generateContent?key=" + apiKey.trim());
    }

    private HttpResponse<String> sendWithRetryAndFallback(String requestJson) throws IOException, InterruptedException {
        List<String> modelsToTry = resolveModelsToTry();
        int retries = Math.max(retryCount, 0);
        String lastErrorMessage = "Gemini request failed. Please verify the API key, model configuration, and network access.";

        for (String modelName : modelsToTry) {
            for (int attempt = 0; attempt <= retries; attempt++) {
                HttpResponse<String> response = sendRequest(requestJson, modelName);
                int status = response.statusCode();

                if (status >= 200 && status < 300) {
                    return response;
                }

                lastErrorMessage = extractErrorMessage(response.body());
                if (!isRetryableStatus(status, lastErrorMessage)) {
                    throw new GeminiUpstreamException(lastErrorMessage);
                }

                if (attempt < retries) {
                    Thread.sleep((long) (attempt + 1) * 800L);
                }
            }
        }

        throw new GeminiUpstreamException(lastErrorMessage);
    }

    private HttpResponse<String> sendRequest(String requestJson, String modelName) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(buildGenerateContentUri(modelName))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(45))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    private List<String> resolveModelsToTry() {
        List<String> models = new ArrayList<>();
        String primaryModel = model == null ? "" : model.trim();
        if (!primaryModel.isBlank()) {
            models.add(primaryModel);
        }

        String fallback = fallbackModels == null ? "" : fallbackModels.trim();
        if (!fallback.isBlank()) {
            for (String value : fallback.split(",")) {
                String candidate = value.trim();
                if (!candidate.isBlank() && !models.contains(candidate)) {
                    models.add(candidate);
                }
            }
        }

        if (models.isEmpty()) {
            models.add("gemini-2.5-flash");
        }

        return models;
    }

    private boolean isRetryableStatus(int status, String message) {
        if (status == 429 || status == 500 || status == 502 || status == 503 || status == 504) {
            return true;
        }
        String normalizedMessage = message == null ? "" : message.toLowerCase(Locale.ROOT);
        return normalizedMessage.contains("high demand") || normalizedMessage.contains("try again later");
    }

    private Map<String, Object> buildGeminiRequest(EvaluationMotivationSuggestionRequest request,
                                                    QuizAttempt attempt,
                                                    List<StudentEvaluation> priorEvaluations) {
        String prompt = buildPrompt(request, attempt, priorEvaluations);

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", List.of(
                Map.of(
                        "role", "user",
                        "parts", List.of(
                                Map.of(
                                        "text",
                                        "Return ONLY valid JSON with keys: headline, motivationMessage, strengthsSuggestion, recommendedActionsSuggestion, coachTip. No markdown. No extra text.\n\n" + prompt
                                )
                        )
                )
        ));
        payload.put("generationConfig", Map.of(
                "temperature", 0.75,
                "maxOutputTokens", 800,
                "responseMimeType", "application/json"
        ));

        return payload;
    }

    private String buildPrompt(EvaluationMotivationSuggestionRequest request,
                               QuizAttempt attempt,
                               List<StudentEvaluation> priorEvaluations) {
        String priorEvaluationTitles = priorEvaluations.isEmpty()
                ? "No previous evaluations saved yet."
                : priorEvaluations.stream()
                .limit(3)
                .map(StudentEvaluation::getTitle)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(title -> !title.isBlank())
                .collect(Collectors.joining("\n- ", "- ", ""));

        String attemptSummary = buildAttemptSummary(attempt);
        String studentLevel = safeText(request.getEnglishLevel(), "Not specified");
        String learningGoal = safeText(request.getLearningGoal(), "Not specified");
        String draftTitle = safeText(request.getTitle(), "No title drafted yet");
        String draftFeedback = safeText(request.getFeedback(), "No draft feedback yet");
        String draftStrengths = safeText(request.getStrengths(), "No strengths drafted yet");
        String draftAreas = safeText(request.getAreasToImprove(), "No improvement areas drafted yet");
        String draftActions = safeText(request.getRecommendedActions(), "No recommended actions drafted yet");
        String draftRating = request.getRating() != null ? request.getRating() + "/5" : "Not selected";

        return """
                Create a motivation suggestion for an admin/teacher writing a student evaluation.

                Student name: %s
                Student email: %s
                English level: %s
                Learning goal: %s

                Linked quiz attempt:
                %s

                Draft evaluation title: %s
                Draft rating: %s
                Draft feedback: %s
                Draft strengths: %s
                Draft areas to improve: %s
                Draft recommended actions: %s

                Recent evaluation titles:
                %s

                Requirements:
                - Keep the tone warm, motivating, and realistic.
                - Mention progress and effort, not only performance.
                - If quiz performance is low, stay encouraging without hiding the need to improve.
                - motivationMessage must be 2 to 4 concise sentences that an admin can reuse inside the feedback.
                - strengthsSuggestion must be a short sentence or two highlighting what to praise.
                - recommendedActionsSuggestion must contain 2 or 3 concrete next-step lines separated by newlines.
                - coachTip must be one short sentence for the admin about how to deliver the message well.
                - Do not use markdown headings.
                - Do not mention Groq or AI.
                """.formatted(
                safeText(request.getStudentName(), "Student"),
                normalizeEmail(request.getStudentEmail()),
                studentLevel,
                learningGoal,
                attemptSummary,
                draftTitle,
                draftRating,
                draftFeedback,
                draftStrengths,
                draftAreas,
                draftActions,
                priorEvaluationTitles
        );
    }

    private String buildAttemptSummary(QuizAttempt attempt) {
        if (attempt == null) {
            return "No quiz attempt linked. This is a general coaching evaluation.";
        }

        Quiz quiz = attempt.getQuiz();
        String quizTitle = quiz != null && quiz.getTitle() != null && !quiz.getTitle().isBlank()
                ? quiz.getTitle().trim()
                : "Quiz title unavailable";

        String scoreLabel = attempt.getScore() != null ? attempt.getScore() + "%" : "Score unavailable";
        String resultLabel = attempt.getPassed() == null
                ? "Result unavailable"
                : (attempt.getPassed() ? "Passed" : "Needs support");

        return """
                Quiz title: %s
                Score: %s
                Result: %s
                Submitted at: %s
                """.formatted(
                quizTitle,
                scoreLabel,
                resultLabel,
                attempt.getSubmittedAt() != null ? attempt.getSubmittedAt() : "Unavailable"
        );
    }

    private String extractSuggestionJson(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new RuntimeException("Gemini did not return a usable motivation suggestion.");
        }

        return textNode.asText();
    }

    private EvaluationMotivationSuggestionResponse parseSuggestionResponse(String responseBody) throws IOException {
        String rawText = extractSuggestionJson(responseBody);

        try {
            return objectMapper.readValue(rawText, EvaluationMotivationSuggestionResponse.class);
        } catch (IOException firstFailure) {
            String cleaned = cleanJsonLikeText(rawText);
            try {
                return objectMapper.readValue(cleaned, EvaluationMotivationSuggestionResponse.class);
            } catch (IOException ignored) {
                try {
                    return parseSuggestionByFieldMapping(cleaned);
                } catch (IOException ignoredToo) {
                    throw firstFailure;
                }
            }
        }
    }

    private EvaluationMotivationSuggestionResponse parseSuggestionByFieldMapping(String cleanedJson) throws IOException {
        JsonNode root = objectMapper.readTree(cleanedJson);

        EvaluationMotivationSuggestionResponse response = new EvaluationMotivationSuggestionResponse();
        response.setStudentName(firstNonBlank(root, "studentName", "student_name", "name"));
        response.setHeadline(firstNonBlank(root, "headline", "title", "subject"));
        response.setMotivationMessage(firstNonBlank(root, "motivationMessage", "motivation_message", "message", "feedback"));
        response.setStrengthsSuggestion(firstNonBlank(root, "strengthsSuggestion", "strengths_suggestion", "strengths", "strength"));
        response.setRecommendedActionsSuggestion(firstNonBlank(
                root,
                "recommendedActionsSuggestion",
                "recommended_actions_suggestion",
                "recommendedActions",
                "recommended_actions",
                "actions",
                "nextSteps",
                "next_steps"
        ));
        response.setCoachTip(firstNonBlank(root, "coachTip", "coach_tip", "tip"));

        return response;
    }

    private String firstNonBlank(JsonNode root, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode node = root.path(fieldName);
            if (!node.isMissingNode() && !node.isNull()) {
                String value = node.asText("");
                if (!value.isBlank()) {
                    return value.trim();
                }
            }
        }
        return null;
    }

    private EvaluationMotivationSuggestionResponse recoverSuggestionFromRawText(
            String responseBody,
            EvaluationMotivationSuggestionRequest request
    ) {
        String rawText;
        try {
            rawText = extractSuggestionJson(responseBody);
        } catch (Exception exception) {
            rawText = responseBody;
        }

        String source = rawText == null ? "" : rawText;
        if (source.isBlank()) {
            return null;
        }

        EvaluationMotivationSuggestionResponse recovered = new EvaluationMotivationSuggestionResponse();
        recovered.setStudentName(safeText(request.getStudentName(), "Student"));
        recovered.setHeadline(extractFieldValue(source, "headline", "title", "subject"));
        recovered.setMotivationMessage(extractFieldValue(source, "motivationMessage", "motivation_message", "message", "feedback"));
        recovered.setStrengthsSuggestion(extractFieldValue(source, "strengthsSuggestion", "strengths_suggestion", "strengths", "strength"));
        recovered.setRecommendedActionsSuggestion(extractFieldValue(
                source,
                "recommendedActionsSuggestion",
                "recommended_actions_suggestion",
                "recommendedActions",
                "recommended_actions",
                "actions",
                "nextSteps",
                "next_steps"
        ));
        recovered.setCoachTip(extractFieldValue(source, "coachTip", "coach_tip", "tip"));

        boolean hasAtLeastOneValue =
                notBlank(recovered.getHeadline()) ||
                notBlank(recovered.getMotivationMessage()) ||
                notBlank(recovered.getStrengthsSuggestion()) ||
                notBlank(recovered.getRecommendedActionsSuggestion()) ||
                notBlank(recovered.getCoachTip());

        return hasAtLeastOneValue ? recovered : null;
    }

    private String extractFieldValue(String text, String... keys) {
        for (String key : keys) {
            String value = extractQuotedJsonValue(text, key);
            if (!value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String extractQuotedJsonValue(String text, String key) {
        String patternText = "\""+ Pattern.quote(key) +"\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"";
        Pattern pattern = Pattern.compile(patternText, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return "";
        }
        String raw = matcher.group(1);
        String unescaped = raw
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();
        return unescaped;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String extractSuggestionPreview(String responseBody) {
        try {
            String text = extractSuggestionJson(responseBody);
            return abbreviate(text, 500);
        } catch (Exception ignored) {
            return abbreviate(responseBody, 500);
        }
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null) {
            return "<empty>";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return "<empty>";
        }
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(1, maxLength - 3)) + "...";
    }

    private String cleanJsonLikeText(String text) {
        if (text == null) {
            return "";
        }

        String cleaned = text.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
            cleaned = cleaned.replaceFirst("\\s*```$", "");
        }

        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }

        return cleaned.trim();
    }

    private EvaluationMotivationSuggestionResponse normalizeSuggestion(EvaluationMotivationSuggestionResponse suggestion,
                                                                       EvaluationMotivationSuggestionRequest request) {
        if (suggestion == null) {
            throw new RuntimeException("Gemini returned an empty motivation suggestion.");
        }

        suggestion.setStudentName(safeText(request.getStudentName(), "Student"));
        suggestion.setHeadline(safeText(suggestion.getHeadline(), "Encourage steady progress"));
        suggestion.setMotivationMessage(safeText(
                suggestion.getMotivationMessage(),
                "You are making progress, and with steady practice you can turn today’s feedback into stronger results next time."
        ));
        suggestion.setStrengthsSuggestion(safeText(
                suggestion.getStrengthsSuggestion(),
                "Highlight the student’s effort, consistency, and the parts of the quiz where progress is already visible."
        ));
        suggestion.setRecommendedActionsSuggestion(safeText(
                suggestion.getRecommendedActionsSuggestion(),
                "- Review the weakest quiz section again.\n- Practice with one short focused session before the next attempt."
        ));
        suggestion.setCoachTip(safeText(
                suggestion.getCoachTip(),
                "Lead with one concrete strength before introducing the improvement target."
        ));

        return suggestion;
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

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "unknown@example.com";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
