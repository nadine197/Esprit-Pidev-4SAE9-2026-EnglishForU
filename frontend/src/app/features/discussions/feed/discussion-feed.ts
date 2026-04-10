import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { OnboardingTourService } from '../../../services/onboarding-tour.service';
import {
  CreateDiscussionPostPayload,
  DiscussionComment,
  DiscussionFeedFilters,
  DiscussionPost,
  DiscussionPostType,
  DiscussionReactionType,
  DiscussionScope,
  DiscussionUserPublicProfile,
  DiscussionsService
} from '../../../services/discussions.service';

interface PostComposerModel {
  targetLevel: string;
  type: DiscussionPostType;
  content: string;
  imagePath: string;
  quizQuestion: string;
  quizOptions: string[];
  quizCorrectOptionIndex: number | null;
}

interface QuizDetails {
  question: string;
  choices: string[];
  answer: string;
}

@Component({
  selector: 'app-discussion-feed',
  templateUrl: './discussion-feed.html',
  styleUrls: ['./discussion-feed.css']
})
export class DiscussionFeedComponent implements OnInit, OnDestroy {
  readonly scopeOptions: DiscussionScope[] = ['ALL', 'MINE', 'OTHERS'];
  readonly postTypeOptions: DiscussionPostType[] = ['TEXT', 'IMAGE', 'QUIZ'];
  readonly levelOptions: string[] = ['ALL_LEVELS', 'A1', 'A2', 'B1', 'B2', 'C1'];
  readonly viewerLevelOptions: string[] = ['A1', 'A2', 'B1', 'B2', 'C1'];
  readonly reactionOptions: Array<{ type: DiscussionReactionType; label: string }> = [
    { type: 'LIKE', label: 'Like 👍' },
    { type: 'LOVE', label: 'Love ❤️' },
    { type: 'CLAP', label: 'Clap 👏' },
    { type: 'INSIGHTFUL', label: 'Insightful 💡' }
  ];

  currentUser: Record<string, unknown> | null = null;

