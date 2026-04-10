import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type DiscussionScope = 'ALL' | 'MINE' | 'OTHERS';
export type DiscussionPostType = 'TEXT' | 'IMAGE' | 'QUIZ';
export type DiscussionReactionType = 'LIKE' | 'LOVE' | 'CLAP' | 'INSIGHTFUL';

export interface DiscussionComment {
  id: number;
  postId: number;
  authorEmail: string;
  message: string;
  createdAt: string;
}

export interface DiscussionReaction {
  id: number;
  postId: number;
  authorEmail: string;
  type: DiscussionReactionType;
  createdAt: string;
}

export interface DiscussionUserPublicProfile {
  id: string;
  name: string;
  lastName: string;
}

export interface DiscussionPost {
  id: number;
  courseId: string;
  type: DiscussionPostType;
  content?: string | null;
  imagePath?: string | null;
  quizPayload?: string | null;
  authorEmail: string;
  authorRole: string;
  authorLevel?: string | null;
  targetRole?: string | null;
  targetLevel?: string | null;
  createdAt: string;
  updatedAt: string;
  commentCount: number;
  reactionCount: number;
  myReaction?: DiscussionReactionType | null;
  comments?: DiscussionComment[] | null;
  reactions?: DiscussionReaction[] | null;
}

export interface DiscussionFeedFilters {
  scope?: DiscussionScope;
  level?: string;
  courseId?: string;
  viewerLevel?: string;
}

export interface CreateDiscussionPostPayload {
  courseId: string;
  type: DiscussionPostType;
  content?: string;
  imagePath?: string;
  quizPayload?: string;
  targetRole?: string;
  targetLevel?: string;
  authorLevel?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DiscussionsService {

  constructor(private http: HttpClient) {}

  getFeed(filters: DiscussionFeedFilters = {}): Observable<DiscussionPost[]> {
    let params = new HttpParams();

    if (filters.scope) {
      params = params.set('scope', filters.scope);
    }

    if (filters.level?.trim()) {
      params = params.set('level', filters.level.trim());
    }

    if (filters.courseId?.trim()) {
      params = params.set('courseId', filters.courseId.trim());
    }

    if (filters.viewerLevel?.trim()) {
      params = params.set('viewerLevel', filters.viewerLevel.trim());
    }

    return this.http.get<DiscussionPost[]>(`${this.resolveBaseUrl()}/api/discussions/feed`, {
      headers: this.getHeaders(),
      params
    });
  }

  createPost(payload: CreateDiscussionPostPayload): Observable<DiscussionPost> {
    return this.http.post<DiscussionPost>(`${this.resolveBaseUrl()}/api/discussions/posts`, payload, {
      headers: this.getHeaders()
    });
  }

  getPost(postId: number, viewerLevel?: string): Observable<DiscussionPost> {
    let params = new HttpParams();
    if (viewerLevel?.trim()) {
      params = params.set('viewerLevel', viewerLevel.trim());
    }

    return this.http.get<DiscussionPost>(`${this.resolveBaseUrl()}/api/discussions/posts/${postId}`, {
      headers: this.getHeaders(),
      params
    });
  }

  addComment(postId: number, message: string, viewerLevel?: string): Observable<DiscussionComment> {
    let params = new HttpParams();
    if (viewerLevel?.trim()) {
      params = params.set('viewerLevel', viewerLevel.trim());
    }

    return this.http.post<DiscussionComment>(
      `${this.resolveBaseUrl()}/api/discussions/posts/${postId}/comments`,
      { message },
      {
        headers: this.getHeaders(),
        params
      }
    );
  }

  reactToPost(postId: number, type: DiscussionReactionType, viewerLevel?: string): Observable<DiscussionPost> {
    let params = new HttpParams();
    if (viewerLevel?.trim()) {
      params = params.set('viewerLevel', viewerLevel.trim());
    }

    return this.http.post<DiscussionPost>(
      `${this.resolveBaseUrl()}/api/discussions/posts/${postId}/reactions`,
      { type },
      {
        headers: this.getHeaders(),
        params
      }
    );
  }

  uploadPostImage(postId: number, file: File): Observable<DiscussionPost> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<DiscussionPost>(
      `${this.resolveBaseUrl()}/api/discussions/posts/${postId}/image`,
      formData,
      {
        headers: this.getHeaders()
      }
    );
  }

  getPublicUserByEmail(email: string): Observable<DiscussionUserPublicProfile> {
    const params = new HttpParams().set('email', email);

    return this.http.get<DiscussionUserPublicProfile>(
      `${this.resolveBaseUrl()}/api/users/public/by-email`,
      {
        headers: this.getHeaders(),
        params
      }
    );
  }

  loadMedia(fileName: string): Observable<Blob> {
    return this.http.get(
      `${this.resolveBaseUrl()}/api/discussions/media/${encodeURIComponent(fileName)}`,
      {
        headers: this.getHeaders(),
        responseType: 'blob'
      }
    );
  }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    if (!token) {
      return new HttpHeaders();
    }

    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  private resolveBaseUrl(): string {
    const gateway = environment.gatewayUrl?.trim();
    if (environment.useDirectBackend) {
      return environment.backendUrl;
    }

    return gateway || environment.backendUrl;
  }
}