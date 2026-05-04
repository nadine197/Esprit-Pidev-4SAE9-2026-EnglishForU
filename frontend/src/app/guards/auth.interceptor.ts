import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('token');
    const isAuthEndpoint = req.url.includes('/api/auth/');

    const authReq = token && !isAuthEndpoint
      ? req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`
          }
        })
      : req;

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        const isUnauthorized = error.status === 401 || error.status === 403;
        const isRefreshCall = req.url.includes('/api/auth/refresh');

        if (!isUnauthorized || isRefreshCall) {
          return throwError(() => error);
        }

        return this.authService.refreshToken().pipe(
          switchMap((res: any) => {
            const newToken = res?.token || localStorage.getItem('token');
            if (!newToken) {
              this.authService.logout();
              this.router.navigate(['/login']);
              return throwError(() => error);
            }

            const retriedReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${newToken}`
              }
            });
            return next.handle(retriedReq);
          }),
          catchError(() => {
            this.authService.logout();
            this.router.navigate(['/login']);
            return throwError(() => error);
          })
        );
      })
    );
  }
}