  filters: DiscussionFeedFilters = {
    scope: 'ALL',
    level: 'ALL_LEVELS',
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
  selectedQuizAnswers: Record<number, string> = {};

  mediaPreviewUrls: Record<number, string> = {};
  authorProfiles: Record<string, DiscussionUserPublicProfile> = {};
  private profileLookupInFlight: Record<string, boolean> = {};

  isLoadingFeed = false;
  isSubmittingPost = false;
  feedErrorMessage = '';
  createErrorMessage = '';
  selectedImageFile: File | null = null;

  constructor(
    private discussionsService: DiscussionsService,
    private authService: AuthService,
    private onboardingTourService: OnboardingTourService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser() as Record<string, unknown> | null;
    const inferredLevel = this.inferUserLevel(this.currentUser);

    if (inferredLevel) {
      this.filters.viewerLevel = inferredLevel;
    }

    this.loadFeed();
    setTimeout(() => this.onboardingTourService.startThreadPageTour(), 700);
  }

  ngOnDestroy(): void {
    this.revokeAllMediaPreviewUrls();
  }

  loadFeed(): void {
    this.isLoadingFeed = true;
    this.feedErrorMessage = '';

    const requestFilters: DiscussionFeedFilters = {
      scope: this.filters.scope,
      level: this.getOptionalLevel(this.filters.level) || undefined,
      viewerLevel: this.normalizeOptional(this.filters.viewerLevel) || undefined
    };

    this.discussionsService.getFeed(requestFilters).subscribe({
      next: (posts) => {
        this.posts = posts;
        this.isLoadingFeed = false;
        this.attachMediaPreviews(posts);
        this.ensureAuthorProfilesForPosts(posts);
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

    if (this.composer.type !== 'QUIZ') {
      this.composer.quizQuestion = '';
      this.composer.quizOptions = ['', ''];
      this.composer.quizCorrectOptionIndex = null;
    }
  }

  setComposerType(type: DiscussionPostType): void {
    this.composer.type = type;
    this.onComposerTypeChange();
  }

  onImageFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files.length > 0 ? input.files[0] : null;

    if (file && !this.isSupportedMediaFile(file)) {
      this.createErrorMessage = 'Only images and PDF files are supported.';
      this.selectedImageFile = null;
      input.value = '';
      return;
    }

    this.createErrorMessage = '';
    this.selectedImageFile = file;
    if (file) {
      this.composer.imagePath = '';
    }
  }

  clearSelectedImage(): void {
    this.selectedImageFile = null;
  }

  addQuizOption(): void {
    if (this.composer.quizOptions.length >= 6) {
      return;
    }

    this.composer.quizOptions = [...this.composer.quizOptions, ''];
  }

  removeQuizOption(index: number): void {
    if (this.composer.quizOptions.length <= 2) {
      return;
    }

    this.composer.quizOptions = this.composer.quizOptions.filter((_, optionIndex) => optionIndex !== index);

    if (this.composer.quizCorrectOptionIndex === index) {
      this.composer.quizCorrectOptionIndex = null;
      return;
    }

    if ((this.composer.quizCorrectOptionIndex ?? -1) > index) {
      this.composer.quizCorrectOptionIndex = (this.composer.quizCorrectOptionIndex ?? 0) - 1;
    }
  }

  setCorrectQuizOption(index: number): void {
    this.composer.quizCorrectOptionIndex = index;
  }

  getQuizOptionLabel(index: number): string {
    return String.fromCharCode(65 + index);
  }

  getComposerPostTypeLabel(type: DiscussionPostType): string {
    return type === 'IMAGE' ? 'MEDIA' : type;
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

  isPdfPost(post: DiscussionPost): boolean {
    const mediaPath = this.normalizeOptional(post.imagePath);
    if (!mediaPath) {
      return false;
    }

    return /\.pdf($|\?)/i.test(mediaPath);
  }

  getPostTypeBadgeLabel(post: DiscussionPost): string {
    if (post.type === 'IMAGE' && this.isPdfPost(post)) {
      return 'PDF';
    }

    return post.type;
  }

  getPostFileName(post: DiscussionPost): string {
    const mediaPath = this.normalizeOptional(post.imagePath);
    if (!mediaPath) {
      return 'attachment.pdf';
    }

    const cleanPath = mediaPath.split('?')[0];
    const fileNameCandidate = cleanPath.split('/').pop() || cleanPath;

    try {
      return decodeURIComponent(fileNameCandidate);
    } catch {
      return fileNameCandidate;
    }
  }

  getAuthorLabel(email: string): string {
    const normalizedEmail = this.normalizeEmail(email);
    if (!normalizedEmail) {
      return 'Anonymous learner';
    }

    const profile = this.authorProfiles[normalizedEmail];
    if (profile) {
      const fullName = `${profile.name || ''} ${profile.lastName || ''}`.trim();
      if (fullName) {
        return fullName;
      }
    }

    return this.buildNameFromEmail(normalizedEmail);
  }

  getAuthorInitials(email: string): string {
    const displayName = this.getAuthorLabel(email);
    return this.extractInitials(displayName);
  }

  getThreadToggleLabel(postId: number): string {
    return this.isThreadOpen(postId) ? 'Hide Thread' : 'Show Thread To Comment';
  }

  getLevelLabel(level: string | null | undefined): string {
    const normalized = this.normalizeOptional(level);
    if (!normalized || normalized.toUpperCase() === 'ALL_LEVELS') {
      return 'All Levels';
    }

    return normalized.toUpperCase();
  }

  selectQuizAnswer(postId: number, choice: string): void {
    this.selectedQuizAnswers[postId] = choice;
  }

  hasAnsweredQuiz(postId: number): boolean {
    return !!this.selectedQuizAnswers[postId];
  }

  getQuizChoiceClass(postId: number, quiz: QuizDetails, choice: string): string {
    const selected = this.selectedQuizAnswers[postId];
    if (!selected) {
      return 'quiz-choice quiz-choice--idle';
    }

    if (choice === quiz.answer) {
      return 'quiz-choice quiz-choice--correct';
    }

    if (choice === selected && selected !== quiz.answer) {
      return 'quiz-choice quiz-choice--wrong';
    }

    return 'quiz-choice quiz-choice--dimmed';
  }

  getQuizFeedback(postId: number, quiz: QuizDetails): string {
    const selected = this.selectedQuizAnswers[postId];
    if (!selected) {
      return 'Select an answer to reveal the correction.';
    }

    if (selected === quiz.answer) {
      return 'Correct answer. Great job!';
    }

    return `Not quite. Correct answer: ${quiz.answer}`;
  }

  getQuizDetails(post: DiscussionPost): QuizDetails | null {
    const rawPayload = this.normalizeOptional(post.quizPayload);
    if (!rawPayload) {
      return null;
    }

    try {
      const parsed = JSON.parse(rawPayload);
      const question = typeof parsed?.question === 'string' ? parsed.question.trim() : '';
      const choices = Array.isArray(parsed?.choices)
        ? parsed.choices
            .filter((choice: unknown): choice is string => typeof choice === 'string' && !!choice.trim())
            .map((choice: string) => choice.trim())
        : [];
      const answer = typeof parsed?.answer === 'string' ? parsed.answer.trim() : '';

      if (!question || choices.length < 2) {
        return null;
      }

      return { question, choices, answer };
    } catch {
      return null;
    }
  }

  trackByIndex(index: number): number {
    return index;
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
        this.ensureAuthorProfilesForPosts([post]);
        this.loadingThread[postId] = false;
      },
      error: (error) => {
        this.postErrors[postId] = error?.error?.message || 'Failed to load this thread.';
        this.loadingThread[postId] = false;
      }
    });
  }

  private mergeCreatedComment(postId: number, createdComment: DiscussionComment): void {
    this.ensureAuthorProfilesForEmails([createdComment.authorEmail]);

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
    const selectedLevel = this.normalizeOptional(this.composer.targetLevel)?.toUpperCase();
    const targetLevel = this.getOptionalLevel(selectedLevel);
    const courseId = targetLevel || 'ALL_LEVELS';
    const type = this.composer.type;
    const content = this.normalizeOptional(this.composer.content);
    const imagePath = this.normalizeOptional(this.composer.imagePath);
    const authorLevel = this.normalizeOptional(this.filters.viewerLevel);
    const quizPayload = type === 'QUIZ' ? this.buildQuizPayload() : null;

    if (type === 'TEXT' && !content) {
      this.createErrorMessage = 'Text posts require a message.';
      return null;
    }

    if (type === 'QUIZ' && !quizPayload) {
      return null;
    }

    if (type === 'IMAGE' && !imagePath && !this.selectedImageFile) {
      this.createErrorMessage = 'Media posts require either an uploaded image/PDF or a media URL.';
      return null;
    }

    return {
      courseId,
      type,
      content: content || undefined,
      quizPayload: quizPayload || undefined,
      imagePath: imagePath || undefined,
      targetLevel: targetLevel || undefined,
      authorLevel: authorLevel || undefined
    };
  }

  private buildQuizPayload(): string | null {
    const question = this.normalizeOptional(this.composer.quizQuestion);
    if (!question) {
      this.createErrorMessage = 'Quiz posts require a question.';
      return null;
    }

    const normalizedOptions = this.composer.quizOptions.map((option) => option.trim());
    const filledOptions = normalizedOptions.filter((option) => !!option);

    if (filledOptions.length < 2) {
      this.createErrorMessage = 'Please provide at least two quiz options.';
      return null;
    }

    const correctIndex = this.composer.quizCorrectOptionIndex;
    if (correctIndex === null || correctIndex < 0 || correctIndex >= normalizedOptions.length) {
      this.createErrorMessage = 'Please select the correct quiz answer.';
      return null;
    }

    const answer = normalizedOptions[correctIndex];
    if (!answer) {
      this.createErrorMessage = 'The selected correct answer cannot be empty.';
      return null;
    }

    return JSON.stringify({
      question,
      choices: filledOptions,
      answer
    });
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
    this.ensureAuthorProfilesForPosts([post]);
    this.composer = this.emptyComposer();
    this.selectedImageFile = null;

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

  private ensureAuthorProfilesForPosts(posts: DiscussionPost[]): void {
    const collectedEmails = new Set<string>();

    posts.forEach((post) => {
      const postAuthor = this.normalizeEmail(post.authorEmail);
      if (postAuthor) {
        collectedEmails.add(postAuthor);
      }

      (post.comments || []).forEach((comment) => {
        const commentAuthor = this.normalizeEmail(comment.authorEmail);
        if (commentAuthor) {
          collectedEmails.add(commentAuthor);
        }
      });
    });

    this.ensureAuthorProfilesForEmails(Array.from(collectedEmails));
  }

  private ensureAuthorProfilesForEmails(emails: string[]): void {
    emails.forEach((email) => {
      const normalized = this.normalizeEmail(email);
      if (!normalized) {
        return;
      }

      if (this.authorProfiles[normalized] || this.profileLookupInFlight[normalized]) {
        return;
      }

      this.profileLookupInFlight[normalized] = true;

      this.discussionsService.getPublicUserByEmail(normalized).subscribe({
        next: (profile) => {
          this.authorProfiles[normalized] = profile;
          delete this.profileLookupInFlight[normalized];
        },
        error: () => {
          delete this.profileLookupInFlight[normalized];
        }
      });
    });
  }

  private emptyComposer(): PostComposerModel {
    return {
      targetLevel: 'ALL_LEVELS',
      type: 'TEXT',
      content: '',
      imagePath: '',
      quizQuestion: '',
      quizOptions: ['', ''],
      quizCorrectOptionIndex: null
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

  private getOptionalLevel(level: unknown): string | null {
    const normalized = this.normalizeOptional(level)?.toUpperCase();
    if (!normalized || normalized === 'ALL_LEVELS') {
      return null;
    }

    return normalized;
  }

  private buildNameFromEmail(email: string): string {
    const [namePart] = email.split('@');
    if (!namePart) {
      return email;
    }

    return namePart
      .split(/[._-]+/)
      .filter(Boolean)
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
      .join(' ');
  }

  private extractInitials(name: string): string {
    const segments = name
      .split(/\s+/)
      .map((segment) => segment.trim())
      .filter(Boolean);

    if (segments.length === 0) {
      return 'NA';
    }

    const first = segments[0].charAt(0).toUpperCase();
    const second = segments.length > 1
      ? segments[1].charAt(0).toUpperCase()
      : (segments[0].charAt(1) || '').toUpperCase();

    return `${first}${second}`.trim();
  }

  private normalizeEmail(value: unknown): string | null {
    const normalized = this.normalizeOptional(value);
    return normalized ? normalized.toLowerCase() : null;
  }

  private isAbsoluteUrl(path: string): boolean {
    return /^https?:\/\//i.test(path);
  }

  private isSupportedMediaFile(file: File): boolean {
    const mimeType = (file.type || '').toLowerCase();
    if (mimeType.startsWith('image/') || mimeType === 'application/pdf') {
      return true;
    }

    return /\.(png|jpe?g|gif|bmp|webp|svg|pdf)$/i.test(file.name || '');
  }

  private normalizeOptional(value: unknown): string | null {
    if (typeof value !== 'string') {
      return null;
    }

    const trimmed = value.trim();
    return trimmed ? trimmed : null;
  }
}