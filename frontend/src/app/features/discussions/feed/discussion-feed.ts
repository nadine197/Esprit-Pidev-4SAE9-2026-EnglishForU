import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import {
  CreateDiscussionPostPayload,
  DiscussionComment,
  DiscussionFeedFilters,
  DiscussionPost,
  DiscussionPostType,
  DiscussionReactionType,
  DiscussionScope,
  DiscussionsService
} from '../../../services/discussions.service';

type AudienceRole = '' | 'STUDENT' | 'TUTOR' | 'HELP_DESK' | 'ADMIN' | 'SUPER_ADMIN';

interface PostComposerModel {
  courseId: string;
  type: DiscussionPostType;
  content: string;
  imagePath: string;
  quizPayload: string;
  targetRole: AudienceRole;
  targetLevel: string;
  authorLevel: string;
}

@Component({
  selector: 'app-discussion-feed',
  templateUrl: './discussion-feed.html',
  styleUrls: ['./discussion-feed.css']
})
export class DiscussionFeedComponent implements OnInit, OnDestroy {
  readonly scopeOptions: DiscussionScope[] = ['ALL', 'MINE', 'OTHERS'];
  readonly postTypeOptions: DiscussionPostType[] = ['TEXT', 'QUIZ', 'IMAGE'];
  readonly audienceRoleOptions: AudienceRole[] = ['', 'STUDENT', 'TUTOR', 'HELP_DESK', 'ADMIN', 'SUPER_ADMIN'];
  readonly reactionOptions: Array<{ type: DiscussionReactionType; label: string }> = [
    { type: 'LIKE', label: 'Like' },
    { type: 'LOVE', label: 'Love' },
    { type: 'CLAP', label: 'Clap' },
    { type: 'INSIGHTFUL', label: 'Insightful' }
  ];

  currentUser: Record<string, unknown> | null = null;

  filters: DiscussionFeedFilters = {
    scope: 'ALL',
    courseId: '',
    level: '',
    viewerLevel: ''
  };

  composer: PostComposerModel = this.emptyComposer();

  posts: DiscussionPost[] = [];
  postDetails: Record<number, DiscussionPost> = {};
  threadOpen: Record<number, boolean> = {};
  loadingThread: Record<number, boolean> = {};
  commentDrafts: Record<number, string> = {};
  sendingComment: Record<number, boolean> = {};
  reactingToPost: Record<number, boolean> = {};
  postErrors: Record<number, string> = {};

  mediaPreviewUrls: Record<number, string> = {};

  isLoadingFeed = false;
  isSubmittingPost = false;
  feedErrorMessage = '';
  createErrorMessage = '';
  selectedImageFile: File | null = null;

  constructor(
    private discussionsService: DiscussionsService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser() as Record<string, unknown> | null;
    const inferredLevel = this.inferUserLevel(this.currentUser);

    if (inferredLevel) {
      this.filters.viewerLevel = inferredLevel;
      this.composer.authorLevel = inferredLevel;
    }

    this.loadFeed();
  }

  ngOnDestroy(): void {
    this.revokeAllMediaPreviewUrls();
  }

  loadFeed(): void {
    this.isLoadingFeed = true;
    this.feedErrorMessage = '';

    this.discussionsService.getFeed(this.filters).subscribe({
      next: (posts) => {
        this.posts = posts;
        this.isLoadingFeed = false;
        this.attachMediaPreviews(posts);
      },
      error: (error) => {
        this.feedErrorMessage = error?.error?.message || 'Failed to load discussion feed.';
        this.posts = [];
        this.isLoadingFeed = false;
      }
    });
  }

  submitPost(): void {
    if (this.isSubmittingPost) {
      return;
    }

    const payload = this.buildCreatePostPayload();
    if (!payload) {
      return;
    }

    this.isSubmittingPost = true;
    this.createErrorMessage = '';

    this.discussionsService.createPost(payload).subscribe({
      next: (createdPost) => {
        if (payload.type === 'IMAGE' && this.selectedImageFile) {
          this.uploadImageForCreatedPost(createdPost, this.selectedImageFile);
          return;
        }

        this.finalizeCreatedPost(createdPost);
      },
      error: (error) => {
        this.createErrorMessage = error?.error?.message || 'Unable to publish post.';
        this.isSubmittingPost = false;
      }
    });
  }

  onComposerTypeChange(): void {
    if (this.composer.type !== 'IMAGE') {
      this.selectedImageFile = null;
      this.composer.imagePath = '';
    }
  }

  onImageFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files.length > 0 ? input.files[0] : null;

