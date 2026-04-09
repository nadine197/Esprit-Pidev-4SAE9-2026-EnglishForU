import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';


platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));
<<<<<<< HEAD
(window as any).global = window;
=======
>>>>>>> 21f8a6f (metier avancer + controle de saisie)
