import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription, filter } from 'rxjs';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'JobBoard';
  showFloatingUi = false;

  private routerSub?: Subscription;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.updateFloatingUiVisibility();

    this.routerSub = this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => this.updateFloatingUiVisibility());
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  private updateFloatingUiVisibility(): void {
    const isAuthenticated = this.authService.isLoggedIn();
    const hiddenRoutes = ['/login', '/register'];
    const isHiddenRoute = hiddenRoutes.some((route) => this.router.url.startsWith(route));

    this.showFloatingUi = isAuthenticated && !isHiddenRoute;
  }
}