    this.selectedImageFile = file;
    if (file) {
      this.composer.imagePath = '';
    }
  }

  clearSelectedImage(): void {
    this.selectedImageFile = null;
  }

  toggleThread(postId: number): void {
    const isOpen = !!this.threadOpen[postId];
    this.threadOpen[postId] = !isOpen;

    if (!isOpen && !this.postDetails[postId]) {
      this.loadThread(postId);
    }
  }

  addComment(postId: number): void {
    if (this.sendingComment[postId]) {
      return;
    }

    const message = (this.commentDrafts[postId] || '').trim();
    if (!message) {
      this.postErrors[postId] = 'Comment message cannot be empty.';
      return;
    }

    this.sendingComment[postId] = true;
    this.postErrors[postId] = '';

    this.discussionsService.addComment(postId, message, this.normalizeOptional(this.filters.viewerLevel) || undefined).subscribe({
      next: (createdComment) => {
        this.commentDrafts[postId] = '';
        this.sendingComment[postId] = false;
        this.mergeCreatedComment(postId, createdComment);
      },
      error: (error) => {
        this.postErrors[postId] = error?.error?.message || 'Failed to add comment.';
        this.sendingComment[postId] = false;
      }
    });
  }

  setReaction(postId: number, reaction: DiscussionReactionType): void {
    if (this.reactingToPost[postId]) {
      return;
    }

    this.reactingToPost[postId] = true;
    this.postErrors[postId] = '';

    this.discussionsService.reactToPost(postId, reaction, this.normalizeOptional(this.filters.viewerLevel) || undefined).subscribe({
      next: (updatedPost) => {
        this.reactingToPost[postId] = false;
        this.postDetails[postId] = updatedPost;
        this.syncPostInFeed(updatedPost);
        this.attachMediaPreviews([updatedPost]);
      },
      error: (error) => {
        this.postErrors[postId] = error?.error?.message || 'Failed to update reaction.';
        this.reactingToPost[postId] = false;
      }
    });
  }

  getThread(postId: number): DiscussionPost | null {
    return this.postDetails[postId] || null;
  }

  getComments(postId: number): DiscussionComment[] {
    const comments = this.postDetails[postId]?.comments;
    return comments ? [...comments] : [];
  }

  isThreadLoading(postId: number): boolean {
    return !!this.loadingThread[postId];
  }

  isThreadOpen(postId: number): boolean {
    return !!this.threadOpen[postId];
  }

  isSendingComment(postId: number): boolean {
    return !!this.sendingComment[postId];
  }

  isReacting(postId: number): boolean {
    return !!this.reactingToPost[postId];
  }

  getPostImage(post: DiscussionPost): string | null {
    const imagePath = this.normalizeOptional(post.imagePath);
    if (!imagePath) {
      return null;
    }

    if (this.isAbsoluteUrl(imagePath)) {
      return imagePath;
    }

    return this.mediaPreviewUrls[post.id] || null;
  }

  getAuthorLabel(email: string): string {
    const normalizedEmail = this.normalizeOptional(email);
    if (!normalizedEmail) {
      return 'Anonymous learner';
    }

    const [namePart] = normalizedEmail.split('@');
    if (!namePart) {
      return normalizedEmail;
    }

    return namePart.replace(/[._-]+/g, ' ').trim();
  }

  trackByPostId(index: number, post: DiscussionPost): number {
    return post.id;
  }

  trackByCommentId(index: number, comment: DiscussionComment): number {
    return comment.id;
  }

  private loadThread(postId: number): void {
    this.loadingThread[postId] = true;
    this.postErrors[postId] = '';

    this.discussionsService.getPost(postId, this.normalizeOptional(this.filters.viewerLevel) || undefined).subscribe({
      next: (post) => {
        this.postDetails[postId] = post;
        this.syncPostInFeed(post);
        this.attachMediaPreviews([post]);
        this.loadingThread[postId] = false;
      },
      error: (error) => {
        this.postErrors[postId] = error?.error?.message || 'Failed to load this thread.';
        this.loadingThread[postId] = false;
      }
    });
  }

  private mergeCreatedComment(postId: number, createdComment: DiscussionComment): void {
    const existing = this.postDetails[postId];
    if (existing) {
      const updatedComments = [...(existing.comments || []), createdComment];
      const updatedPost: DiscussionPost = {
        ...existing,
        commentCount: existing.commentCount + 1,
        comments: updatedComments
      };

      this.postDetails[postId] = updatedPost;
      this.syncPostInFeed(updatedPost);
      return;
    }

    this.loadThread(postId);
  }

  private syncPostInFeed(updatedPost: DiscussionPost): void {
    const index = this.posts.findIndex((post) => post.id === updatedPost.id);
    if (index === -1) {
      return;
    }

    this.posts[index] = {
      ...this.posts[index],
      ...updatedPost
    };

    this.posts = [...this.posts];
  }

  private buildCreatePostPayload(): CreateDiscussionPostPayload | null {
    const courseId = this.normalizeOptional(this.composer.courseId);
    const type = this.composer.type;
    const content = this.normalizeOptional(this.composer.content);
    const quizPayload = this.normalizeOptional(this.composer.quizPayload);
    const imagePath = this.normalizeOptional(this.composer.imagePath);
    const targetRole = this.normalizeOptional(this.composer.targetRole);
    const targetLevel = this.normalizeOptional(this.composer.targetLevel);
    const authorLevel = this.normalizeOptional(this.composer.authorLevel);

    if (!courseId) {
      this.createErrorMessage = 'Course ID is required.';
      return null;
    }

    if (type === 'TEXT' && !content) {
      this.createErrorMessage = 'Text posts require a message.';
      return null;
    }

    if (type === 'QUIZ' && !quizPayload) {
      this.createErrorMessage = 'Quiz posts require quiz payload content.';
      return null;
    }

    if (type === 'IMAGE' && !imagePath && !this.selectedImageFile) {
      this.createErrorMessage = 'Image posts require either an uploaded file or image path.';
      return null;
    }

    return {
      courseId,
      type,
      content: content || undefined,
      quizPayload: quizPayload || undefined,
      imagePath: imagePath || undefined,
      targetRole: targetRole || undefined,
      targetLevel: targetLevel || undefined,
      authorLevel: authorLevel || undefined
    };
  }

  private uploadImageForCreatedPost(createdPost: DiscussionPost, file: File): void {
    this.discussionsService.uploadPostImage(createdPost.id, file).subscribe({
      next: (updatedPost) => {
        this.finalizeCreatedPost(updatedPost);
      },
      error: (error) => {
        this.createErrorMessage = error?.error?.message || 'Post created but image upload failed.';
        this.finalizeCreatedPost(createdPost);
      }
    });
  }

  private finalizeCreatedPost(post: DiscussionPost): void {
    this.posts = [post, ...this.posts];
    this.attachMediaPreviews([post]);
    this.composer = this.emptyComposer();
    this.selectedImageFile = null;

    const inferredLevel = this.inferUserLevel(this.currentUser);
    if (inferredLevel) {
      this.composer.authorLevel = inferredLevel;
    }

    this.isSubmittingPost = false;
  }

  private attachMediaPreviews(posts: DiscussionPost[]): void {
    posts.forEach((post) => {
      const imagePath = this.normalizeOptional(post.imagePath);
      if (!imagePath || post.type !== 'IMAGE') {
        return;
      }

      if (this.isAbsoluteUrl(imagePath) || this.mediaPreviewUrls[post.id]) {
        return;
      }

      this.discussionsService.loadMedia(imagePath).subscribe({
        next: (blob) => {
          const previousUrl = this.mediaPreviewUrls[post.id];
          if (previousUrl) {
            URL.revokeObjectURL(previousUrl);
          }

          this.mediaPreviewUrls[post.id] = URL.createObjectURL(blob);
        },
        error: () => {
          // Keep the card visible even when media preview cannot be fetched.
        }
      });
    });
  }

  private revokeAllMediaPreviewUrls(): void {
    Object.keys(this.mediaPreviewUrls).forEach((postId) => {
      const url = this.mediaPreviewUrls[Number(postId)];
      if (url) {
        URL.revokeObjectURL(url);
      }
    });
  }

  private emptyComposer(): PostComposerModel {
    return {
      courseId: '',
      type: 'TEXT',
      content: '',
      imagePath: '',
      quizPayload: '',
      targetRole: '',
      targetLevel: '',
      authorLevel: ''
    };
  }

  private inferUserLevel(user: Record<string, unknown> | null): string {
    if (!user) {
      return '';
    }

    const candidateKeys = ['level', 'authorLevel', 'viewerLevel', 'languageLevel', 'englishLevel', 'cefrLevel'];

    for (const key of candidateKeys) {
      const value = user[key];
      if (typeof value === 'string' && value.trim()) {
        return value.trim().toUpperCase();
      }
    }

    return '';
  }

  private isAbsoluteUrl(path: string): boolean {
    return /^https?:\/\//i.test(path);
  }

  private normalizeOptional(value: unknown): string | null {
    if (typeof value !== 'string') {
      return null;
    }

    const trimmed = value.trim();
    return trimmed ? trimmed : null;
  }
}