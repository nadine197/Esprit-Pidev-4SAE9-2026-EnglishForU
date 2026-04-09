package tn.spring.discussion.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.discussion.dtos.CreateDiscussionCommentRequest;
import tn.spring.discussion.dtos.CreateDiscussionPostRequest;
import tn.spring.discussion.dtos.CreateDiscussionReactionRequest;
import tn.spring.discussion.dtos.DiscussionCommentResponse;
import tn.spring.discussion.dtos.DiscussionPostResponse;
import tn.spring.discussion.dtos.DiscussionReactionResponse;
import tn.spring.discussion.enums.DiscussionPostType;
import tn.spring.discussion.enums.DiscussionReactionType;
import tn.spring.discussion.enums.DiscussionScope;
import tn.spring.discussion.models.DiscussionComment;
import tn.spring.discussion.models.DiscussionPost;
import tn.spring.discussion.models.DiscussionReaction;
import tn.spring.discussion.repositories.DiscussionCommentRepository;
import tn.spring.discussion.repositories.DiscussionPostRepository;
import tn.spring.discussion.repositories.DiscussionReactionRepository;
import tn.spring.discussion.security.AuthenticatedUser;
import tn.spring.discussion.security.JwtUserContextResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DiscussionService {

    private final DiscussionPostRepository postRepository;
    private final DiscussionCommentRepository commentRepository;
    private final DiscussionReactionRepository reactionRepository;
    private final JwtUserContextResolver jwtUserContextResolver;

    @Value("${discussion.storage.path:uploads/discussions}")
    private String storagePath;

    public DiscussionPostResponse createPost(CreateDiscussionPostRequest request, String authorizationHeader) {
        AuthenticatedUser user = jwtUserContextResolver.resolveRequired(authorizationHeader);
        validateCreatePostRequest(request);

        DiscussionPost post = DiscussionPost.builder()
                .courseId(request.getCourseId().trim())
                .type(request.getType())
                .content(nullableTrim(request.getContent()))
                .imagePath(nullableTrim(request.getImagePath()))
                .quizPayload(nullableTrim(request.getQuizPayload()))
                .authorEmail(user.email())
                .authorRole(user.role())
                .authorLevel(normalizeNullable(request.getAuthorLevel()))
                .targetRole(normalizeNullable(request.getTargetRole()))
                .targetLevel(normalizeNullable(request.getTargetLevel()))
                .build();

        DiscussionPost saved = postRepository.save(post);
        return toPostResponse(saved, user.email(), false);
    }

    @Transactional(readOnly = true)
    public List<DiscussionPostResponse> getFeed(String scope,
                                                String level,
                                                String courseId,
                                                String viewerLevel,
                                                String authorizationHeader) {
        AuthenticatedUser user = jwtUserContextResolver.resolveRequired(authorizationHeader);
        DiscussionScope feedScope = parseScope(scope);

        String normalizedLevel = normalizeNullable(level);
        String normalizedCourseId = nullableTrim(courseId);
        String normalizedViewerLevel = normalizeNullable(viewerLevel);

        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(post -> matchesScope(post, user, feedScope))
                .filter(post -> normalizedCourseId == null || post.getCourseId().equalsIgnoreCase(normalizedCourseId))
                .filter(post -> normalizedLevel == null || matchesLevelFilter(post, normalizedLevel))
                .filter(post -> canViewPost(post, user, normalizedViewerLevel))
                .map(post -> toPostResponse(post, user.email(), false))
                .toList();
    }

    @Transactional(readOnly = true)
    public DiscussionPostResponse getPost(Long postId,
                                          String viewerLevel,
                                          String authorizationHeader) {
        AuthenticatedUser user = jwtUserContextResolver.resolveRequired(authorizationHeader);
        DiscussionPost post = findPost(postId);

        if (!canViewPost(post, user, normalizeNullable(viewerLevel))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "POST_ACCESS_FORBIDDEN");
        }

        return toPostResponse(post, user.email(), true);
    }

    public DiscussionCommentResponse addComment(Long postId,
                                                CreateDiscussionCommentRequest request,
                                                String viewerLevel,
                                                String authorizationHeader) {
        AuthenticatedUser user = jwtUserContextResolver.resolveRequired(authorizationHeader);
        DiscussionPost post = findPost(postId);

        if (!canViewPost(post, user, normalizeNullable(viewerLevel))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "POST_ACCESS_FORBIDDEN");
        }

        String message = nullableTrim(request.getMessage());
        if (message == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "COMMENT_MESSAGE_REQUIRED");
        }

        DiscussionComment comment = DiscussionComment.builder()
                .post(post)
                .authorEmail(user.email())
                .message(message)
                .build();

        DiscussionComment saved = commentRepository.save(comment);
        return toCommentResponse(saved);
    }

    public DiscussionPostResponse reactToPost(Long postId,
                                              CreateDiscussionReactionRequest request,
                                              String viewerLevel,
                                              String authorizationHeader) {
        AuthenticatedUser user = jwtUserContextResolver.resolveRequired(authorizationHeader);
        DiscussionPost post = findPost(postId);

        if (!canViewPost(post, user, normalizeNullable(viewerLevel))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "POST_ACCESS_FORBIDDEN");
        }

        if (request.getType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "REACTION_TYPE_REQUIRED");
        }

        DiscussionReaction reaction = reactionRepository.findByPostAndAuthorEmailIgnoreCase(post, user.email())
                .orElseGet(() -> DiscussionReaction.builder()
                        .post(post)
                        .authorEmail(user.email())
                        .build());

        reaction.setType(request.getType());
        reactionRepository.save(reaction);

        return toPostResponse(post, user.email(), true);
    }

    public DiscussionPostResponse uploadPostImage(Long postId,
                                                  MultipartFile file,
                                                  String authorizationHeader) {
        AuthenticatedUser user = jwtUserContextResolver.resolveRequired(authorizationHeader);
        DiscussionPost post = findPost(postId);

        if (!post.getAuthorEmail().equalsIgnoreCase(user.email()) && !isStaffRole(user.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "POST_IMAGE_UPLOAD_FORBIDDEN");
        }

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IMAGE_FILE_REQUIRED");
        }

        String storedFileName = storeImageFile(file);
        post.setType(DiscussionPostType.IMAGE);
        post.setImagePath(storedFileName);

        DiscussionPost saved = postRepository.save(post);
        return toPostResponse(saved, user.email(), true);
    }

    @Transactional(readOnly = true)
    public MediaPayload loadMedia(String fileName, String authorizationHeader) {
        jwtUserContextResolver.resolveRequired(authorizationHeader);

        Path rootPath = Paths.get(storagePath).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(fileName).normalize();

        if (!targetPath.startsWith(rootPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_MEDIA_PATH");
        }

        try {
            Resource resource = new UrlResource(targetPath.toUri());
            if (!resource.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MEDIA_NOT_FOUND");
            }

            MediaType mediaType = MediaTypeFactory.getMediaType(fileName)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);
            return new MediaPayload(resource, mediaType);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "MEDIA_READ_FAILED");
        }
    }

    private DiscussionPost findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "POST_NOT_FOUND"));
    }

    private DiscussionScope parseScope(String scope) {
        if (scope == null || scope.isBlank()) {
            return DiscussionScope.ALL;
        }

        try {
            return DiscussionScope.valueOf(scope.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_DISCUSSION_SCOPE");
        }
    }

    private boolean matchesScope(DiscussionPost post, AuthenticatedUser user, DiscussionScope scope) {
        return switch (scope) {
            case ALL -> true;
            case MINE -> post.getAuthorEmail().equalsIgnoreCase(user.email());
            case OTHERS -> !post.getAuthorEmail().equalsIgnoreCase(user.email());
        };
    }

    private boolean matchesLevelFilter(DiscussionPost post, String levelFilter) {
        if (post.getAuthorLevel() != null && post.getAuthorLevel().equalsIgnoreCase(levelFilter)) {
            return true;
        }

        return post.getTargetLevel() != null && post.getTargetLevel().equalsIgnoreCase(levelFilter);
    }

    private boolean canViewPost(DiscussionPost post, AuthenticatedUser user, String viewerLevel) {
        if (post.getTargetRole() != null && !post.getTargetRole().isBlank()) {
            if (!post.getTargetRole().equalsIgnoreCase(user.role())) {
                return false;
            }
        }

        if (post.getTargetLevel() != null && !post.getTargetLevel().isBlank()) {
            if (viewerLevel == null || !post.getTargetLevel().equalsIgnoreCase(viewerLevel)) {
                return false;
            }
        }

        return true;
    }

    private void validateCreatePostRequest(CreateDiscussionPostRequest request) {
        if (request.getType() == DiscussionPostType.TEXT && nullableTrim(request.getContent()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TEXT_CONTENT_REQUIRED");
        }

        if (request.getType() == DiscussionPostType.IMAGE && nullableTrim(request.getImagePath()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IMAGE_PATH_REQUIRED");
        }

        if (request.getType() == DiscussionPostType.QUIZ && nullableTrim(request.getQuizPayload()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QUIZ_PAYLOAD_REQUIRED");
        }
    }

    private DiscussionPostResponse toPostResponse(DiscussionPost post,
                                                  String currentUserEmail,
                                                  boolean includeDetails) {
        DiscussionReactionType myReaction = reactionRepository.findByPostAndAuthorEmailIgnoreCase(post, currentUserEmail)
                .map(DiscussionReaction::getType)
                .orElse(null);

        List<DiscussionCommentResponse> comments = includeDetails
                ? commentRepository.findByPostOrderByCreatedAtAsc(post).stream()
                .map(this::toCommentResponse)
                .toList()
                : null;

        List<DiscussionReactionResponse> reactions = includeDetails
                ? reactionRepository.findByPost(post).stream()
                .sorted(Comparator.comparing(DiscussionReaction::getCreatedAt))
                .map(this::toReactionResponse)
                .toList()
                : null;

        return DiscussionPostResponse.builder()
                .id(post.getId())
                .courseId(post.getCourseId())
                .type(post.getType())
                .content(post.getContent())
                .imagePath(post.getImagePath())
                .quizPayload(post.getQuizPayload())
                .authorEmail(post.getAuthorEmail())
                .authorRole(post.getAuthorRole())
                .authorLevel(post.getAuthorLevel())
                .targetRole(post.getTargetRole())
                .targetLevel(post.getTargetLevel())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .commentCount(commentRepository.countByPost(post))
                .reactionCount(reactionRepository.countByPost(post))
                .myReaction(myReaction)
                .comments(comments)
                .reactions(reactions)
                .build();
    }

    private DiscussionCommentResponse toCommentResponse(DiscussionComment comment) {
        return DiscussionCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .authorEmail(comment.getAuthorEmail())
                .message(comment.getMessage())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private DiscussionReactionResponse toReactionResponse(DiscussionReaction reaction) {
        return DiscussionReactionResponse.builder()
                .id(reaction.getId())
                .postId(reaction.getPost().getId())
                .authorEmail(reaction.getAuthorEmail())
                .type(reaction.getType())
                .createdAt(reaction.getCreatedAt())
                .build();
    }

    private String nullableTrim(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeNullable(String value) {
        String trimmed = nullableTrim(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    private boolean isStaffRole(String role) {
        return "HELP_DESK".equalsIgnoreCase(role)
                || "ADMIN".equalsIgnoreCase(role)
                || "SUPER_ADMIN".equalsIgnoreCase(role);
    }

    private String storeImageFile(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String extension = "";

        if (originalName != null) {
            int extensionIndex = originalName.lastIndexOf('.');
            if (extensionIndex >= 0 && extensionIndex < originalName.length() - 1) {
                extension = originalName.substring(extensionIndex).toLowerCase(Locale.ROOT);
            }
        }

        String fileName = UUID.randomUUID() + extension;

        Path rootPath = Paths.get(storagePath).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(fileName).normalize();

        if (!targetPath.startsWith(rootPath)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_STORAGE_PATH_INVALID");
        }

        try {
            Files.createDirectories(rootPath);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return fileName;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_STORAGE_FAILED");
        }
    }

    public record MediaPayload(Resource resource, MediaType mediaType) {
    }
}
