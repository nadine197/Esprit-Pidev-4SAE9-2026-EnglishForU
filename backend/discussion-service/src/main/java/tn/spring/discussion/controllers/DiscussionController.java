package tn.spring.discussion.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.spring.discussion.dtos.CreateDiscussionCommentRequest;
import tn.spring.discussion.dtos.CreateDiscussionPostRequest;
import tn.spring.discussion.dtos.CreateDiscussionReactionRequest;
import tn.spring.discussion.dtos.DiscussionCommentResponse;
import tn.spring.discussion.dtos.DiscussionPostResponse;
import tn.spring.discussion.services.DiscussionService;

import java.util.List;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @PostMapping("/posts")
    public ResponseEntity<DiscussionPostResponse> createPost(
            @Valid @RequestBody CreateDiscussionPostRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        DiscussionPostResponse response = discussionService.createPost(request, authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/feed")
    public ResponseEntity<List<DiscussionPostResponse>> feed(
            @RequestParam(required = false, defaultValue = "all") String scope,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String viewerLevel,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return ResponseEntity.ok(discussionService.getFeed(scope, level, courseId, viewerLevel, authorizationHeader));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<DiscussionPostResponse> getPost(
            @PathVariable Long postId,
            @RequestParam(required = false) String viewerLevel,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return ResponseEntity.ok(discussionService.getPost(postId, viewerLevel, authorizationHeader));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<DiscussionCommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateDiscussionCommentRequest request,
            @RequestParam(required = false) String viewerLevel,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        DiscussionCommentResponse response = discussionService.addComment(postId, request, viewerLevel, authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/posts/{postId}/reactions")
    public ResponseEntity<DiscussionPostResponse> react(
            @PathVariable Long postId,
            @Valid @RequestBody CreateDiscussionReactionRequest request,
            @RequestParam(required = false) String viewerLevel,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return ResponseEntity.ok(discussionService.reactToPost(postId, request, viewerLevel, authorizationHeader));
    }

    @PostMapping(value = "/posts/{postId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiscussionPostResponse> uploadImage(
            @PathVariable Long postId,
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return ResponseEntity.ok(discussionService.uploadPostImage(postId, file, authorizationHeader));
    }

    @GetMapping("/media/{fileName:.+}")
    public ResponseEntity<Resource> media(
            @PathVariable String fileName,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        DiscussionService.MediaPayload mediaPayload = discussionService.loadMedia(fileName, authorizationHeader);
        return ResponseEntity.ok()
                .contentType(mediaPayload.mediaType())
                .header(HttpHeaders.CACHE_CONTROL, "max-age=300")
                .body(mediaPayload.resource());
    }
}
